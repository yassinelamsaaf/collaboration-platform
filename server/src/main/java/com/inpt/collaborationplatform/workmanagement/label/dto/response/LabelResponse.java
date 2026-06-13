package com.inpt.collaborationplatform.workmanagement.label.dto.response;

import java.time.LocalDateTime;

public record LabelResponse(
        String id,
        String projectId,
        String name,
        String color,
        LocalDateTime createdAt
) {
}
