package com.inpt.collaborationplatform.tasks.attachment.repository;

import com.inpt.collaborationplatform.tasks.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    List<Attachment> findByTask_IdOrderByCreatedAtDesc(String taskId);

    long countByTask_Id(String taskId);

    void deleteByTask_Id(String taskId);
}
