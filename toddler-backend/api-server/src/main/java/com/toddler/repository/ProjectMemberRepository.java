package com.toddler.repository;

import com.toddler.entity.ProjectMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMemberEntity, Long> {
    List<ProjectMemberEntity> findAllByProjectId(UUID projectId);
    Optional<ProjectMemberEntity> findByProjectIdAndUserId(UUID projectId, UUID userId);
}
