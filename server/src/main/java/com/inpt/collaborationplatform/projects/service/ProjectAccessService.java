package com.inpt.collaborationplatform.projects.service;

import com.inpt.collaborationplatform.projects.entity.Project;
import com.inpt.collaborationplatform.projects.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.entity.ProjectRole;
import com.inpt.collaborationplatform.projects.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {
// RBAC
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMember requireMember(Project project, String userId) {
        return projectMemberRepository.findByProject_IdAndUserId(project.getId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));
    }

    public ProjectMember requireManager(Project project, String userId) {
        ProjectMember member = requireMember(project, userId);
        if (member.getRole() != ProjectRole.OWNER && member.getRole() != ProjectRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage this project");
        }
        return member;
    }

    public ProjectMember requireOwner(Project project, String userId) {
        ProjectMember member = requireMember(project, userId);
        if (member.getRole() != ProjectRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can do this");
        }
        return member;
    }
}
