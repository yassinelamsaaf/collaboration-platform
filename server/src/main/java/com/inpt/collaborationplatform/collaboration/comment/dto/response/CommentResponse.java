package com.inpt.collaborationplatform.collaboration.comment.dto.response;

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
