package com.inpt.collaborationplatform.projects.controller;

import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.projects.dto.request.CreateProjectRequest;
import com.inpt.collaborationplatform.projects.dto.request.CreateProjectInvitationRequest;
import com.inpt.collaborationplatform.projects.dto.request.UpdateProjectMemberRoleRequest;
import com.inpt.collaborationplatform.projects.dto.request.UpdateProjectRequest;
import com.inpt.collaborationplatform.projects.dto.response.ProjectInvitationPreviewResponse;
import com.inpt.collaborationplatform.projects.dto.response.ProjectInvitationResponse;
import com.inpt.collaborationplatform.projects.dto.response.ProjectMemberResponse;
import com.inpt.collaborationplatform.projects.dto.response.ProjectResponse;
import com.inpt.collaborationplatform.projects.service.ProjectService;
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

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request, currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProjectResponse>> listMyProjects(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(projectService.listMyProjects(currentUserId(authentication), pageable));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.getProject(projectId, currentUserId(authentication)));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request, currentUserId(authentication)));
    }

    @PostMapping("/{projectId}/archive")
    public ResponseEntity<ProjectResponse> archiveProject(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.archiveProject(projectId, currentUserId(authentication)));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> listMembers(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.listMembers(projectId, currentUserId(authentication)));
    }

    @PostMapping("/{projectId}/invitations")
    public ResponseEntity<ProjectInvitationResponse> inviteMember(
            @PathVariable String projectId,
            @Valid @RequestBody CreateProjectInvitationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.inviteMember(projectId, request, currentUserId(authentication)));
    }

    @GetMapping("/{projectId}/invitations")
    public ResponseEntity<List<ProjectInvitationResponse>> listInvitations(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.listInvitations(projectId, currentUserId(authentication)));
    }

    @GetMapping("/invitations/{token}")
    public ResponseEntity<ProjectInvitationPreviewResponse> previewInvitation(@PathVariable String token) {
        return ResponseEntity.ok(projectService.previewInvitation(token));
    }

    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<ProjectMemberResponse> acceptInvitation(
            @PathVariable String token,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.acceptInvitation(token, currentUserId(authentication)));
    }

    @PostMapping("/{projectId}/invitations/{invitationId}/cancel")
    public ResponseEntity<MessageResponse> cancelInvitation(
            @PathVariable String projectId,
            @PathVariable String invitationId,
            Authentication authentication
    ) {
        projectService.cancelInvitation(projectId, invitationId, currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Project invitation cancelled successfully"));
    }

    @PatchMapping("/{projectId}/members/{memberUserId}/role")
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @PathVariable String projectId,
            @PathVariable String memberUserId,
            @Valid @RequestBody UpdateProjectMemberRoleRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(projectService.updateMemberRole(
                projectId,
                memberUserId,
                request,
                currentUserId(authentication)
        ));
    }

    @DeleteMapping("/{projectId}/members/{memberUserId}")
    public ResponseEntity<MessageResponse> removeMember(
            @PathVariable String projectId,
            @PathVariable String memberUserId,
            Authentication authentication
    ) {
        projectService.removeMember(projectId, memberUserId, currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Project member removed successfully"));
    }

    private String currentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
