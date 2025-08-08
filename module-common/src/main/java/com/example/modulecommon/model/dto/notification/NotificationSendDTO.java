package com.example.modulecommon.model.dto.notification;

import com.example.modulecommon.model.enumuration.NotificationType;

public record NotificationSendDTO(
        String userId,
        NotificationType type,
        String title,
        Long relatedId
) {
}
