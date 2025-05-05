package com.toddler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@AllArgsConstructor
@Getter
public class DashboardDto {
    private UUID id;
    private String name;
    private String description;
    private String role;
}
