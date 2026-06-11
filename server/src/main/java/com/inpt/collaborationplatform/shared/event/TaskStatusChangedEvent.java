package com.inpt.collaborationplatform.shared.event;

public record TaskStatusChangedEvent(
        String taskId,
        String taskTitle,
        String projectId,
        String teamId,
        String oldStatus,
        String newStatus,
        String assigneeId,
        String triggeredByUserId
) {
}
