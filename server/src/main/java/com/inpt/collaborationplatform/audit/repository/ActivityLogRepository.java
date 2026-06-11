package com.inpt.collaborationplatform.audit.repository;

import com.inpt.collaborationplatform.audit.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
    Page<ActivityLog> findByProjectIdOrderByTimestampDesc(String projectId, Pageable pageable);
}
