package com.inpt.collaborationplatform.projects.project.dto.response;

import com.inpt.collaborationplatform.projects.project.entity.ProjectRole;

import java.time.LocalDateTime;

// Could add user details here if needed, but for now we just return IDs and role info. (yaani 7ta lfrontend o n9do nzido)
public record ProjectMemberResponse(
        String id,
        String projectId,
        String userId,
        ProjectRole role,
        LocalDateTime joinedAt,
        String memberName,
        String memberEmail
) {
}
