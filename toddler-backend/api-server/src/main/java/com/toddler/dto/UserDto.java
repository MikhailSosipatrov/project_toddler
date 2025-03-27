package com.toddler.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String email;
    private String passwordHash;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private String profilePicture;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private Boolean isVerified;
}

