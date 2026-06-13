package com.inpt.collaborationplatform.projects.project.service;

import com.inpt.collaborationplatform.Identity.service.IdentityAccessService;
import com.inpt.collaborationplatform.projects.project.dto.request.CreateProjectRequest;
import com.inpt.collaborationplatform.projects.project.dto.request.UpdateProjectMemberRoleRequest;
import com.inpt.collaborationplatform.projects.project.dto.request.UpdateProjectRequest;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectMemberResponse;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectResponse;
import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.project.entity.ProjectStatus;
import com.inpt.collaborationplatform.projects.project.mapper.ProjectMapper;
import com.inpt.collaborationplatform.projects.project.repository.ProjectMemberRepository;
import com.inpt.collaborationplatform.projects.project.repository.ProjectRepository;
import com.inpt.collaborationplatform.projects.shared.SlugGenerator;
import com.inpt.collaborationplatform.projects.team.repository.TeamMemberRepository;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final ProjectLookupService projectLookupService;
    private final ProjectMapper projectMapper;
    private final SlugGenerator slugGenerator;
    private final IdentityAccessService identityAccessService;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String currentUserId) {
        String name = request.getName().trim();
        Project project = Project.builder()
                .name(name)
                .slug(slugGenerator.uniqueSlug(name, projectRepository::existsBySlug))
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

        return projectMapper.toProjectResponse(savedProject, ProjectRole.OWNER);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> listMyProjects(String currentUserId, Pageable pageable) {
        return PageResponse.from(projectMemberRepository.findByUserId(currentUserId, pageable)
                .map((member) -> projectMapper.toProjectResponse(member.getProject(), member.getRole())));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(String projectRef, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        ProjectMember member = projectAccessService.requireViewer(project, currentUserId);
        return projectMapper.toProjectResponse(project, member.getRole());
    }

    @Transactional
    public ProjectResponse updateProject(String projectRef, UpdateProjectRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
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

        return projectMapper.toProjectResponse(projectRepository.save(project), member.getRole());
    }

    @Transactional
    public ProjectResponse archiveProject(String projectRef, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        ProjectMember member = projectAccessService.requireOwner(project, currentUserId);

        if (project.getStatus() != ProjectStatus.ARCHIVED) {
            project.setStatus(ProjectStatus.ARCHIVED);
            project.setArchivedAt(LocalDateTime.now());
        }

        return projectMapper.toProjectResponse(projectRepository.save(project), member.getRole());
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectMemberResponse> listMembers(String projectRef, String currentUserId, Pageable pageable) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);

        return PageResponse.from(projectMemberRepository.findByProject_Id(project.getId(), pageable)
                .map((member) -> projectMapper.toMemberResponse(
                        member,
                        identityAccessService.requireUserUsername(member.getUserId()),
                        identityAccessService.requireUserEmail(member.getUserId())
                )));
    }

    @Transactional
    public ProjectMemberResponse updateMemberRole(
            String projectRef,
            String memberUserId,
            UpdateProjectMemberRoleRequest request,
            String currentUserId
    ) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireOwner(project, currentUserId);

        ProjectMember member = projectLookupService.requireProjectMember(project.getId(), memberUserId);
        if (member.getRole() == ProjectRole.OWNER || request.getRole() == ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner role changes are not supported yet");
        }

        member.setRole(request.getRole());
        return projectMapper.toMemberResponse(
                projectMemberRepository.save(member),
                identityAccessService.requireUserUsername(member.getUserId()),
                identityAccessService.requireUserEmail(member.getUserId())
        );
    }

    @Transactional
    public void removeMember(String projectRef, String memberUserId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireOwner(project, currentUserId);

        ProjectMember member = projectLookupService.requireProjectMember(project.getId(), memberUserId);
        if (member.getRole() == ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project owner cannot be removed");
        }

        teamMemberRepository.deleteByTeam_Project_IdAndUserId(project.getId(), memberUserId);
        projectMemberRepository.delete(member);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

}
