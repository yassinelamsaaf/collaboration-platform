package com.inpt.collaborationplatform.projects.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 120, message = "Team name must be at most 120 characters")
    private String name;

    @Size(max = 1000, message = "Team description must be at most 1000 characters")
    private String description;
}
