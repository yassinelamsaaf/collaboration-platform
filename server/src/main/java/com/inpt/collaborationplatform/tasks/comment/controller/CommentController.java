package com.inpt.collaborationplatform.tasks.comment.controller;

import com.inpt.collaborationplatform.shared.dto.MessageResponse;
import com.inpt.collaborationplatform.shared.util.SecurityUtils;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import com.inpt.collaborationplatform.tasks.comment.dto.request.CreateCommentRequest;
import com.inpt.collaborationplatform.tasks.comment.dto.response.CommentResponse;
import com.inpt.collaborationplatform.tasks.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(projectRef, teamRef, taskId, request, SecurityUtils.currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CommentResponse>> listComments(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.listComments(projectRef, teamRef, taskId, SecurityUtils.currentUserId(authentication), pageable));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<MessageResponse> deleteComment(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @PathVariable String commentId,
            Authentication authentication
    ) {
        commentService.deleteComment(projectRef, teamRef, taskId, commentId, SecurityUtils.currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully"));
    }

}
