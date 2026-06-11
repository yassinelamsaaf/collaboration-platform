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
@RequestMapping("/api/projects/{projectRef}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @PathVariable String projectRef,
            @Valid @RequestBody CreateTeamRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.createTeam(projectRef, request, currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TeamResponse>> listTeams(
            @PathVariable String projectRef,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(teamService.listTeams(projectRef, currentUserId(authentication), pageable));
    }

    @GetMapping("/{teamRef}")
    public ResponseEntity<TeamResponse> getTeam(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teamService.getTeam(projectRef, teamRef, currentUserId(authentication)));
    }

    @PatchMapping("/{teamRef}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @Valid @RequestBody UpdateTeamRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teamService.updateTeam(projectRef, teamRef, request, currentUserId(authentication)));
    }

    @DeleteMapping("/{teamRef}")
    public ResponseEntity<MessageResponse> deleteTeam(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            Authentication authentication
    ) {
        teamService.deleteTeam(projectRef, teamRef, currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Team deleted successfully"));
    }

    @GetMapping("/{teamRef}/members")
    public ResponseEntity<PageResponse<TeamMemberResponse>> listTeamMembers(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(teamService.listTeamMembers(projectRef, teamRef, currentUserId(authentication), pageable));
    }

    @PostMapping("/{teamRef}/members")
    public ResponseEntity<TeamMemberResponse> addTeamMember(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @Valid @RequestBody AddTeamMemberRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.addTeamMember(projectRef, teamRef, request, currentUserId(authentication)));
    }

    @PatchMapping("/{teamRef}/members/{memberUserId}/role")
    public ResponseEntity<TeamMemberResponse> updateTeamMemberRole(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String memberUserId,
            @Valid @RequestBody UpdateTeamMemberRoleRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teamService.updateTeamMemberRole(
                projectRef,
                teamRef,
                memberUserId,
                request,
                currentUserId(authentication)
        ));
    }

    @DeleteMapping("/{teamRef}/members/{memberUserId}")
    public ResponseEntity<MessageResponse> removeTeamMember(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String memberUserId,
            Authentication authentication
    ) {
        teamService.removeTeamMember(projectRef, teamRef, memberUserId, currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Team member removed successfully"));
    }

    private String currentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
