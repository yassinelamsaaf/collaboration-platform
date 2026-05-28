package com.inpt.collaborationplatform.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    private boolean enabled = false;

    // 6-digit OTP code (stored as String to preserve leading zeros e.g. "012345")
    private String verificationCode;

    private LocalDateTime verificationCodeExpiry;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}