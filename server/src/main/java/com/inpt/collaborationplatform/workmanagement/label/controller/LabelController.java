package com.inpt.collaborationplatform.workmanagement.label.controller;

import com.inpt.collaborationplatform.shared.dto.MessageResponse;
import com.inpt.collaborationplatform.shared.util.SecurityUtils;
import com.inpt.collaborationplatform.workmanagement.label.dto.request.CreateLabelRequest;
import com.inpt.collaborationplatform.workmanagement.label.dto.response.LabelResponse;
import com.inpt.collaborationplatform.workmanagement.label.service.LabelService;
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
@RequestMapping("/api/projects/{projectRef}/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelResponse> createLabel(
            @PathVariable String projectRef,
            @Valid @RequestBody CreateLabelRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(labelService.createLabel(projectRef, request, SecurityUtils.currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<LabelResponse>> listLabels(
            @PathVariable String projectRef,
            Authentication authentication
    ) {
        return ResponseEntity.ok(labelService.listLabels(projectRef, SecurityUtils.currentUserId(authentication)));
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<MessageResponse> deleteLabel(
            @PathVariable String projectRef,
            @PathVariable String labelId,
            Authentication authentication
    ) {
        labelService.deleteLabel(projectRef, labelId, SecurityUtils.currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Label deleted successfully"));
    }

    @PostMapping("/{labelId}/tasks/{taskId}")
    public ResponseEntity<MessageResponse> addLabelToTask(
            @PathVariable String projectRef,
            @PathVariable String labelId,
            @PathVariable String taskId,
            Authentication authentication
    ) {
        labelService.addLabelToTask(projectRef, taskId, labelId, SecurityUtils.currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Label added to task successfully"));
    }

    @DeleteMapping("/{labelId}/tasks/{taskId}")
    public ResponseEntity<MessageResponse> removeLabelFromTask(
            @PathVariable String projectRef,
            @PathVariable String labelId,
            @PathVariable String taskId,
            Authentication authentication
    ) {
        labelService.removeLabelFromTask(projectRef, taskId, labelId, SecurityUtils.currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Label removed from task successfully"));
    }

}
