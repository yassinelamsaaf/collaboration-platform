package com.inpt.collaborationplatform.tasks.subtask.repository;

import com.inpt.collaborationplatform.tasks.subtask.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, String> {
    List<SubTask> findByTask_IdOrderByCreatedAtAsc(String taskId);

    void deleteByTask_Id(String taskId);
}
