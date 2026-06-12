package com.inpt.collaborationplatform.collaboration.listener;

import com.inpt.collaborationplatform.collaboration.attachment.repository.AttachmentRepository;
import com.inpt.collaborationplatform.collaboration.comment.repository.CommentRepository;
import com.inpt.collaborationplatform.shared.event.TaskDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollaborationCleanupListener {

    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    @EventListener
    public void onTaskDeleted(TaskDeletedEvent event) {
        commentRepository.deleteByTask_Id(event.taskId());
        attachmentRepository.deleteByTask_Id(event.taskId());
    }
}
