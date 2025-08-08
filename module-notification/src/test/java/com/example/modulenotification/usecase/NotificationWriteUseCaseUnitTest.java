package com.example.modulenotification.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Notification;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.modulenotification.service.NotificationDataService;
import com.example.modulenotification.service.NotificationDomainService;
import com.example.moduleuser.service.UserDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationWriteUseCaseUnitTest {

    @InjectMocks
    private NotificationWriteUseCase notificationWriteUseCase;

    @Mock
    private NotificationDataService notificationDataService;

    @Mock
    private NotificationDomainService notificationDomainService;

    @Mock
    private UserDataService userDataService;

    @Test
    @DisplayName(value = "알림 처리")
    void sendNotification() {
        NotificationSendDTO sendDTO = new NotificationSendDTO(
                "tester",
                NotificationType.MEMBER_QNA_REPLY,
                NotificationType.MEMBER_QNA_REPLY.getTitle(),
                1L
        );
        Member member = Member.builder().userId(sendDTO.userId()).build();
        Notification notification = Notification.builder()
                .id(1L)
                .build();

        when(userDataService.getMemberByUserIdOrElseNull(any()))
                .thenReturn(member);
        when(notificationDomainService.buildNotificationEntity(any(Member.class), any(NotificationSendDTO.class)))
                .thenReturn(notification);
        doNothing().when(notificationDataService).saveNotification(any(Notification.class));

        assertDoesNotThrow(() -> notificationWriteUseCase.saveNotificationData(sendDTO));
    }

    @Test
    @DisplayName(value = "알림 처리. 사용자 데이터가 없는 경우")
    void sendNotificationMemberIsNull() {
        NotificationSendDTO sendDTO = new NotificationSendDTO(
                "tester",
                NotificationType.MEMBER_QNA_REPLY,
                NotificationType.MEMBER_QNA_REPLY.getTitle(),
                1L
        );

        when(userDataService.getMemberByUserIdOrElseNull(any()))
                .thenReturn(null);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> notificationWriteUseCase.saveNotificationData(sendDTO)
        );

        verify(notificationDomainService, never()).buildNotificationEntity(any(Member.class), any(NotificationSendDTO.class));
        verify(notificationDataService, never()).saveNotification(any(Notification.class));
    }
}
