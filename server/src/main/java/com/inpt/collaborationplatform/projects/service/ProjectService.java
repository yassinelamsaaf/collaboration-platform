package com.inpt.collaborationplatform.projects.service;

import com.inpt.collaborationplatform.Identity.service.EmailService;
import com.inpt.collaborationplatform.Identity.service.IdentityAccessService;
import com.inpt.collaborationplatform.projects.dto.request.CreateProjectRequest;
import com.inpt.collaborationplatform.projects.dto.request.CreateProjectInvitationRequest;
import com.inpt.collaborationplatform.projects.dto.request.UpdateProjectMemberRoleRequest;
import com.inpt.collaborationplatform.projects.dto.request.UpdateProjectRequest;
import com.inpt.collaborationplatform.projects.dto.response.ProjectInvitationPreviewResponse;
import com.inpt.collaborationplatform.projects.dto.response.ProjectInvitationResponse;
import com.inpt.collaborationplatform.projects.dto.response.ProjectMemberResponse;
import com.inpt.collaborationplatform.projects.dto.response.ProjectResponse;
import com.inpt.collaborationplatform.projects.entity.Project;
import com.inpt.collaborationplatform.projects.entity.ProjectInvitation;
import com.inpt.collaborationplatform.projects.entity.ProjectInvitationStatus;
import com.inpt.collaborationplatform.projects.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.entity.ProjectStatus;
import com.inpt.collaborationplatform.projects.repository.ProjectInvitationRepository;
import com.inpt.collaborationplatform.projects.repository.ProjectMemberRepository;
import com.inpt.collaborationplatform.projects.repository.ProjectRepository;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectAccessService projectAccessService;
    private final IdentityAccessService identityAccessService;
    private final EmailService emailService;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String currentUserId) {
        Project project = Project.builder()
                .name(request.getName().trim())
                .description(normalizeOptionalText(request.getDescription()))
                .createdByUserId(currentUserId)
                .status(ProjectStatus.ACTIVE)
                .build();

        Project savedProject = projectRepository.save(project);
        projectMemberRepository.save(ProjectMember.builder()
                .project(savedProject)
                .userId(currentUserId)
                .role(ProjectRole.OWNER)
                .build());

        return toProjectResponse(savedProject, ProjectRole.OWNER);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> listMyProjects(String currentUserId, Pageable pageable) {
        return PageResponse.from(projectMemberRepository.findByUserId(currentUserId, pageable)
                .map((member) -> toProjectResponse(member.getProject(), member.getRole())));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(String projectId, String currentUserId) {
        Project project = requireProject(projectId);
        ProjectMember member = projectAccessService.requireMember(project, currentUserId);
        return toProjectResponse(project, member.getRole());
    }

    @Transactional
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request, String currentUserId) {
        Project project = requireProject(projectId);
        ProjectMember member = projectAccessService.requireManager(project, currentUserId);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project name cannot be blank");
            }
            project.setName(name);
        }

        if (request.getDescription() != null) {
            project.setDescription(normalizeOptionalText(request.getDescription()));
        }

        return toProjectResponse(projectRepository.save(project), member.getRole());
    }

    @Transactional
    public ProjectResponse archiveProject(String projectId, String currentUserId) {
        Project project = requireProject(projectId);
        ProjectMember member = projectAccessService.requireManager(project, currentUserId);

        if (project.getStatus() != ProjectStatus.ARCHIVED) {
            project.setStatus(ProjectStatus.ARCHIVED);
            project.setArchivedAt(LocalDateTime.now());
        }

        return toProjectResponse(projectRepository.save(project), member.getRole());
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(String projectId, String currentUserId) {
        Project project = requireProject(projectId);
        projectAccessService.requireMember(project, currentUserId);

        return projectMemberRepository.findByProject_IdOrderByJoinedAtAsc(projectId)
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public ProjectInvitationResponse inviteMember(
            String projectId,
            CreateProjectInvitationRequest request,
            String currentUserId
    ) {
        Project project = requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);

        String email = normalizeEmail(request.getEmail());
        if (request.getRole() == ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner invitations are not supported yet");
        }
        identityAccessService.findUserIdByEmail(email).ifPresent((userId) -> {
            if (projectMemberRepository.existsByProject_IdAndUserId(projectId, userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a project member");
            }
        });

        String invitedByEmail = identityAccessService.requireUserEmail(currentUserId);
        projectInvitationRepository.findByProject_IdAndEmailIgnoreCaseAndStatus(
                projectId,
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

        emailService.sendProjectInvitation(email, project.getName(), invitedByEmail, invitation.getToken());
        return toInvitationResponse(invitation);
    }

    @Transactional(readOnly = true)
    public List<ProjectInvitationResponse> listInvitations(String projectId, String currentUserId) {
        Project project = requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);

        return projectInvitationRepository.findByProject_IdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toInvitationResponse)
                .toList();
    }

    @Transactional
    public ProjectInvitationPreviewResponse previewInvitation(String token) {
        ProjectInvitation invitation = requireInvitation(token);
        refreshExpiredInvitation(invitation);
        return toInvitationPreviewResponse(invitation);
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
            return toMemberResponse(requireProjectMember(project.getId(), currentUserId));
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

        return toMemberResponse(member);
    }

    @Transactional
    public void cancelInvitation(String projectId, String invitationId, String currentUserId) {
        Project project = requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);

        ProjectInvitation invitation = projectInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project invitation not found"));
        if (!invitation.getProject().getId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project invitation not found");
        }
        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending invitations can be cancelled");
        }

        invitation.setStatus(ProjectInvitationStatus.CANCELLED);
        projectInvitationRepository.save(invitation);
    }

    @Transactional
    public ProjectMemberResponse updateMemberRole(
            String projectId,
            String memberUserId,
            UpdateProjectMemberRoleRequest request,
            String currentUserId
    ) {
        Project project = requireProject(projectId);
        projectAccessService.requireOwner(project, currentUserId);

        ProjectMember member = requireProjectMember(projectId, memberUserId);
        if (member.getRole() == ProjectRole.OWNER || request.getRole() == ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner role changes are not supported yet");
        }

        member.setRole(request.getRole());
        return toMemberResponse(projectMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(String projectId, String memberUserId, String currentUserId) {
        Project project = requireProject(projectId);
        projectAccessService.requireOwner(project, currentUserId);

        ProjectMember member = requireProjectMember(projectId, memberUserId);
        if (member.getRole() == ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project owner cannot be removed");
        }

        projectMemberRepository.delete(member);
    }

    private Project requireProject(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private ProjectMember requireProjectMember(String projectId, String userId) {
        return projectMemberRepository.findByProject_IdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project member not found"));
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

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String generateInvitationToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private ProjectResponse toProjectResponse(Project project, ProjectRole currentUserRole) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedByUserId(),
                project.getStatus(),
                currentUserRole,
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getArchivedAt()
        );
    }

    private ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                member.getUserId(),
                member.getRole(),
                member.getJoinedAt()
        );
    }

    private ProjectInvitationResponse toInvitationResponse(ProjectInvitation invitation) {
        return new ProjectInvitationResponse(
                invitation.getId(),
                invitation.getProject().getId(),
                invitation.getProject().getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getInvitedByUserId(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt(),
                invitation.getAcceptedAt()
        );
    }

    private ProjectInvitationPreviewResponse toInvitationPreviewResponse(ProjectInvitation invitation) {
        return new ProjectInvitationPreviewResponse(
                invitation.getProject().getId(),
                invitation.getProject().getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getExpiresAt()
        );
    }
}
