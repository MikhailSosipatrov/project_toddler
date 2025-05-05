package com.toddler.repository;

import com.toddler.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    @Query("SELECT t FROM TaskEntity t WHERE t.dueDate < :now AND t.status NOT IN :excludedStatuses")
    List<TaskEntity> findByDueDateBeforeAndStatusNotIn(LocalDateTime now, List<String> excludedStatuses);

    // Find all tasks by project ID (no change needed as it's a Spring Data method)
    List<TaskEntity> findByProjectId(UUID projectId);

    // Count tasks for a specific project
    @Query(value = "SELECT COUNT(*) FROM tasks WHERE project_id = :projectId", nativeQuery = true)
    int countTasksByProjectId(@Param("projectId") UUID projectId);

    // Count comments for tasks in a specific project
    @Query(value = "SELECT COUNT(*) FROM task_comments tc " +
            "WHERE tc.task_id IN (SELECT t.id FROM tasks t WHERE t.project_id = :projectId)", nativeQuery = true)
    int countCommentsByProjectId(@Param("projectId") UUID projectId);

    // Count tasks grouped by status for a specific project
    @Query(value = "SELECT status, COUNT(*) FROM tasks WHERE project_id = :projectId GROUP BY status", nativeQuery = true)
    Object[][] findTaskStatusCountsByProjectId(@Param("projectId") UUID projectId);

    // Count tasks grouped by priority for a specific project
    @Query(value = "SELECT priority, COUNT(*) FROM tasks WHERE project_id = :projectId GROUP BY priority", nativeQuery = true)
    Object[][] findTaskPriorityCountsByProjectId(@Param("projectId") UUID projectId);

    // Calculate average completion time (in hours) by priority for a specific project
    @Query(value = "SELECT t.priority, AVG(EXTRACT(EPOCH FROM (ttl.end_time - ttl.start_time)) / 3600) " +
            "FROM tasks t " +
            "JOIN task_time_logs ttl ON t.id = ttl.task_id " +
            "WHERE t.project_id = :projectId AND ttl.end_time IS NOT NULL " +
            "GROUP BY t.priority", nativeQuery = true)
    Object[][] findAvgCompletionTimeByPriority(@Param("projectId") UUID projectId);

    // Count tasks assigned to each user (by username) for a specific project
    @Query(value = "SELECT u.username, COUNT(t.id) " +
            "FROM tasks t " +
            "JOIN users u ON t.assigned_to = u.id " +
            "WHERE t.project_id = :projectId " +
            "GROUP BY u.username", nativeQuery = true)
    Object[][] findTasksByUser(@Param("projectId") UUID projectId);

    // Calculate total time spent (in hours) by each user for a specific project
    @Query(value = "SELECT u.username, SUM(EXTRACT(EPOCH FROM (ttl.end_time - ttl.start_time)) / 3600) " +
            "FROM task_time_logs ttl " +
            "JOIN users u ON ttl.user_id = u.id " +
            "WHERE ttl.task_id IN (SELECT t.id FROM tasks t WHERE t.project_id = :projectId) " +
            "AND ttl.end_time IS NOT NULL " +
            "GROUP BY u.username", nativeQuery = true)
    Object[][] findTimeSpentByUser(@Param("projectId") UUID projectId);

    // Calculate average completion time (in hours) for all tasks in a specific project
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (ttl.end_time - ttl.start_time)) / 3600) " +
            "FROM task_time_logs ttl " +
            "WHERE ttl.task_id IN (SELECT t.id FROM tasks t WHERE t.project_id = :projectId) " +
            "AND ttl.end_time IS NOT NULL", nativeQuery = true)
    Double findAvgCompletionTime(@Param("projectId") UUID projectId);

    // Calculate maximum completion time (in hours) for any task in a specific project
    @Query(value = "SELECT MAX(EXTRACT(EPOCH FROM (ttl.end_time - ttl.start_time)) / 3600) " +
            "FROM task_time_logs ttl " +
            "WHERE ttl.task_id IN (SELECT t.id FROM tasks t WHERE t.project_id = :projectId) " +
            "AND ttl.end_time IS NOT NULL", nativeQuery = true)
    Double findMaxCompletionTime(@Param("projectId") UUID projectId);

    // Calculate total time spent (in hours) for all tasks in a specific project
    @Query(value = "SELECT SUM(EXTRACT(EPOCH FROM (ttl.end_time - ttl.start_time)) / 3600) " +
            "FROM task_time_logs ttl " +
            "WHERE ttl.task_id IN (SELECT t.id FROM tasks t WHERE t.project_id = :projectId) " +
            "AND ttl.end_time IS NOT NULL", nativeQuery = true)
    Double findTotalTimeSpent(@Param("projectId") UUID projectId);

    // Count tasks created per day for a specific project
    @Query(value = "SELECT TO_CHAR(t.created_at, 'YYYY-MM-DD') AS date, COUNT(t.id) " +
            "FROM tasks t " +
            "WHERE t.project_id = :projectId " +
            "GROUP BY TO_CHAR(t.created_at, 'YYYY-MM-DD') " +
            "ORDER BY TO_CHAR(t.created_at, 'YYYY-MM-DD')", nativeQuery = true)
    Object[][] findTasksOverTime(@Param("projectId") UUID projectId);
}
