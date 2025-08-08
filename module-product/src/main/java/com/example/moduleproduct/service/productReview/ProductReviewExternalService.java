package com.example.moduleproduct.service.productReview;

import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.moduleconfig.config.rabbitMQ.RabbitMQPrefix;
import com.example.moduleconfig.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductReviewExternalService {

    private final RabbitMQProperties rabbitMQProperties;

    private final RabbitTemplate rabbitTemplate;

    public void sendProductReviewNotification(String userId) {
        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange().get(RabbitMQPrefix.EXCHANGE_NOTIFICATION.getKey()).getName(),
                rabbitMQProperties.getQueue().get(RabbitMQPrefix.QUEUE_NOTIFICATION.getKey()).getRouting(),
                new NotificationSendDTO(
                        userId,
                        NotificationType.REVIEW_REPLY,
                        NotificationType.REVIEW_REPLY.getTitle(),
                        null
                )
        );
    }
}
