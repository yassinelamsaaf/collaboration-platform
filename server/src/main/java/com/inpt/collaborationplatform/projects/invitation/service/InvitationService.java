package com.inpt.collaborationplatform.projects.invitation.service;

import com.inpt.collaborationplatform.Identity.service.EmailService;
import com.inpt.collaborationplatform.Identity.service.IdentityAccessService;
import com.inpt.collaborationplatform.projects.invitation.dto.request.CreateProjectInvitationRequest;
import com.inpt.collaborationplatform.projects.invitation.dto.response.ProjectInvitationPreviewResponse;
import com.inpt.collaborationplatform.projects.invitation.dto.response.ProjectInvitationResponse;
import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitation;
import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitationStatus;
import com.inpt.collaborationplatform.projects.invitation.mapper.InvitationMapper;
import com.inpt.collaborationplatform.projects.invitation.repository.ProjectInvitationRepository;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectMemberResponse;
import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.project.mapper.ProjectMapper;
import com.inpt.collaborationplatform.projects.project.repository.ProjectMemberRepository;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectAccessService projectAccessService;
    private final ProjectLookupService projectLookupService;
    private final IdentityAccessService identityAccessService;
    private final EmailService emailService;
    private final ProjectMapper projectMapper;
    private final InvitationMapper invitationMapper;

    @Transactional
    public ProjectInvitationResponse inviteMember(
            String projectRef,
            CreateProjectInvitationRequest request,
            String currentUserId
    ) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireManager(project, currentUserId);

        String email = normalizeEmail(request.getEmail());
        if (request.getRole() == ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner invitations are not supported");
        }
        identityAccessService.findUserIdByEmail(email).ifPresent((userId) -> {
            if (projectMemberRepository.existsByProject_IdAndUserId(project.getId(), userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a project member");
            }
        });

        String invitedByEmail = identityAccessService.requireUserEmail(currentUserId);
        projectInvitationRepository.findByProject_IdAndEmailIgnoreCaseAndStatus(
                project.getId(),
                email,
                ProjectInvitationStatus.PENDING
        ).ifPresent((invitation) -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending invitation already exists for this email");
        });

        ProjectInvitation invitation = projectInvitationRepository.save(ProjectInvitation.builder()
                .project(project)
                .email(email)
                .role(request.getRole())
                .token(generateInvitationToken())
                .status(ProjectInvitationStatus.PENDING)
                .invitedByUserId(currentUserId)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        sendInvitationEmailAfterCommit(email, project.getName(), invitedByEmail, invitation.getToken());
        return invitationMapper.toInvitationResponse(invitation);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectInvitationResponse> listInvitations(String projectRef, String currentUserId, Pageable pageable) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireManager(project, currentUserId);

        return PageResponse.from(projectInvitationRepository.findByProject_Id(project.getId(), pageable)
                .map(invitationMapper::toInvitationResponse));
    }

    @Transactional
    public ProjectInvitationPreviewResponse previewInvitation(String token) {
        ProjectInvitation invitation = requireInvitation(token);
        refreshExpiredInvitation(invitation);
        return invitationMapper.toInvitationPreviewResponse(invitation);
    }

    @Transactional
    public ProjectMemberResponse acceptInvitation(String token, String currentUserId) {
        ProjectInvitation invitation = requireInvitation(token);
        refreshExpiredInvitation(invitation);

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation is not pending");
        }

        String currentUserEmail = identityAccessService.requireUserEmail(currentUserId);
        if (!invitation.getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This invitation belongs to another email");
        }

        Project project = invitation.getProject();
        if (projectMemberRepository.existsByProject_IdAndUserId(project.getId(), currentUserId)) {
            invitation.setStatus(ProjectInvitationStatus.ACCEPTED);
            invitation.setAcceptedByUserId(currentUserId);
            invitation.setAcceptedAt(LocalDateTime.now());
            projectInvitationRepository.save(invitation);
            return projectMapper.toMemberResponse(projectLookupService.requireProjectMember(project.getId(), currentUserId));
        }

        ProjectMember member = projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .userId(currentUserId)
                .role(invitation.getRole())
                .build());

        invitation.setStatus(ProjectInvitationStatus.ACCEPTED);
        invitation.setAcceptedByUserId(currentUserId);
        invitation.setAcceptedAt(LocalDateTime.now());
        projectInvitationRepository.save(invitation);

        return projectMapper.toMemberResponse(member);
    }

    @Transactional
    public void cancelInvitation(String projectRef, String invitationId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireManager(project, currentUserId);

        ProjectInvitation invitation = projectInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project invitation not found"));
        if (!invitation.getProject().getId().equals(project.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project invitation not found");
        }
        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending invitations can be cancelled");
        }

        invitation.setStatus(ProjectInvitationStatus.CANCELLED);
        projectInvitationRepository.save(invitation);
    }

    private ProjectInvitation requireInvitation(String token) {
        return projectInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project invitation not found"));
    }

    private void refreshExpiredInvitation(ProjectInvitation invitation) {
        if (invitation.getStatus() == ProjectInvitationStatus.PENDING
                && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(ProjectInvitationStatus.EXPIRED);
            projectInvitationRepository.save(invitation);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String generateInvitationToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private void sendInvitationEmailAfterCommit(String email, String projectName, String invitedByEmail, String token) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            emailService.sendProjectInvitation(email, projectName, invitedByEmail, token);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    emailService.sendProjectInvitation(email, projectName, invitedByEmail, token);
                } catch (RuntimeException e) {
                    log.error("Project invitation was saved but email delivery failed for {}", email, e);
                }
            }
        });
    }

    
}
