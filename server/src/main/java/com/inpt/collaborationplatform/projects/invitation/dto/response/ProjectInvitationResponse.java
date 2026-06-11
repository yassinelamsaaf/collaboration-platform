package com.inpt.collaborationplatform.projects.invitation.dto.response;

import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitationStatus;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;

import java.time.LocalDateTime;

public record ProjectInvitationResponse(
        String id,
        String projectId,
        String projectSlug,
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
