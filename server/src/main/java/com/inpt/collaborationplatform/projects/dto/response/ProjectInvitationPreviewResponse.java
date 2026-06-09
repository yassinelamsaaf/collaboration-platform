package com.inpt.collaborationplatform.projects.dto.response;

import com.inpt.collaborationplatform.projects.entity.ProjectInvitationStatus;
import com.inpt.collaborationplatform.projects.entity.ProjectRole;

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
