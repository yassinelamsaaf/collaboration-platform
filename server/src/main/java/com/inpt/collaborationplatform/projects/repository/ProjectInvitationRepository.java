package com.inpt.collaborationplatform.projects.repository;

import com.inpt.collaborationplatform.projects.entity.ProjectInvitation;
import com.inpt.collaborationplatform.projects.entity.ProjectInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, String> {
    Optional<ProjectInvitation> findByToken(String token);

    Optional<ProjectInvitation> findByProject_IdAndEmailIgnoreCaseAndStatus(
            String projectId,
            String email,
            ProjectInvitationStatus status
    );

    List<ProjectInvitation> findByProject_IdOrderByCreatedAtDesc(String projectId);
}
