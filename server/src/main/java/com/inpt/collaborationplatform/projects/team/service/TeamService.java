package com.inpt.collaborationplatform.projects.team.service;

import com.inpt.collaborationplatform.Identity.service.IdentityAccessService;
import com.inpt.collaborationplatform.projects.team.dto.request.AddTeamMemberRequest;
import com.inpt.collaborationplatform.projects.team.dto.request.CreateTeamRequest;
import com.inpt.collaborationplatform.projects.team.dto.request.UpdateTeamMemberRoleRequest;
import com.inpt.collaborationplatform.projects.team.dto.request.UpdateTeamRequest;
import com.inpt.collaborationplatform.projects.team.dto.response.TeamMemberResponse;
import com.inpt.collaborationplatform.projects.team.dto.response.TeamResponse;
import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.entity.TeamMember;
import com.inpt.collaborationplatform.projects.project.repository.ProjectMemberRepository;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.projects.team.mapper.TeamMapper;
import com.inpt.collaborationplatform.projects.team.repository.TeamMemberRepository;
import com.inpt.collaborationplatform.projects.team.repository.TeamRepository;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final ProjectMemberRepository projectMemberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final ProjectLookupService projectLookupService;
    private final IdentityAccessService identityAccessService;
    private final TeamMapper teamMapper;

    @Transactional
    public TeamResponse createTeam(String projectId, CreateTeamRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);

        String name = normalizeRequiredName(request.getName(), "Team name cannot be blank");
        String normalizedName = normalizeNameKey(name);
        if (teamRepository.existsByProject_IdAndNormalizedName(projectId, normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A team with this name already exists in the project");
        }

        Team team = teamRepository.save(Team.builder()
                .project(project)
                .name(name)
                .normalizedName(normalizedName)
                .description(normalizeOptionalText(request.getDescription()))
                .createdByUserId(currentUserId)
                .build());

        return teamMapper.toTeamResponse(team);
    }

    @Transactional(readOnly = true)
    public PageResponse<TeamResponse> listTeams(String projectId, String currentUserId, Pageable pageable) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireViewer(project, currentUserId);

        return PageResponse.from(teamRepository.findByProject_Id(projectId, pageable)
                .map(teamMapper::toTeamResponse));
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeam(String projectId, String teamId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireViewer(project, currentUserId);
        return teamMapper.toTeamResponse(requireTeam(projectId, teamId));
    }

    @Transactional
    public TeamResponse updateTeam(String projectId, String teamId, UpdateTeamRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);

        Team team = requireTeam(projectId, teamId);
        if (request.getName() != null) {
            String name = normalizeRequiredName(request.getName(), "Team name cannot be blank");
            String normalizedName = normalizeNameKey(name);
            if (!normalizedName.equals(team.getNormalizedName())
                    && teamRepository.existsByProject_IdAndNormalizedName(projectId, normalizedName)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A team with this name already exists in the project");
            }
            team.setName(name);
            team.setNormalizedName(normalizedName);
        }

        if (request.getDescription() != null) {
            team.setDescription(normalizeOptionalText(request.getDescription()));
        }

        return teamMapper.toTeamResponse(teamRepository.save(team));
    }

    @Transactional
    public void deleteTeam(String projectId, String teamId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);
        Team team = requireTeam(projectId, teamId);
        teamMemberRepository.deleteByTeam_Id(team.getId());
        teamRepository.delete(team);
    }

    @Transactional(readOnly = true)
    public PageResponse<TeamMemberResponse> listTeamMembers(
            String projectId,
            String teamId,
            String currentUserId,
            Pageable pageable
    ) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = requireTeam(projectId, teamId);

        return PageResponse.from(teamMemberRepository.findByTeam_Id(team.getId(), pageable)
                .map(teamMapper::toTeamMemberResponse));
    }

    @Transactional
    public TeamMemberResponse addTeamMember(
            String projectId,
            String teamId,
            AddTeamMemberRequest request,
            String currentUserId
    ) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);
        Team team = requireTeam(projectId, teamId);

        identityAccessService.requireUserExists(request.getUserId());
        ProjectMember projectMember = projectMemberRepository.findByProject_IdAndUserId(projectId, request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must be a project member before joining a team"));
        if (projectMember.getRole() == ProjectRole.VIEWER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project viewers cannot join teams");
        }
        if (teamMemberRepository.existsByTeam_IdAndUserId(teamId, request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of this team");
        }

        TeamMember member = teamMemberRepository.save(TeamMember.builder()
                .team(team)
                .userId(request.getUserId())
                .role(request.getRole())
                .build());

        return teamMapper.toTeamMemberResponse(member);
    }

    @Transactional
    public TeamMemberResponse updateTeamMemberRole(
            String projectId,
            String teamId,
            String memberUserId,
            UpdateTeamMemberRoleRequest request,
            String currentUserId
    ) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);
        requireTeam(projectId, teamId);

        TeamMember member = requireTeamMember(teamId, memberUserId);
        member.setRole(request.getRole());
        return teamMapper.toTeamMemberResponse(teamMemberRepository.save(member));
    }

    @Transactional
    public void removeTeamMember(String projectId, String teamId, String memberUserId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectId);
        projectAccessService.requireManager(project, currentUserId);
        requireTeam(projectId, teamId);
        teamMemberRepository.delete(requireTeamMember(teamId, memberUserId));
    }

    private Team requireTeam(String projectId, String teamId) {
        return teamRepository.findByIdAndProject_Id(teamId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    private TeamMember requireTeamMember(String teamId, String userId) {
        return teamMemberRepository.findByTeam_IdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found"));
    }

    private String normalizeRequiredName(String value, String errorMessage) {
        String normalized = value.trim();
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeNameKey(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

}
