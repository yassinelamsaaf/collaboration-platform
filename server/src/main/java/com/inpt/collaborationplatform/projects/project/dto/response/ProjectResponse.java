package com.inpt.collaborationplatform.projects.project.dto.response;

import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.project.entity.ProjectStatus;

import java.time.LocalDateTime;

// (nfss lblan hna)
public record ProjectResponse(
        String id,
        String name,
        String description,
        String createdByUserId,
        ProjectStatus status,
        ProjectRole currentUserRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime archivedAt
) {
}
