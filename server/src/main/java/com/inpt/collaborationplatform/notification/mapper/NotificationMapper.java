package com.inpt.collaborationplatform.notification.mapper;

import com.inpt.collaborationplatform.notification.dto.response.NotificationResponse;
import com.inpt.collaborationplatform.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.getCreatedAt()
        );
    }
}
