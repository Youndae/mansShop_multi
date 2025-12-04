package com.example.moduleadmin.service.failedData;

import com.example.moduleadmin.model.dto.failedData.out.FailedQueueDTO;
import com.example.moduleconfig.properties.RabbitMQConnectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminFailedDataExternalService {

    private final RabbitMQConnectionProperties rabbitMQConnProperties;

    private final RabbitTemplate rabbitTemplate;

    private final Jackson2JsonMessageConverter converter;

    public int getDLQMessageCount(String queueName) {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:15672")
                .defaultHeaders(headers ->
                        headers.setBasicAuth(
                                rabbitMQConnProperties.getUsername(),
                                rabbitMQConnProperties.getPassword()
                        )
                )
                .build();

        return (int) webClient.get()
                .uri(builder ->
                        builder.path("/api/queues/{vhost}/{queueNames}")
                                .build("/", queueName)
                )
                .retrieve()
                .bodyToMono(Map.class)
                .block()
                .get("messages");
    }

    public void retryMessage(List<FailedQueueDTO> failedQueueDTOList) {
        failedQueueDTOList.forEach(this::retryMessage);
    }

    private void retryMessage(FailedQueueDTO dto) {
        for(int i = 0; i < dto.messageCount(); i++) {
            Message message = rabbitTemplate.receive(dto.queueName());
            if(message != null) {
                Object data = converter.fromMessage(message);
                Map<String, Object> headers = message.getMessageProperties().getHeaders();
                List<Map<String, Object>> xDeathList = (List<Map<String, Object>>) headers.get("x-death");
                if(xDeathList != null && !xDeathList.isEmpty()) {
                    Map<String, Object> xDeath = xDeathList.get(0);
                    String exchange = (String) xDeath.get("exchange");
                    List<String> routingKeyList = (List<String>) xDeath.get("routing-keys");
                    String routingKey = routingKeyList.get(0);
                    rabbitTemplate.convertAndSend(exchange, routingKey, data);
                }
            }
        }
    }
}
