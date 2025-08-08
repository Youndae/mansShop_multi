package com.example.moduleapi.usecase;

import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulenotification.model.dto.out.NotificationDTO;
import com.example.modulenotification.usecase.NotificationWriteUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiNotificationWriteUseCase {

    private final NotificationWriteUseCase notificationWriteUseCase;

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(NotificationSendDTO sendDTO) {
        notificationWriteUseCase.saveNotificationData(sendDTO);

        if(notificationWriteUseCase.isUseOnline(sendDTO.userId())){
            NotificationDTO responseMessage = new NotificationDTO(sendDTO.title(), sendDTO.relatedId());
            messagingTemplate.convertAndSendToUser(sendDTO.userId(), "/queue/notifications", responseMessage);
        }
    }
}
