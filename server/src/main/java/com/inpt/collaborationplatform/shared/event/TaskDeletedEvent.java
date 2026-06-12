package com.inpt.collaborationplatform.shared.event;

public record TaskDeletedEvent(
        String taskId,
        String taskTitle,
        String projectId,
        String teamId,
        String triggeredByUserId
) {
}
