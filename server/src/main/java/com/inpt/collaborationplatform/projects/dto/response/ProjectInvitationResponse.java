package com.inpt.collaborationplatform.projects.dto.response;

import com.inpt.collaborationplatform.projects.entity.ProjectInvitationStatus;
import com.inpt.collaborationplatform.projects.entity.ProjectRole;

import java.time.LocalDateTime;

public record ProjectInvitationResponse(
        String id,
        String projectId,
        String projectName,
        String email,
        ProjectRole role,
        ProjectInvitationStatus status,
        String invitedByUserId,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt
) {
}
