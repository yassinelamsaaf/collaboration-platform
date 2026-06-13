package com.inpt.collaborationplatform.workmanagement.timeentry.service;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.service.TeamLookupService;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import com.inpt.collaborationplatform.workmanagement.task.entity.Task;
import com.inpt.collaborationplatform.workmanagement.task.service.TaskLookupService;
import com.inpt.collaborationplatform.workmanagement.timeentry.dto.request.CreateTimeEntryRequest;
import com.inpt.collaborationplatform.workmanagement.timeentry.dto.response.TimeEntryResponse;
import com.inpt.collaborationplatform.workmanagement.timeentry.entity.TimeEntry;
import com.inpt.collaborationplatform.workmanagement.timeentry.mapper.TimeEntryMapper;
import com.inpt.collaborationplatform.workmanagement.timeentry.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TeamLookupService teamLookupService;
    private final TaskLookupService taskLookupService;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessService projectAccessService;
    private final TimeEntryMapper timeEntryMapper;

    @Transactional
    public TimeEntryResponse logTime(String projectRef, String teamRef, String taskId, CreateTimeEntryRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());

        TimeEntry entry = TimeEntry.builder()
                .task(task)
                .userId(currentUserId)
                .durationMinutes(request.getDurationMinutes())
                .date(request.getDate())
                .description(normalizeOptionalText(request.getDescription()))
                .build();

        return timeEntryMapper.toTimeEntryResponse(timeEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public PageResponse<TimeEntryResponse> listTimeEntries(String projectRef, String teamRef, String taskId, String currentUserId, Pageable pageable) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);
        taskLookupService.requireTask(taskId, team.getId());

        return PageResponse.from(timeEntryRepository.findByTask_Id(taskId, pageable)
                .map(timeEntryMapper::toTimeEntryResponse));
    }

    @Transactional
    public void deleteTimeEntry(String projectRef, String teamRef, String taskId, String entryId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);
        taskLookupService.requireTask(taskId, team.getId());

        TimeEntry entry = timeEntryRepository.findById(entryId)
                .filter(e -> e.getTask().getId().equals(taskId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Time entry not found"));

        if (!entry.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own time entries");
        }

        timeEntryRepository.delete(entry);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
