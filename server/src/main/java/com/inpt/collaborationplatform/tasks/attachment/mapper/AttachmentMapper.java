package com.inpt.collaborationplatform.tasks.attachment.mapper;

import com.inpt.collaborationplatform.tasks.attachment.dto.response.AttachmentResponse;
import com.inpt.collaborationplatform.tasks.attachment.entity.Attachment;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponse toAttachmentResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getTask().getId(),
                attachment.getUserId(),
                attachment.getFileName(),
                attachment.getFileUrl(),
                attachment.getFileSize(),
                attachment.getCreatedAt()
        );
    }
}
