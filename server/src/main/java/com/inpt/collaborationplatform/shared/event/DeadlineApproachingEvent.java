package com.inpt.collaborationplatform.shared.event;

public record DeadlineApproachingEvent(
        String taskId,
        String taskTitle,
        String projectId,
        String teamId,
        String assigneeId
) {
}
