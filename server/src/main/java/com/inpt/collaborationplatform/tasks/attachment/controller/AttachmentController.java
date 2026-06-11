package com.inpt.collaborationplatform.tasks.attachment.controller;

import com.inpt.collaborationplatform.Identity.entity.User;
import com.inpt.collaborationplatform.shared.dto.MessageResponse;
import com.inpt.collaborationplatform.tasks.attachment.dto.request.CreateAttachmentRequest;
import com.inpt.collaborationplatform.tasks.attachment.dto.response.AttachmentResponse;
import com.inpt.collaborationplatform.tasks.attachment.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<AttachmentResponse> createAttachment(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @Valid @RequestBody CreateAttachmentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attachmentService.createAttachment(projectRef, teamRef, taskId, request, currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<AttachmentResponse>> listAttachments(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(attachmentService.listAttachments(projectRef, teamRef, taskId, currentUserId(authentication)));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<MessageResponse> deleteAttachment(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @PathVariable String attachmentId,
            Authentication authentication
    ) {
        attachmentService.deleteAttachment(projectRef, teamRef, taskId, attachmentId, currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Attachment deleted successfully"));
    }

    private String currentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
