package com.inpt.collaborationplatform.projects.team.dto.request;

import com.inpt.collaborationplatform.projects.team.entity.TeamRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTeamMemberRoleRequest {

    @NotNull(message = "Team role is required")
    private TeamRole role;
}
