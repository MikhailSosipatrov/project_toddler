package com.toddler.dto;

import lombok.Data;

@Data
public class GeneralStatsDto {
    private String projectName;
    private int taskCount;
    private int memberCount;
    private int commentCount;
    private double avgCompletionTime; // in hours
    private double maxCompletionTime; // in hours
    private double totalTimeSpent; // in hours
}
