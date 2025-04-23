package com.toddler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskCommentEntity {
    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
