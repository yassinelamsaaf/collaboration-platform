package com.inpt.collaborationplatform.audit.listener;

import com.inpt.collaborationplatform.audit.service.ActivityLogService;
import com.inpt.collaborationplatform.shared.event.CommentAddedEvent;
import com.inpt.collaborationplatform.shared.event.DeadlineApproachingEvent;
import com.inpt.collaborationplatform.shared.event.MemberInvitedEvent;
import com.inpt.collaborationplatform.shared.event.TaskAssignedEvent;
import com.inpt.collaborationplatform.shared.event.TaskDeletedEvent;
import com.inpt.collaborationplatform.shared.event.TaskStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogListener {

    private final ActivityLogService activityLogService;

    @EventListener
    public void onTaskAssigned(TaskAssignedEvent event) {
        log.info("ActivityLogListener.onTaskAssigned: taskId={}, taskTitle={}", event.taskId(), event.taskTitle());
        activityLogService.log(
                event.triggeredByUserId(),
                event.projectId(),
                "TASK",
                event.taskId(),
                "ASSIGNED",
                "Task \"" + event.taskTitle() + "\" assigned to " + event.newAssigneeId()
        );
    }

    @EventListener
    public void onTaskStatusChanged(TaskStatusChangedEvent event) {
        activityLogService.log(
                event.triggeredByUserId(),
                event.projectId(),
                "TASK",
                event.taskId(),
                "STATUS_CHANGED",
                "Task \"" + event.taskTitle() + "\" moved from " + event.oldStatus() + " to " + event.newStatus()
        );
    }

    @EventListener
    public void onTaskDeleted(TaskDeletedEvent event) {
        activityLogService.log(
                event.triggeredByUserId(),
                event.projectId(),
                "TASK",
                event.taskId(),
                "DELETED",
                "Task \"" + event.taskTitle() + "\" deleted"
        );
    }

    @EventListener
    public void onCommentAdded(CommentAddedEvent event) {
        activityLogService.log(
                event.triggeredByUserId(),
                event.projectId(),
                "COMMENT",
                event.commentId(),
                "ADDED",
                "Comment added on task \"" + event.taskTitle() + "\""
        );
    }

    @EventListener
    public void onMemberInvited(MemberInvitedEvent event) {
        activityLogService.log(
                event.triggeredByUserId(),
                event.projectId(),
                "INVITATION",
                event.invitationId(),
                "INVITED",
                "Invited " + event.invitedEmail() + " to project \"" + event.projectName() + "\""
        );
    }

    @EventListener
    public void onDeadlineApproaching(DeadlineApproachingEvent event) {
        activityLogService.log(
                "SYSTEM",
                event.projectId(),
                "TASK",
                event.taskId(),
                "DEADLINE_APPROACHING",
                "Task \"" + event.taskTitle() + "\" is due soon"
        );
    }
}
