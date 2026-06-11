package com.inpt.collaborationplatform.projects.project.repository;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Optional<Project> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
