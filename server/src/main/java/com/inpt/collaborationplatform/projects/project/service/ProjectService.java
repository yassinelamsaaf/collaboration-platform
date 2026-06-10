package com.inpt.collaborationplatform.projects.project.service;

import com.inpt.collaborationplatform.projects.project.dto.request.CreateProjectRequest;
import com.inpt.collaborationplatform.projects.project.dto.request.UpdateProjectMemberRoleRequest;
import com.inpt.collaborationplatform.projects.project.dto.request.UpdateProjectRequest;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectMemberResponse;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectResponse;
import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.project.entity.ProjectStatus;
import com.inpt.collaborationplatform.projects.project.repository.ProjectMemberRepository;
import com.inpt.collaborationplatform.projects.project.repository.ProjectRepository;
import com.inpt.collaborationplatform.projects.team.repository.TeamMemberRepository;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectAccessService projectAccessService;

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
        ProjectMember member = projectAccessService.requireViewer(project, currentUserId);
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
        ProjectMember member = projectAccessService.requireOwner(project, currentUserId);

        if (project.getStatus() != ProjectStatus.ARCHIVED) {
            project.setStatus(ProjectStatus.ARCHIVED);
            project.setArchivedAt(LocalDateTime.now());
        }

        return toProjectResponse(projectRepository.save(project), member.getRole());
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(String projectId, String currentUserId) {
        Project project = requireProject(projectId);
        projectAccessService.requireViewer(project, currentUserId);

        return projectMemberRepository.findByProject_IdOrderByJoinedAtAsc(projectId)
                .stream()
                .map(this::toMemberResponse)
                .toList();
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

        teamMemberRepository.deleteByTeam_Project_IdAndUserId(projectId, memberUserId);
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

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
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

}
