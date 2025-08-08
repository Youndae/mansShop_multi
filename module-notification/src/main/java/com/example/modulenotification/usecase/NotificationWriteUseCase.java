package com.example.modulenotification.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Notification;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulenotification.service.NotificationDataService;
import com.example.modulenotification.service.NotificationDomainService;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationWriteUseCase {

    private final NotificationDataService notificationDataService;

    private final NotificationDomainService notificationDomainService;

    private final UserDataService userDataService;

    public void saveNotificationData(NotificationSendDTO sendDTO) {
        String userId = sendDTO.userId();
        Member member = userDataService.getMemberByUserIdOrElseNull(userId);

        if(member == null) {
            log.info("SendNotification Member not found. userId: {}", userId);
            throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage());
        }

        Notification notification = notificationDomainService.buildNotificationEntity(member, sendDTO);
        notificationDataService.saveNotification(notification);
    }

    public boolean isUseOnline(String userId) {
        return notificationDataService.isUserOnline(userId);
    }

    public void updateUserOnlineStatus(String userId) {
        notificationDataService.updateUserOnlineStatus(userId);
    }
}
