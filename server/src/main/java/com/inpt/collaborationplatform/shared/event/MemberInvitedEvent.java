package com.inpt.collaborationplatform.shared.event;

public record MemberInvitedEvent(
        String invitationId,
        String invitedEmail,
        String projectId,
        String projectName,
        String triggeredByUserId
) {
}
