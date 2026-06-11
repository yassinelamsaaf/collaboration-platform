package com.inpt.collaborationplatform.tasks.task.mapper;

import com.inpt.collaborationplatform.tasks.task.dto.response.TaskResponse;
import com.inpt.collaborationplatform.tasks.task.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toTaskResponse(Task task, int subTaskCount, int completedSubTaskCount,
                                       int commentCount, int attachmentCount, int totalTimeMinutes) {
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getTeam().getId(),
                task.getAssigneeId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedByUserId(),
                subTaskCount,
                completedSubTaskCount,
                commentCount,
                attachmentCount,
                totalTimeMinutes,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
