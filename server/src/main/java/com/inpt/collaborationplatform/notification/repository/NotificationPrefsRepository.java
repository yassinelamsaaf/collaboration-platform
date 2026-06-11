package com.inpt.collaborationplatform.notification.repository;

import com.inpt.collaborationplatform.notification.entity.NotificationPrefs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPrefsRepository extends JpaRepository<NotificationPrefs, String> {
    Optional<NotificationPrefs> findByUserId(String userId);
}
