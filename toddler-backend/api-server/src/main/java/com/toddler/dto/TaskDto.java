package com.toddler.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskDto {
    private UUID id;
    private String name;
    private String description;
    private String status;
    private String priority;
    private UUID projectId;
    private LocalDateTime deadline;
    private UserDto executor;
}
