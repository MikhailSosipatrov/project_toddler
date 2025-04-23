package com.toddler.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectDto {
    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private String status;
}
