package com.toddler.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskReportDto {
    private String id; // UUID as String
    private String title;
    private String description;
    private String status; // TODO, IN_PROGRESS, DONE, TEST
    private String priority; // LOW, MEDIUM, HIGH
    private String assigneeName; // From users.username
    private String createdAt; // ISO date string
    private LocalDate dueDate; // ISO date string
}
