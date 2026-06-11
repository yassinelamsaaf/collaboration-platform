package com.inpt.collaborationplatform.projects.invitation.repository;

import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitation;
import com.inpt.collaborationplatform.projects.invitation.entity.ProjectInvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, String> {
    Optional<ProjectInvitation> findByToken(String token);

    Optional<ProjectInvitation> findByProject_IdAndEmailIgnoreCaseAndStatus(
            String projectId,
            String email,
            ProjectInvitationStatus status
    );

    Page<ProjectInvitation> findByProject_Id(String projectId, Pageable pageable);
}
