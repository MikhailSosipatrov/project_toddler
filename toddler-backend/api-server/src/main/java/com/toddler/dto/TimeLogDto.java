package com.toddler.dto;

import lombok.Data;

@Data
public class TimeLogDto {
    private String userName; // From users.username
    private double hours; // Calculated from start_time and end_time
    private String startTime; // ISO date string
}
