package com.example.moduleapi.consumer;

import com.example.moduleapi.usecase.ApiNotificationWriteUseCase;
import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationConsumer {

    private final ApiNotificationWriteUseCase apiNotificationWriteUseCase;

    public NotificationConsumer(ApiNotificationWriteUseCase apiNotificationWriteUseCase) {
        this.apiNotificationWriteUseCase = apiNotificationWriteUseCase;
    }

    @RabbitListener(queues = "${rabbitmq.queue.notificationSend.name}", concurrency = "3")
    public void consumeNotification(NotificationSendDTO notificationDTO) {
        apiNotificationWriteUseCase.sendNotification(notificationDTO);
    }
}
