package com.inpt.collaborationplatform.shared.event;

public record CommentAddedEvent(
        String commentId,
        String commentContent,
        String taskId,
        String taskTitle,
        String projectId,
        String teamId,
        String taskAssigneeId,
        String triggeredByUserId
) {
}
