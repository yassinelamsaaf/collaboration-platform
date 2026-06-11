package com.inpt.collaborationplatform.projects.team.mapper;

import com.inpt.collaborationplatform.projects.team.dto.response.TeamMemberResponse;
import com.inpt.collaborationplatform.projects.team.dto.response.TeamResponse;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.entity.TeamMember;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    public TeamResponse toTeamResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getProject().getId(),
                team.getSlug(),
                team.getName(),
                team.getDescription(),
                team.getCreatedByUserId(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }

    public TeamMemberResponse toTeamMemberResponse(TeamMember member) {
        return new TeamMemberResponse(
                member.getId(),
                member.getTeam().getId(),
                member.getUserId(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
