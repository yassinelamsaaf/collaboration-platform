package com.inpt.collaborationplatform.projects.invitation.mapper;

import com.inpt.collaborationplatform.projects.invitation.dto.response.ProjectInvitationPreviewResponse;
import com.inpt.collaborationplatform.projects.invitation.dto.response.ProjectInvitationResponse;
import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitation;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public ProjectInvitationResponse toInvitationResponse(ProjectInvitation invitation) {
        return new ProjectInvitationResponse(
                invitation.getId(),
                invitation.getProject().getId(),
                invitation.getProject().getSlug(),
                invitation.getProject().getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getInvitedByUserId(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt(),
                invitation.getAcceptedAt()
        );
    }

    public ProjectInvitationPreviewResponse toInvitationPreviewResponse(ProjectInvitation invitation) {
        return new ProjectInvitationPreviewResponse(
                invitation.getProject().getId(),
                invitation.getProject().getSlug(),
                invitation.getProject().getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getExpiresAt()
        );
    }
}
