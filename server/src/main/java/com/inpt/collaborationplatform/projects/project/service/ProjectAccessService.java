package com.inpt.collaborationplatform.projects.project.service;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMember requireViewer(Project project, String userId) {
        return projectMemberRepository.findByProject_IdAndUserId(project.getId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));
    }

    public ProjectMember requireContributor(Project project, String userId) {
        ProjectMember member = requireViewer(project, userId);
        if (member.getRole() == ProjectRole.VIEWER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot contribute to this project");
        }
        return member;
    }

    public ProjectMember requireManager(Project project, String userId) {
        ProjectMember member = requireViewer(project, userId);
        if (member.getRole() != ProjectRole.OWNER && member.getRole() != ProjectRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage this project");
        }
        return member;
    }

    public ProjectMember requireOwner(Project project, String userId) {
        ProjectMember member = requireViewer(project, userId);
        if (member.getRole() != ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can do this");
        }
        return member;
    }
}
