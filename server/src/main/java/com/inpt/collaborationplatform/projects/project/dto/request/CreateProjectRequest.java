package com.inpt.collaborationplatform.projects.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 120, message = "Project name must be at most 120 characters")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
}
