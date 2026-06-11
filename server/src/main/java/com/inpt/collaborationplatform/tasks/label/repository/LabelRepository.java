package com.inpt.collaborationplatform.tasks.label.repository;

import com.inpt.collaborationplatform.tasks.label.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, String> {
    List<Label> findByProject_Id(String projectId);

    Optional<Label> findByIdAndProject_Id(String id, String projectId);

    boolean existsByProject_IdAndName(String projectId, String name);
}
