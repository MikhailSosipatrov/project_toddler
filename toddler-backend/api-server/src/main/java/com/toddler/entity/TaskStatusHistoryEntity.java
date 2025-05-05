package com.toddler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusHistoryEntity {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "status_entered_at", nullable = false)
    private LocalDateTime statusEnteredAt;

    @Column(name = "status_left_at")
    private LocalDateTime statusLeftAt;
}
