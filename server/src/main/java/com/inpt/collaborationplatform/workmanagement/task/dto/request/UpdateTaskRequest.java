package com.inpt.collaborationplatform.workmanagement.task.dto.request;

import com.inpt.collaborationplatform.workmanagement.task.entity.Priority;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {
    @Size(max = 255, message = "Task title must be at most 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;

    private Priority priority;

    private LocalDate dueDate;

    private String assigneeId;
}
