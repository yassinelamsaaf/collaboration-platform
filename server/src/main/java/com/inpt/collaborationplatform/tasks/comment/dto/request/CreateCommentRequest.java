package com.inpt.collaborationplatform.tasks.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Comment content is required")
    @Size(max = 10000, message = "Comment must be at most 10000 characters")
    private String content;
}
