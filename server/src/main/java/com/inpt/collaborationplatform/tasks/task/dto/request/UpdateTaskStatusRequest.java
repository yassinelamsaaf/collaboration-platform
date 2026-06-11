package com.inpt.collaborationplatform.tasks.task.dto.request;

import com.inpt.collaborationplatform.tasks.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTaskStatusRequest {
    @NotNull(message = "Status is required")
    private TaskStatus status;
}
