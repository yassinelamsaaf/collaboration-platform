package com.inpt.collaborationplatform.projects.team.repository;

import com.inpt.collaborationplatform.projects.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, String> {
    Page<Team> findByProject_Id(String projectId, Pageable pageable);

    Optional<Team> findByIdAndProject_Id(String id, String projectId);

    Optional<Team> findBySlugAndProject_Id(String slug, String projectId);

    boolean existsByProject_IdAndSlug(String projectId, String slug);

    boolean existsByProject_IdAndNormalizedName(String projectId, String normalizedName);
}
