package com.inpt.collaborationplatform.notification.listener;

import com.inpt.collaborationplatform.Identity.service.EmailService;
import com.inpt.collaborationplatform.Identity.service.IdentityAccessService;
import com.inpt.collaborationplatform.notification.dto.response.NotificationResponse;
import com.inpt.collaborationplatform.notification.entity.NotificationPrefs;
import com.inpt.collaborationplatform.notification.entity.NotificationType;
import com.inpt.collaborationplatform.notification.repository.NotificationPrefsRepository;
import com.inpt.collaborationplatform.notification.service.NotificationService;
import com.inpt.collaborationplatform.projects.team.entity.TeamMember;
import com.inpt.collaborationplatform.projects.team.repository.TeamMemberRepository;
import com.inpt.collaborationplatform.shared.event.CommentAddedEvent;
import com.inpt.collaborationplatform.shared.event.DeadlineApproachingEvent;
import com.inpt.collaborationplatform.shared.event.MemberInvitedEvent;
import com.inpt.collaborationplatform.shared.event.TaskAssignedEvent;
import com.inpt.collaborationplatform.shared.event.TaskStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;
    private final NotificationPrefsRepository notificationPrefsRepository;
    private final EmailService emailService;
    private final IdentityAccessService identityAccessService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TeamMemberRepository teamMemberRepository;

    @EventListener
    public void onTaskAssigned(TaskAssignedEvent event) {
        if (event.newAssigneeId() == null || event.newAssigneeId().equals(event.oldAssigneeId())) {
            return;
        }
        String userId = resolveUserId(event.newAssigneeId());
        log.info("onTaskAssigned: newAssigneeId={}, userId={}, taskTitle={}", event.newAssigneeId(), userId, event.taskTitle());
        NotificationResponse notification = notificationService.createNotification(
                userId,
                NotificationType.TASK_ASSIGNED,
                "Task Assigned",
                "You were assigned to task \"" + event.taskTitle() + "\"",
                "TASK",
                event.taskId()
        );
        log.info("onTaskAssigned: notification created id={}, userId={}", notification.id(), notification.userId());
        push(notification);
        sendEmailIfOptedIn(userId, NotificationPrefs::isEmailOnTaskAssignment,
                "Task Assigned: " + event.taskTitle(),
                "You were assigned to task \"" + event.taskTitle() + "\".");
    }

    @EventListener
    public void onTaskStatusChanged(TaskStatusChangedEvent event) {
        if (event.assigneeId() == null) {
            return;
        }
        String userId = resolveUserId(event.assigneeId());
        NotificationResponse notification = notificationService.createNotification(
                userId,
                NotificationType.STATUS_CHANGED,
                "Status Changed",
                "Task \"" + event.taskTitle() + "\" changed from " + event.oldStatus() + " to " + event.newStatus(),
                "TASK",
                event.taskId()
        );
        push(notification);
        sendEmailIfOptedIn(userId, NotificationPrefs::isEmailOnStatusChange,
                "Status Changed: " + event.taskTitle(),
                "Task \"" + event.taskTitle() + "\" changed from " + event.oldStatus() + " to " + event.newStatus() + ".");
    }

    @EventListener
    public void onCommentAdded(CommentAddedEvent event) {
        if (event.taskAssigneeId() == null) {
            return;
        }
        String userId = resolveUserId(event.taskAssigneeId());
        if (userId.equals(event.triggeredByUserId())) {
            return;
        }
        NotificationResponse notification = notificationService.createNotification(
                userId,
                NotificationType.COMMENT_ADDED,
                "New Comment",
                event.triggeredByUserId() + " commented on task \"" + event.taskTitle() + "\"",
                "COMMENT",
                event.commentId()
        );
        push(notification);
        sendEmailIfOptedIn(userId, NotificationPrefs::isEmailOnCommentAdded,
                "New Comment on: " + event.taskTitle(),
                "A new comment was added to task \"" + event.taskTitle() + "\".");
    }

    @EventListener
    public void onMemberInvited(MemberInvitedEvent event) {
    }

    @EventListener
    public void onDeadlineApproaching(DeadlineApproachingEvent event) {
        if (event.assigneeId() == null) {
            return;
        }
        String userId = resolveUserId(event.assigneeId());
        NotificationResponse notification = notificationService.createNotification(
                userId,
                NotificationType.DEADLINE_APPROACHING,
                "Deadline Approaching",
                "Task \"" + event.taskTitle() + "\" is due soon",
                "TASK",
                event.taskId()
        );
        push(notification);
        sendEmailIfOptedIn(userId, NotificationPrefs::isEmailOnDeadlineApproaching,
                "Deadline Approaching: " + event.taskTitle(),
                "Task \"" + event.taskTitle() + "\" is due soon.");
    }

    private String resolveUserId(String teamMemberId) {
        return teamMemberRepository.findById(teamMemberId)
                .map(TeamMember::getUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team member not found"));
    }

    private void push(NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + notification.userId(), notification);
        } catch (Exception e) {
            log.error("WebSocket push failed for user {}: {}", notification.userId(), e.getMessage());
        }
    }

    @Async
    protected void sendEmailIfOptedIn(String userId, java.util.function.Predicate<NotificationPrefs> prefCheck,
                                       String subject, String body) {
        try {
            var prefsOpt = notificationPrefsRepository.findByUserId(userId);
            if (prefsOpt.isEmpty() || !prefCheck.test(prefsOpt.get())) {
                return;
            }
            String email = identityAccessService.requireUserEmail(userId);
            emailService.sendNotification(email, subject, body);
        } catch (Exception e) {
            log.error("Failed to send notification email to user {}: {}", userId, e.getMessage());
        }
    }
}
