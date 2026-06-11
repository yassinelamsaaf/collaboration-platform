package com.inpt.collaborationplatform.notification.service;

import com.inpt.collaborationplatform.notification.dto.response.NotificationResponse;
import com.inpt.collaborationplatform.notification.entity.Notification;
import com.inpt.collaborationplatform.notification.entity.NotificationPrefs;
import com.inpt.collaborationplatform.notification.entity.NotificationType;
import com.inpt.collaborationplatform.notification.mapper.NotificationMapper;
import com.inpt.collaborationplatform.notification.repository.NotificationPrefsRepository;
import com.inpt.collaborationplatform.notification.repository.NotificationRepository;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPrefsRepository notificationPrefsRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationResponse createNotification(String userId, NotificationType type, String title, String message,
                                                    String relatedEntityType, String relatedEntityId) {
        Notification notification = notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build());
        log.info("createNotification: saved id={}, userId={}, type={}", notification.getId(), notification.getUserId(), notification.getType());
        return notificationMapper.toResponse(notification);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listNotifications(String userId, Boolean unreadOnly, Pageable pageable) {
        PageResponse<Notification> page;
        if (Boolean.TRUE.equals(unreadOnly)) {
            page = PageResponse.from(
                    notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable));
        } else {
            page = PageResponse.from(
                    notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable));
        }
        log.info("listNotifications: userId={}, totalElements={}, ids={}",
                userId, page.totalElements(),
                page.content().stream().map(Notification::getId).toList());
        return new PageResponse<>(
                page.content().stream().map(notificationMapper::toResponse).toList(),
                page.page(), page.size(), page.totalElements(),
                page.totalPages(), page.first(), page.last()
        );
    }

    @Transactional
    public NotificationResponse markAsRead(String notificationId, String currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot mark another user's notification as read");
        }
        notification.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(String currentUserId) {
        var notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                currentUserId, Pageable.unpaged());
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public long countUnread(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationPrefs getOrCreatePrefs(String userId) {
        return notificationPrefsRepository.findByUserId(userId)
                .orElseGet(() -> notificationPrefsRepository.save(
                        NotificationPrefs.builder().userId(userId).build()));
    }

    @Transactional
    public NotificationPrefs updatePrefs(String userId, NotificationPrefs updated) {
        NotificationPrefs prefs = getOrCreatePrefs(userId);
        prefs.setEmailOnTaskAssignment(updated.isEmailOnTaskAssignment());
        prefs.setEmailOnCommentAdded(updated.isEmailOnCommentAdded());
        prefs.setEmailOnStatusChange(updated.isEmailOnStatusChange());
        prefs.setEmailOnDeadlineApproaching(updated.isEmailOnDeadlineApproaching());
        prefs.setEmailOnMemberInvited(updated.isEmailOnMemberInvited());
        return notificationPrefsRepository.save(prefs);
    }
}
