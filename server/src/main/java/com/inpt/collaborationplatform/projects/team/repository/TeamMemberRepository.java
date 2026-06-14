package com.inpt.collaborationplatform.projects.team.repository;

import com.inpt.collaborationplatform.projects.team.entity.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {
    Page<TeamMember> findByTeam_Id(String teamId, Pageable pageable);

    Optional<TeamMember> findByTeam_IdAndUserId(String teamId, String userId);

    boolean existsByTeam_IdAndUserId(String teamId, String userId);

    long countByTeam_Id(String teamId);

    void deleteByTeam_Id(String teamId);

    void deleteByTeam_Project_IdAndUserId(String projectId, String userId);
}
