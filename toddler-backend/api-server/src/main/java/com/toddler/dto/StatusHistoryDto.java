package com.toddler.dto;

import lombok.Data;

@Data
public class StatusHistoryDto {
    private String status;
    private String statusEnteredAt; // ISO date string
    private String statusLeftAt; // ISO date string or null
}
