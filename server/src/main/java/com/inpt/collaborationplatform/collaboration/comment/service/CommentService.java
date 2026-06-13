package com.inpt.collaborationplatform.collaboration.comment.service;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.projects.team.entity.Team;
import com.inpt.collaborationplatform.projects.team.service.TeamLookupService;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import com.inpt.collaborationplatform.collaboration.comment.dto.request.CreateCommentRequest;
import com.inpt.collaborationplatform.collaboration.comment.dto.response.CommentResponse;
import com.inpt.collaborationplatform.collaboration.comment.entity.Comment;
import com.inpt.collaborationplatform.collaboration.comment.mapper.CommentMapper;
import com.inpt.collaborationplatform.collaboration.comment.repository.CommentRepository;
import com.inpt.collaborationplatform.workmanagement.task.entity.Task;
import com.inpt.collaborationplatform.shared.event.CommentAddedEvent;
import com.inpt.collaborationplatform.workmanagement.task.service.TaskLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TeamLookupService teamLookupService;
    private final TaskLookupService taskLookupService;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessService projectAccessService;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentResponse createComment(String projectRef, String teamRef, String taskId, CreateCommentRequest request, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);

        Task task = taskLookupService.requireTask(taskId, team.getId());

        Comment comment = Comment.builder()
                .task(task)
                .userId(currentUserId)
                .content(request.getContent().trim())
                .build();

        comment = commentRepository.save(comment);

        eventPublisher.publishEvent(new CommentAddedEvent(
                comment.getId(), comment.getContent(), task.getId(), task.getTitle(),
                project.getId(), team.getId(), task.getAssigneeId(), currentUserId
        ));

        return commentMapper.toCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> listComments(String projectRef, String teamRef, String taskId, String currentUserId, Pageable pageable) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, currentUserId);
        Team team = teamLookupService.requireTeam(project.getId(), teamRef);
        taskLookupService.requireTask(taskId, team.getId());

        return PageResponse.from(commentRepository.findByTask_IdOrderByCreatedAtDesc(taskId, pageable)
                .map(commentMapper::toCommentResponse));
    }

    @Transactional
    public void deleteComment(String projectRef, String teamRef, String taskId, String commentId, String currentUserId) {
        Project project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireContributor(project, currentUserId);
        teamLookupService.requireTeam(project.getId(), teamRef);

        Comment comment = commentRepository.findById(commentId)
                .filter(c -> c.getTask().getId().equals(taskId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}
