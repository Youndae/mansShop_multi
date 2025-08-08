package com.example.moduleproduct.service.productQnA;

import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.moduleconfig.config.rabbitMQ.RabbitMQPrefix;
import com.example.moduleconfig.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductQnAExternalService {

    private final RabbitMQProperties rabbitMQProperties;

    private final RabbitTemplate rabbitTemplate;

    public void sendProductQnANotification(ProductQnA productQnA) {
        String notificationTitle = productQnA.getProduct().getProductName() + NotificationType.PRODUCT_QNA_REPLY.getTitle();

        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange().get(RabbitMQPrefix.EXCHANGE_NOTIFICATION.getKey()).getName(),
                rabbitMQProperties.getQueue().get(RabbitMQPrefix.QUEUE_NOTIFICATION.getKey()).getRouting(),
                new NotificationSendDTO(
                        productQnA.getMember().getUserId(),
                        NotificationType.PRODUCT_QNA_REPLY,
                        notificationTitle,
                        productQnA.getId()
                )
        );
    }
}
