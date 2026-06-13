package com.inpt.collaborationplatform.workmanagement.task.service;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.service.TeamLookupService;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import com.inpt.collaborationplatform.collaboration.service.CollaborationQueryService;
import com.inpt.collaborationplatform.workmanagement.subtask.repository.SubTaskRepository;
import com.inpt.collaborationplatform.workmanagement.task.dto.request.CreateTaskRequest;
import com.inpt.collaborationplatform.workmanagement.task.dto.request.UpdateTaskRequest;
import com.inpt.collaborationplatform.workmanagement.task.dto.request.UpdateTaskStatusRequest;
import com.inpt.collaborationplatform.workmanagement.task.dto.response.TaskResponse;
import com.inpt.collaborationplatform.workmanagement.task.entity.Priority;
import com.inpt.collaborationplatform.workmanagement.task.entity.Task;
import com.inpt.collaborationplatform.workmanagement.task.entity.TaskStatus;
import com.inpt.collaborationplatform.workmanagement.task.mapper.TaskMapper;
import com.inpt.collaborationplatform.shared.event.TaskAssignedEvent;
import com.inpt.collaborationplatform.shared.event.TaskDeletedEvent;
import com.inpt.collaborationplatform.shared.event.TaskStatusChangedEvent;
import com.inpt.collaborationplatform.workmanagement.task.repository.TaskRepository;
import com.inpt.collaborationplatform.workmanagement.timeentry.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final TimeEntryRepository timeEntryRepository;
    private final CollaborationQueryService collaborationQueryService;
    private final TeamLookupService teamLookupService;
    private final TaskLookupService taskLookupService;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessService projectAccessService;
    private final ApplicationEventPublisher eventPublisher;
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

        task = taskRepository.save(task);

        if (request.getAssigneeId() != null) {
            eventPublisher.publishEvent(new TaskAssignedEvent(
                    task.getId(), task.getTitle(), project.getId(), team.getId(),
                    null, request.getAssigneeId(), currentUserId
            ));
        }

        return taskMapper.toTaskResponse(task, 0, 0, 0, 0, 0);
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

        String oldAssigneeId = task.getAssigneeId();
        if (request.getAssigneeId() != null) {
            teamLookupService.requireTeamMember(team.getId(), request.getAssigneeId());
            task.setAssigneeId(request.getAssigneeId());
        }

        task = taskRepository.save(task);

        if (request.getAssigneeId() != null && !request.getAssigneeId().equals(oldAssigneeId)) {
            eventPublisher.publishEvent(new TaskAssignedEvent(
                    task.getId(), task.getTitle(), project.getId(), team.getId(),
                    oldAssigneeId, request.getAssigneeId(), currentUserId
            ));
        }

        var ag = computeAggregates(task.getId());
        return taskMapper.toTaskResponse(task, ag.subTaskCount(), ag.completedSubTaskCount(),
                ag.commentCount(), ag.attachmentCount(), ag.totalTimeMinutes());
    }

    @Transactional
    public TaskResponse updateTaskStatus(String projectRef, String teamRef, String taskId, UpdateTaskStatusRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(request.getStatus());

        task = taskRepository.save(task);

        eventPublisher.publishEvent(new TaskStatusChangedEvent(
                task.getId(), task.getTitle(), project.getId(), team.getId(),
                oldStatus.name(), request.getStatus().name(), task.getAssigneeId(), currentUserId
        ));

        var ag = computeAggregates(task.getId());
        return taskMapper.toTaskResponse(task, ag.subTaskCount(), ag.completedSubTaskCount(),
                ag.commentCount(), ag.attachmentCount(), ag.totalTimeMinutes());
    }

    @Transactional
    public void deleteTask(String projectRef, String teamRef, String taskId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireManager(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());

        subTaskRepository.deleteByTask_Id(task.getId());
        eventPublisher.publishEvent(new TaskDeletedEvent(
                task.getId(), task.getTitle(), project.getId(), team.getId(), currentUserId
        ));
        timeEntryRepository.deleteByTask_Id(task.getId());

        taskRepository.delete(task);
    }

    private record TaskAggregates(int subTaskCount, int completedSubTaskCount,
                                  int commentCount, int attachmentCount, int totalTimeMinutes) {}

    private TaskAggregates computeAggregates(String taskId) {
        var subTasks = subTaskRepository.findByTask_IdOrderByCreatedAtAsc(taskId);
        int subTaskCount = subTasks.size();
        int completedSubTaskCount = (int) subTasks.stream().filter(st -> st.getStatus() == com.inpt.collaborationplatform.workmanagement.task.entity.TaskStatus.DONE).count();
        int commentCount = collaborationQueryService.countCommentsForTask(taskId);
        int attachmentCount = collaborationQueryService.countAttachmentsForTask(taskId);
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
