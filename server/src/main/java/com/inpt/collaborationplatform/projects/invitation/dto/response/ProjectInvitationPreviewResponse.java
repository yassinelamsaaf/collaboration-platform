package com.inpt.collaborationplatform.projects.invitation.dto.response;

import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitationStatus;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;

import java.time.LocalDateTime;

public record ProjectInvitationPreviewResponse(
        String projectId,
        String projectName,
        String email,
        ProjectRole role,
        ProjectInvitationStatus status,
        LocalDateTime expiresAt
) {
}
