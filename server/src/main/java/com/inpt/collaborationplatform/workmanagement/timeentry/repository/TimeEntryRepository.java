package com.inpt.collaborationplatform.workmanagement.timeentry.repository;

import com.inpt.collaborationplatform.workmanagement.timeentry.entity.TimeEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, String> {
    Page<TimeEntry> findByTask_Id(String taskId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) FROM TimeEntry t WHERE t.task.id = :taskId")
    int sumDurationMinutesByTask_Id(@Param("taskId") String taskId);

    void deleteByTask_Id(String taskId);
}
