package com.inpt.collaborationplatform.tasks.comment.repository;

import com.inpt.collaborationplatform.tasks.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByTask_IdOrderByCreatedAtDesc(String taskId, Pageable pageable);

    long countByTask_Id(String taskId);

    void deleteByTask_Id(String taskId);
}
