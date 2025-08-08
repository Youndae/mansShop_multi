package com.example.modulenotification.service;

import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Notification;
import org.springframework.stereotype.Service;

@Service
public class NotificationDomainService {
    public Notification buildNotificationEntity(Member member, NotificationSendDTO sendDTO) {
        return Notification.builder()
                .member(member)
                .type(sendDTO.type().getType())
                .title(sendDTO.title())
                .relatedId(sendDTO.relatedId())
                .isRead(false)
                .build();
    }
}
