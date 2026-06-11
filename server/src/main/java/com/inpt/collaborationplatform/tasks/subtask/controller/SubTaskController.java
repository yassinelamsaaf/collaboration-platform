package com.inpt.collaborationplatform.tasks.subtask.controller;

import com.inpt.collaborationplatform.shared.dto.MessageResponse;
import com.inpt.collaborationplatform.shared.util.SecurityUtils;
import com.inpt.collaborationplatform.tasks.subtask.dto.request.CreateSubTaskRequest;
import com.inpt.collaborationplatform.tasks.subtask.dto.request.UpdateSubTaskRequest;
import com.inpt.collaborationplatform.tasks.subtask.dto.response.SubTaskResponse;
import com.inpt.collaborationplatform.tasks.subtask.service.SubTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}/subtasks")
@RequiredArgsConstructor
public class SubTaskController {

    private final SubTaskService subTaskService;

    @PostMapping
    public ResponseEntity<SubTaskResponse> createSubTask(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @Valid @RequestBody CreateSubTaskRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subTaskService.createSubTask(projectRef, teamRef, taskId, request, SecurityUtils.currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<SubTaskResponse>> listSubTasks(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(subTaskService.listSubTasks(projectRef, teamRef, taskId, SecurityUtils.currentUserId(authentication)));
    }

    @PatchMapping("/{subTaskId}")
    public ResponseEntity<SubTaskResponse> updateSubTask(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @PathVariable String subTaskId,
            @Valid @RequestBody UpdateSubTaskRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(subTaskService.updateSubTask(projectRef, teamRef, taskId, subTaskId, request, SecurityUtils.currentUserId(authentication)));
    }

    @DeleteMapping("/{subTaskId}")
    public ResponseEntity<MessageResponse> deleteSubTask(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @PathVariable String subTaskId,
            Authentication authentication
    ) {
        subTaskService.deleteSubTask(projectRef, teamRef, taskId, subTaskId, SecurityUtils.currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Sub-task deleted successfully"));
    }

}
