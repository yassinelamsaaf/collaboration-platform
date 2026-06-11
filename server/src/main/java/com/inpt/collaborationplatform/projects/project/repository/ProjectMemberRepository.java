package com.inpt.collaborationplatform.projects.project.repository;

import com.inpt.collaborationplatform.projects.project.entity.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {
    Optional<ProjectMember> findByProject_IdAndUserId(String projectId, String userId);

    boolean existsByProject_IdAndUserId(String projectId, String userId);

    Page<ProjectMember> findByUserId(String userId, Pageable pageable);

    Page<ProjectMember> findByProject_Id(String projectId, Pageable pageable);
}
