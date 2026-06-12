package com.inpt.collaborationplatform.collaboration.service;

import com.inpt.collaborationplatform.collaboration.attachment.repository.AttachmentRepository;
import com.inpt.collaborationplatform.collaboration.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollaborationQueryService {

    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional(readOnly = true)
    public int countCommentsForTask(String taskId) {
        return Math.toIntExact(commentRepository.countByTask_Id(taskId));
    }

    @Transactional(readOnly = true)
    public int countAttachmentsForTask(String taskId) {
        return Math.toIntExact(attachmentRepository.countByTask_Id(taskId));
    }
}
