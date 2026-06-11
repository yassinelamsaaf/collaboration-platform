package com.inpt.collaborationplatform.tasks.label.service;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.tasks.label.dto.request.CreateLabelRequest;
import com.inpt.collaborationplatform.tasks.label.dto.response.LabelResponse;
import com.inpt.collaborationplatform.tasks.label.entity.Label;
import com.inpt.collaborationplatform.tasks.label.mapper.LabelMapper;
import com.inpt.collaborationplatform.tasks.label.repository.LabelRepository;
import com.inpt.collaborationplatform.tasks.task.entity.Task;
import com.inpt.collaborationplatform.tasks.task.service.TaskLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final TaskLookupService taskLookupService;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessService projectAccessService;
    private final LabelMapper labelMapper;

    @Transactional
    public LabelResponse createLabel(String projectRef, CreateLabelRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);

        String name = request.getName().trim();
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Label name cannot be blank");
        }

        if (labelRepository.existsByProject_IdAndName(project.getId(), name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A label with this name already exists in the project");
        }

        Label label = Label.builder()
                .project(project)
                .name(name)
                .color(request.getColor())
                .build();

        return labelMapper.toLabelResponse(labelRepository.save(label));
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> listLabels(String projectRef, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);

        return labelRepository.findByProject_Id(project.getId()).stream()
                .map(labelMapper::toLabelResponse)
                .toList();
    }

    @Transactional
    public void deleteLabel(String projectRef, String labelId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireManager(project, currentUserId);

        Label label = labelRepository.findByIdAndProject_Id(labelId, project.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Label not found"));

        labelRepository.delete(label);
    }

    @Transactional
    public void addLabelToTask(String projectRef, String taskId, String labelId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);

        Task task = taskLookupService.requireTaskByIdAndProject(taskId, project.getId());

        Label label = labelRepository.findByIdAndProject_Id(labelId, project.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Label not found"));

        task.getLabels().add(label);
        label.getTasks().add(task);

        labelRepository.flush();
    }

    @Transactional
    public void removeLabelFromTask(String projectRef, String taskId, String labelId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);

        Task task = taskLookupService.requireTaskByIdAndProject(taskId, project.getId());

        Label label = labelRepository.findByIdAndProject_Id(labelId, project.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Label not found"));

        task.getLabels().remove(label);
        label.getTasks().remove(task);

        labelRepository.flush();
    }
}
