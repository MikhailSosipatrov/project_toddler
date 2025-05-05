package com.toddler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailsDto {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private UUID projectId;
    private LocalDate deadline;
    private UserDto assignee;
    private List<TaskCommentDto> comments;
    private List<TaskStatusHistoryDto> statusHistory;
    private List<TaskDetailsTimeLogDto> timeLogs;
}
