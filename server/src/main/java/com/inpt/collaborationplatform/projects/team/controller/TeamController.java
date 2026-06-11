package com.inpt.collaborationplatform.projects.team.controller;

import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.projects.team.dto.request.AddTeamMemberRequest;
import com.inpt.collaborationplatform.projects.team.dto.request.CreateTeamRequest;
import com.inpt.collaborationplatform.projects.team.dto.request.UpdateTeamMemberRoleRequest;
import com.inpt.collaborationplatform.projects.team.dto.request.UpdateTeamRequest;
import com.inpt.collaborationplatform.projects.team.dto.response.TeamMemberResponse;
import com.inpt.collaborationplatform.projects.team.dto.response.TeamResponse;
import com.inpt.collaborationplatform.projects.team.service.TeamService;
import com.inpt.collaborationplatform.shared.dto.MessageResponse;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/projects/{projectId}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @PathVariable String projectId,
            @Valid @RequestBody CreateTeamRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.createTeam(projectId, request, currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TeamResponse>> listTeams(
            @PathVariable String projectId,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(teamService.listTeams(projectId, currentUserId(authentication), pageable));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(
            @PathVariable String projectId,
            @PathVariable String teamId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teamService.getTeam(projectId, teamId, currentUserId(authentication)));
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable String projectId,
            @PathVariable String teamId,
            @Valid @RequestBody UpdateTeamRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teamService.updateTeam(projectId, teamId, request, currentUserId(authentication)));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<MessageResponse> deleteTeam(
            @PathVariable String projectId,
            @PathVariable String teamId,
            Authentication authentication
    ) {
        teamService.deleteTeam(projectId, teamId, currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Team deleted successfully"));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<PageResponse<TeamMemberResponse>> listTeamMembers(
            @PathVariable String projectId,
            @PathVariable String teamId,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(teamService.listTeamMembers(projectId, teamId, currentUserId(authentication), pageable));
    }

    @PostMapping("/{teamId}/members")
    public ResponseEntity<TeamMemberResponse> addTeamMember(
            @PathVariable String projectId,
            @PathVariable String teamId,
            @Valid @RequestBody AddTeamMemberRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.addTeamMember(projectId, teamId, request, currentUserId(authentication)));
    }

    @PatchMapping("/{teamId}/members/{memberUserId}/role")
    public ResponseEntity<TeamMemberResponse> updateTeamMemberRole(
            @PathVariable String projectId,
            @PathVariable String teamId,
            @PathVariable String memberUserId,
            @Valid @RequestBody UpdateTeamMemberRoleRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teamService.updateTeamMemberRole(
                projectId,
                teamId,
                memberUserId,
                request,
                currentUserId(authentication)
        ));
    }

    @DeleteMapping("/{teamId}/members/{memberUserId}")
    public ResponseEntity<MessageResponse> removeTeamMember(
            @PathVariable String projectId,
            @PathVariable String teamId,
            @PathVariable String memberUserId,
            Authentication authentication
    ) {
        teamService.removeTeamMember(projectId, teamId, memberUserId, currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Team member removed successfully"));
    }

    private String currentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
