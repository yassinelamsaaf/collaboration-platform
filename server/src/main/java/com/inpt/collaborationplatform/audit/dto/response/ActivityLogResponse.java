package com.inpt.collaborationplatform.audit.dto.response;

import java.time.LocalDateTime;

public record ActivityLogResponse(
        String id,
        String actorId,
        String projectId,
        String entityType,
        String entityId,
        String action,
        String details,
        LocalDateTime timestamp,
        String actorName
) {
}
