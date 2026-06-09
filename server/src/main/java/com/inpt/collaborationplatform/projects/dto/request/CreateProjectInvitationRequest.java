package com.inpt.collaborationplatform.projects.dto.request;

import com.inpt.collaborationplatform.projects.entity.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProjectInvitationRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Project role is required")
    private ProjectRole role;
}
