package com.inpt.collaborationplatform.collaboration.attachment.service;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.service.TeamLookupService;
import com.inpt.collaborationplatform.collaboration.attachment.dto.request.CreateAttachmentRequest;
import com.inpt.collaborationplatform.collaboration.attachment.dto.response.AttachmentResponse;
import com.inpt.collaborationplatform.collaboration.attachment.entity.Attachment;
import com.inpt.collaborationplatform.collaboration.attachment.mapper.AttachmentMapper;
import com.inpt.collaborationplatform.collaboration.attachment.repository.AttachmentRepository;
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
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TeamLookupService teamLookupService;
    private final TaskLookupService taskLookupService;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessService projectAccessService;
    private final AttachmentMapper attachmentMapper;

    @Transactional
    public AttachmentResponse createAttachment(String projectRef, String teamRef, String taskId, CreateAttachmentRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());

        Attachment attachment = Attachment.builder()
                .task(task)
                .userId(currentUserId)
                .fileName(request.getFileName().trim())
                .fileUrl(request.getFileUrl().trim())
                .fileSize(request.getFileSize())
                .build();

        return attachmentMapper.toAttachmentResponse(attachmentRepository.save(attachment));
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listAttachments(String projectRef, String teamRef, String taskId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);
        taskLookupService.requireTask(taskId, team.getId());

        return attachmentRepository.findByTask_IdOrderByCreatedAtDesc(taskId).stream()
                .map(attachmentMapper::toAttachmentResponse)
                .toList();
    }

    @Transactional
    public void deleteAttachment(String projectRef, String teamRef, String taskId, String attachmentId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);
        teamLookupService.requireTeam(project.getId(), teamRef);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .filter(a -> a.getTask().getId().equals(taskId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        if (!attachment.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own attachments");
        }

        attachmentRepository.delete(attachment);
    }
}
