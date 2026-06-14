package com.inpt.collaborationplatform.projects.team.repository;

import com.inpt.collaborationplatform.projects.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, String> {
    Page<Team> findByProject_Id(String projectId, Pageable pageable);

    @Query("""
            select t from Team t
            where t.project.id = :projectId
              and (
                lower(t.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(t.description, '')) like lower(concat('%', :query, '%'))
              )
            """)
    Page<Team> searchByProjectId(
            @Param("projectId") String projectId,
            @Param("query") String query,
            Pageable pageable
    );

    Optional<Team> findByIdAndProject_Id(String id, String projectId);

    Optional<Team> findBySlugAndProject_Id(String slug, String projectId);

    boolean existsByProject_IdAndSlug(String projectId, String slug);

    boolean existsByProject_IdAndNormalizedName(String projectId, String normalizedName);
}
