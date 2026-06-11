package com.inpt.collaborationplatform.tasks.task.service;

import com.inpt.collaborationplatform.tasks.task.entity.Task;
import com.inpt.collaborationplatform.tasks.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TaskLookupService {

    private final TaskRepository taskRepository;

    public Task requireTask(String taskId, String teamId) {
        return taskRepository.findByIdAndTeam_Id(taskId, teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public Task requireTaskByIdAndProject(String taskId, String projectId) {
        return taskRepository.findByIdAndProject_Id(taskId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found in this project"));
    }
}
