package com.toddler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskStatusHistory {
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
