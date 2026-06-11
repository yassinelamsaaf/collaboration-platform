package com.inpt.collaborationplatform.projects.project.mapper;

import com.inpt.collaborationplatform.projects.project.dto.response.ProjectMemberResponse;
import com.inpt.collaborationplatform.projects.project.dto.response.ProjectResponse;
import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.entity.ProjectMember;
import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toProjectResponse(Project project, ProjectRole currentUserRole) {
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

    public ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                member.getUserId(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
