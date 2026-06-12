package com.inpt.collaborationplatform.collaboration.comment.mapper;

import com.inpt.collaborationplatform.collaboration.comment.dto.response.CommentResponse;
import com.inpt.collaborationplatform.collaboration.comment.entity.Comment;
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
