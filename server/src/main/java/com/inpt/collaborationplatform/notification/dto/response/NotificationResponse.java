package com.inpt.collaborationplatform.notification.dto.response;

import com.inpt.collaborationplatform.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String userId,
        NotificationType type,
        String title,
        String message,
        boolean isRead,
        String relatedEntityType,
        String relatedEntityId,
        LocalDateTime createdAt
) {
}
