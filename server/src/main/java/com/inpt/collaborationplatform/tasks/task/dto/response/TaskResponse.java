package com.inpt.collaborationplatform.tasks.task.dto.response;

import com.inpt.collaborationplatform.tasks.task.entity.Priority;
import com.inpt.collaborationplatform.tasks.task.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        String id,
        String projectId,
        String teamId,
        String assigneeId,
        String title,
        String description,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate,
        String createdByUserId,
        int subTaskCount,
        int completedSubTaskCount,
        int commentCount,
        int attachmentCount,
        int totalTimeMinutes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String assigneeName
) {
}
