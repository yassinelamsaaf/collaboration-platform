package com.inpt.collaborationplatform.projects.team.service;

import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.entity.TeamMember;
import com.inpt.collaborationplatform.projects.team.entity.TeamRole;
import com.inpt.collaborationplatform.projects.team.repository.TeamMemberRepository;
import com.inpt.collaborationplatform.projects.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeamLookupService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public Team requireTeam(String projectId, String teamRef) {
        return teamRepository.findByIdAndProject_Id(teamRef, projectId)
                .or(() -> teamRepository.findBySlugAndProject_Id(teamRef, projectId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    public TeamMember requireTeamMember(String teamId, String teamMemberId) {
        return teamMemberRepository.findById(teamMemberId)
                .filter(tm -> tm.getTeam().getId().equals(teamId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found in this team"));
    }

    public TeamMember requireTeamMemberByUser(String teamId, String userId) {
        return teamMemberRepository.findByTeam_IdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found"));
    }

    public TeamMember requireTeamLeader(String teamId, String userId) {
        return teamMemberRepository.findByTeam_IdAndUserId(teamId, userId)
                .filter(tm -> tm.getRole() == TeamRole.LEADER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the team leader can perform this action"));
    }
}
