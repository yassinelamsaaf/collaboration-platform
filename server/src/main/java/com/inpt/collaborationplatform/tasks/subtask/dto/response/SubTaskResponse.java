package com.inpt.collaborationplatform.tasks.subtask.dto.response;

import com.inpt.collaborationplatform.tasks.task.entity.TaskStatus;

import java.time.LocalDateTime;

public record SubTaskResponse(
        String id,
        String taskId,
        String assigneeId,
        String title,
        boolean isDone,
        TaskStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
