package com.toddler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TasksOverTimeDto {
    private String[] labels; // e.g., dates or weeks
    private int[] data; // e.g., task counts
}
