package com.toddler.repository;

import com.toddler.entity.TaskStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistoryEntity, UUID> {
    List<TaskStatusHistoryEntity> findByTaskId(UUID taskId);
    Optional<TaskStatusHistoryEntity> findTopByTaskIdAndStatusOrderByStatusEnteredAtDesc(UUID taskId, String status);
}
