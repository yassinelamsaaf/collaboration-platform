package com.inpt.collaborationplatform.workmanagement.subtask.dto.request;

import com.inpt.collaborationplatform.workmanagement.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSubTaskRequest {
    @NotBlank(message = "Sub-task title is required")
    @Size(max = 255, message = "Sub-task title must be at most 255 characters")
    private String title;

    private String assigneeId;

    private TaskStatus status;
}
