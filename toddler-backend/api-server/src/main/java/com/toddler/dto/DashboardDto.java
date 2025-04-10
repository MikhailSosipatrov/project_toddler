package com.toddler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DashboardDto {
    private UUID uuid;
    private String name;
    private String description;
    private String role;
}
