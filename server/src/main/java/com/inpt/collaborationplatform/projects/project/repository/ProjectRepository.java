package com.inpt.collaborationplatform.projects.project.repository;

import com.inpt.collaborationplatform.projects.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
}
