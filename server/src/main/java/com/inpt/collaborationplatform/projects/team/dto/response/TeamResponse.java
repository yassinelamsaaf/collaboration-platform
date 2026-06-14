package com.inpt.collaborationplatform.projects.team.dto.response;

import java.time.LocalDateTime;

public record TeamResponse(
        String id,
        String projectId,
        String slug,
        String name,
        String description,
        long memberCount,
        String createdByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
