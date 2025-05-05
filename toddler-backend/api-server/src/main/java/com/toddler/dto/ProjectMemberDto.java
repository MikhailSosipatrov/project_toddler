package com.toddler.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectMemberDto {
    private UUID id;
    private UUID projectId;
    private UserDto user;
    private String role;
}
