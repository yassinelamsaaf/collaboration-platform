package com.inpt.collaborationplatform.tasks.task.controller;

import com.inpt.collaborationplatform.shared.dto.MessageResponse;
import com.inpt.collaborationplatform.shared.util.SecurityUtils;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import com.inpt.collaborationplatform.tasks.task.dto.request.CreateTaskRequest;
import com.inpt.collaborationplatform.tasks.task.dto.request.UpdateTaskRequest;
import com.inpt.collaborationplatform.tasks.task.dto.request.UpdateTaskStatusRequest;
import com.inpt.collaborationplatform.tasks.task.dto.response.TaskResponse;
import com.inpt.collaborationplatform.tasks.task.service.TaskService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectRef}/teams/{teamRef}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(projectRef, teamRef, request, SecurityUtils.currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TaskResponse>> listTasks(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.listTasks(projectRef, teamRef, SecurityUtils.currentUserId(authentication), pageable));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(taskService.getTask(projectRef, teamRef, taskId, SecurityUtils.currentUserId(authentication)));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(taskService.updateTask(projectRef, teamRef, taskId, request, SecurityUtils.currentUserId(authentication)));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(taskService.updateTaskStatus(projectRef, teamRef, taskId, request, SecurityUtils.currentUserId(authentication)));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<MessageResponse> deleteTask(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            Authentication authentication
    ) {
        taskService.deleteTask(projectRef, teamRef, taskId, SecurityUtils.currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Task deleted successfully"));
    }

}
