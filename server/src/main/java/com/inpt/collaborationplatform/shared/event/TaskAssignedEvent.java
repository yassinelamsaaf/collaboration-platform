package com.inpt.collaborationplatform.shared.event;

public record TaskAssignedEvent(
        String taskId,
        String taskTitle,
        String projectId,
        String teamId,
        String oldAssigneeId,
        String newAssigneeId,
        String triggeredByUserId
) {
}
