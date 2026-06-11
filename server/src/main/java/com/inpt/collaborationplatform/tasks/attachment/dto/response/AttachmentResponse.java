package com.inpt.collaborationplatform.tasks.attachment.dto.response;

import java.time.LocalDateTime;

public record AttachmentResponse(
        String id,
        String taskId,
        String userId,
        String fileName,
        String fileUrl,
        long fileSize,
        LocalDateTime createdAt
) {
}
