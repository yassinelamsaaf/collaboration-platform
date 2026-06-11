package com.inpt.collaborationplatform.notification.controller;

import com.inpt.collaborationplatform.notification.entity.NotificationPrefs;
import com.inpt.collaborationplatform.shared.util.SecurityUtils;
import com.inpt.collaborationplatform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/prefs")
@RequiredArgsConstructor
public class NotificationPrefsController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<NotificationPrefs> getPrefs(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getOrCreatePrefs(SecurityUtils.currentUserId(authentication)));
    }

    @PutMapping
    public ResponseEntity<NotificationPrefs> updatePrefs(
            @RequestBody NotificationPrefs updated,
            Authentication authentication
    ) {
        return ResponseEntity.ok(notificationService.updatePrefs(SecurityUtils.currentUserId(authentication), updated));
    }

}
