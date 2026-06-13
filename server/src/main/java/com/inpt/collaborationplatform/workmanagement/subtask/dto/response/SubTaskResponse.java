package com.inpt.collaborationplatform.workmanagement.subtask.dto.response;

import com.inpt.collaborationplatform.workmanagement.task.entity.TaskStatus;

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
