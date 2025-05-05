package com.toddler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailsTimeLogDto {
    private UUID id;
    private UUID taskId;
    private UUID userId;
    private String userName; // Optional, for frontend display
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
