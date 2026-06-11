package com.inpt.collaborationplatform.tasks.comment.mapper;

import com.inpt.collaborationplatform.tasks.comment.dto.response.CommentResponse;
import com.inpt.collaborationplatform.tasks.comment.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
