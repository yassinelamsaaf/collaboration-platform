package com.inpt.collaborationplatform.projects.project.controller;

import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.projects.project.dto.request.CreateProjectRequest;
import com.inpt.collaborationplatform.projects.project.dto.request.UpdateProjectMemberRoleRequest;
import com.inpt.collaborationplatform.projects.project.dto.request.UpdateProjectRequest;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectMemberResponse;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectResponse;
import com.inpt.collaborationplatform.projects.project.service.ProjectService;
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
    public ResponseEntity<PageResponse<ProjectMemberResponse>> listMembers(
            @PathVariable String projectId,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(projectService.listMembers(projectId, currentUserId(authentication), pageable));
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
