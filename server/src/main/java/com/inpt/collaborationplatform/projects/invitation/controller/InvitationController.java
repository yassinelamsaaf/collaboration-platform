package com.inpt.collaborationplatform.projects.invitation.controller;

import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.projects.invitation.dto.request.CreateProjectInvitationRequest;
import com.inpt.collaborationplatform.projects.invitation.dto.response.ProjectInvitationPreviewResponse;
import com.inpt.collaborationplatform.projects.invitation.dto.response.ProjectInvitationResponse;
import com.inpt.collaborationplatform.projects.invitation.service.InvitationService;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectMemberResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/{projectId}/invitations")
    public ResponseEntity<ProjectInvitationResponse> inviteMember(
            @PathVariable String projectId,
            @Valid @RequestBody CreateProjectInvitationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.inviteMember(projectId, request, currentUserId(authentication)));
    }

    @GetMapping("/{projectId}/invitations")
    public ResponseEntity<PageResponse<ProjectInvitationResponse>> listInvitations(
            @PathVariable String projectId,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(invitationService.listInvitations(projectId, currentUserId(authentication), pageable));
    }

    @GetMapping("/invitations/{token}")
    public ResponseEntity<ProjectInvitationPreviewResponse> previewInvitation(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.previewInvitation(token));
    }

    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<ProjectMemberResponse> acceptInvitation(
            @PathVariable String token,
            Authentication authentication
    ) {
        return ResponseEntity.ok(invitationService.acceptInvitation(token, currentUserId(authentication)));
    }

    @PostMapping("/{projectId}/invitations/{invitationId}/cancel")
    public ResponseEntity<MessageResponse> cancelInvitation(
            @PathVariable String projectId,
            @PathVariable String invitationId,
            Authentication authentication
    ) {
        invitationService.cancelInvitation(projectId, invitationId, currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Project invitation cancelled successfully"));
    }

    private String currentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
