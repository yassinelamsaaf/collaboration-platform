package com.inpt.collaborationplatform.workmanagement.task.repository;

import com.inpt.collaborationplatform.workmanagement.task.entity.Task;
import com.inpt.collaborationplatform.workmanagement.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    Page<Task> findByTeam_Id(String teamId, Pageable pageable);

    Optional<Task> findByIdAndProject_Id(String id, String projectId);

    Optional<Task> findByIdAndTeam_Id(String id, String teamId);

    long countByTeam_Id(String teamId);

    long countByTeam_IdAndStatus(String teamId, TaskStatus status);

    List<Task> findByDueDate(LocalDate dueDate);
}
