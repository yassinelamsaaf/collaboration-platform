package com.inpt.collaborationplatform.projects.team.dto.response;

import com.inpt.collaborationplatform.projects.team.entity.TeamRole;

import java.time.LocalDateTime;

public record TeamMemberResponse(
        String id,
        String teamId,
        String userId,
        TeamRole role,
        LocalDateTime joinedAt
) {
}
