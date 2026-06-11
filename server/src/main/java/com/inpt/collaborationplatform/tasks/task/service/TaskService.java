package com.inpt.collaborationplatform.tasks.task.service;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.service.TeamLookupService;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import com.inpt.collaborationplatform.tasks.attachment.repository.AttachmentRepository;
import com.inpt.collaborationplatform.tasks.comment.repository.CommentRepository;
import com.inpt.collaborationplatform.tasks.subtask.entity.SubTask;
import com.inpt.collaborationplatform.tasks.subtask.repository.SubTaskRepository;
import com.inpt.collaborationplatform.tasks.task.dto.request.CreateTaskRequest;
import com.inpt.collaborationplatform.tasks.task.dto.request.UpdateTaskRequest;
import com.inpt.collaborationplatform.tasks.task.dto.request.UpdateTaskStatusRequest;
import com.inpt.collaborationplatform.tasks.task.dto.response.TaskResponse;
import com.inpt.collaborationplatform.tasks.task.entity.Priority;
import com.inpt.collaborationplatform.tasks.task.entity.Task;
import com.inpt.collaborationplatform.tasks.task.mapper.TaskMapper;
import com.inpt.collaborationplatform.tasks.task.repository.TaskRepository;
import com.inpt.collaborationplatform.tasks.timeentry.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final TeamLookupService teamLookupService;
    private final TaskLookupService taskLookupService;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessService projectAccessService;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponse createTask(String projectRef, String teamRef, CreateTaskRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireManager(project, currentUserId);

        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        if (request.getAssigneeId() != null) {
            teamLookupService.requireTeamMember(team.getId(), request.getAssigneeId());
        }

        Task task = Task.builder()
                .project(project)
                .team(team)
                .title(request.getTitle().trim())
                .description(normalizeOptionalText(request.getDescription()))
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .dueDate(request.getDueDate())
                .assigneeId(request.getAssigneeId())
                .createdByUserId(currentUserId)
                .build();

        return taskMapper.toTaskResponse(taskRepository.save(task), 0, 0, 0, 0, 0);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> listTasks(String projectRef, String teamRef, String currentUserId, Pageable pageable) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        return PageResponse.from(taskRepository.findByTeam_Id(team.getId(), pageable)
                .map(task -> {
                    var ag = computeAggregates(task.getId());
                    return taskMapper.toTaskResponse(task, ag.subTaskCount(), ag.completedSubTaskCount(),
                            ag.commentCount(), ag.attachmentCount(), ag.totalTimeMinutes());
                }));
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(String projectRef, String teamRef, String taskId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());
        var ag = computeAggregates(task.getId());
        return taskMapper.toTaskResponse(task, ag.subTaskCount(), ag.completedSubTaskCount(),
                ag.commentCount(), ag.attachmentCount(), ag.totalTimeMinutes());
    }

    @Transactional
    public TaskResponse updateTask(String projectRef, String teamRef, String taskId, UpdateTaskRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireManager(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task title cannot be blank");
            }
            task.setTitle(title);
        }

        if (request.getDescription() != null) {
            task.setDescription(normalizeOptionalText(request.getDescription()));
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.getAssigneeId() != null) {
            teamLookupService.requireTeamMember(team.getId(), request.getAssigneeId());
            task.setAssigneeId(request.getAssigneeId());
        }

        var ag = computeAggregates(task.getId());
        return taskMapper.toTaskResponse(taskRepository.save(task), ag.subTaskCount(), ag.completedSubTaskCount(),
                ag.commentCount(), ag.attachmentCount(), ag.totalTimeMinutes());
    }

    @Transactional
    public TaskResponse updateTaskStatus(String projectRef, String teamRef, String taskId, UpdateTaskStatusRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());
        task.setStatus(request.getStatus());

        var ag = computeAggregates(task.getId());
        return taskMapper.toTaskResponse(taskRepository.save(task), ag.subTaskCount(), ag.completedSubTaskCount(),
                ag.commentCount(), ag.attachmentCount(), ag.totalTimeMinutes());
    }

    @Transactional
    public void deleteTask(String projectRef, String teamRef, String taskId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireManager(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());

        subTaskRepository.deleteByTask_Id(task.getId());
        commentRepository.deleteByTask_Id(task.getId());
        attachmentRepository.deleteByTask_Id(task.getId());
        timeEntryRepository.deleteByTask_Id(task.getId());

        taskRepository.delete(task);
    }

    private record TaskAggregates(int subTaskCount, int completedSubTaskCount,
                                  int commentCount, int attachmentCount, int totalTimeMinutes) {}

    private TaskAggregates computeAggregates(String taskId) {
        var subTasks = subTaskRepository.findByTask_IdOrderByCreatedAtAsc(taskId);
        int subTaskCount = subTasks.size();
        int completedSubTaskCount = (int) subTasks.stream().filter(SubTask::isDone).count();
        int commentCount = (int) commentRepository.countByTask_Id(taskId);
        int attachmentCount = (int) attachmentRepository.countByTask_Id(taskId);
        int totalTimeMinutes = timeEntryRepository.sumDurationMinutesByTask_Id(taskId);
        return new TaskAggregates(subTaskCount, completedSubTaskCount, commentCount, attachmentCount, totalTimeMinutes);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
