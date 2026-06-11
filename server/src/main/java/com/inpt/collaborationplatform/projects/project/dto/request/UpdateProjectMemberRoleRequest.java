package com.inpt.collaborationplatform.projects.project.dto.request;

import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProjectMemberRoleRequest {
    @NotNull(message = "Project role is required")
    private ProjectRole role;
}
