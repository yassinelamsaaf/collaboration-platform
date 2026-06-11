package com.inpt.collaborationplatform.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_prefs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPrefs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Builder.Default
    private boolean emailOnTaskAssignment = true;

    @Builder.Default
    private boolean emailOnCommentAdded = true;

    @Builder.Default
    private boolean emailOnStatusChange = true;

    @Builder.Default
    private boolean emailOnDeadlineApproaching = true;

    @Builder.Default
    private boolean emailOnMemberInvited = true;
}
