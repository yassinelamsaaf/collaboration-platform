package com.inpt.collaborationplatform.tasks.comment.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        String id,
        String taskId,
        String userId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
