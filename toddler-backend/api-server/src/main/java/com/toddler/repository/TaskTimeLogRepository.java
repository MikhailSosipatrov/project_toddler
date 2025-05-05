package com.toddler.repository;

import com.toddler.entity.TaskTimeLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskTimeLogRepository extends JpaRepository<TaskTimeLogsEntity, UUID> {
    List<TaskTimeLogsEntity> findByTaskId(UUID taskId);
    Optional<TaskTimeLogsEntity> findByTaskIdAndUserIdAndEndTimeIsNull(UUID taskId, UUID userId);
}