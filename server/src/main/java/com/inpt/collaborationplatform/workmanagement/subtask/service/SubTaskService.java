package com.inpt.collaborationplatform.workmanagement.subtask.service;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.service.TeamLookupService;
import com.inpt.collaborationplatform.workmanagement.subtask.dto.request.CreateSubTaskRequest;
import com.inpt.collaborationplatform.workmanagement.subtask.dto.request.UpdateSubTaskRequest;
import com.inpt.collaborationplatform.workmanagement.subtask.dto.response.SubTaskResponse;
import com.inpt.collaborationplatform.workmanagement.subtask.entity.SubTask;
import com.inpt.collaborationplatform.workmanagement.subtask.mapper.SubTaskMapper;
import com.inpt.collaborationplatform.workmanagement.subtask.repository.SubTaskRepository;
import com.inpt.collaborationplatform.workmanagement.task.entity.Task;
import com.inpt.collaborationplatform.workmanagement.task.entity.TaskStatus;
import com.inpt.collaborationplatform.workmanagement.task.service.TaskLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TeamLookupService teamLookupService;
    private final TaskLookupService taskLookupService;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessService projectAccessService;
    private final SubTaskMapper subTaskMapper;

    @Transactional
    public SubTaskResponse createSubTask(String projectRef, String teamRef, String taskId, CreateSubTaskRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);
        teamLookupService.requireTeamLeader(team.getId(), currentUserId);
        Task task = taskLookupService.requireTask(taskId, team.getId());

        if (request.getAssigneeId() != null) {
            teamLookupService.requireTeamMember(team.getId(), request.getAssigneeId());
        }

        TaskStatus status = request.getStatus() != null ? request.getStatus() : TaskStatus.TODO;

        SubTask subTask = SubTask.builder()
                .task(task)
                .title(request.getTitle().trim())
                .assigneeId(request.getAssigneeId())
                .status(status)
                .isDone(status == TaskStatus.DONE)
                .build();

        return subTaskMapper.toSubTaskResponse(subTaskRepository.save(subTask));
    }

    @Transactional(readOnly = true)
    public List<SubTaskResponse> listSubTasks(String projectRef, String teamRef, String taskId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);
        taskLookupService.requireTask(taskId, team.getId());

        return subTaskRepository.findByTask_IdOrderByCreatedAtAsc(taskId).stream()
                .map(subTaskMapper::toSubTaskResponse)
                .toList();
    }

    @Transactional
    public SubTaskResponse updateSubTask(String projectRef, String teamRef, String taskId, String subTaskId, UpdateSubTaskRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);
        teamLookupService.requireTeamLeader(team.getId(), currentUserId);
        taskLookupService.requireTask(taskId, team.getId());

        SubTask subTask = requireSubTask(subTaskId, taskId);

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sub-task title cannot be blank");
            }
            subTask.setTitle(title);
        }

        if (request.getStatus() != null) {
            subTask.setStatus(request.getStatus());
            subTask.setDone(request.getStatus() == TaskStatus.DONE);
        } else if (request.getIsDone() != null) {
            subTask.setDone(request.getIsDone());
            subTask.setStatus(request.getIsDone() ? TaskStatus.DONE : TaskStatus.TODO);
        }

        if (request.getAssigneeId() != null) {
            teamLookupService.requireTeamMember(team.getId(), request.getAssigneeId());
            subTask.setAssigneeId(request.getAssigneeId());
        }

        return subTaskMapper.toSubTaskResponse(subTaskRepository.save(subTask));
    }

    @Transactional
    public void deleteSubTask(String projectRef, String teamRef, String taskId, String subTaskId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);
        teamLookupService.requireTeamLeader(team.getId(), currentUserId);
        taskLookupService.requireTask(taskId, team.getId());

        subTaskRepository.delete(requireSubTask(subTaskId, taskId));
    }

    private SubTask requireSubTask(String subTaskId, String taskId) {
        return subTaskRepository.findById(subTaskId)
                .filter(st -> st.getTask().getId().equals(taskId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sub-task not found"));
    }
}
