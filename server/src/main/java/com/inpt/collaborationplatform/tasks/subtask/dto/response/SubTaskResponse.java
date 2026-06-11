package com.inpt.collaborationplatform.tasks.subtask.dto.response;

import java.time.LocalDateTime;

public record SubTaskResponse(
        String id,
        String taskId,
        String assigneeId,
        String title,
        boolean isDone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
