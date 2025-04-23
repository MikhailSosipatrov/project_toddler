package com.toddler.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskPayload(
        @NotBlank @Size(min = 3, max = 255) String name,
        @Size(max = 2000) String description,
        @NotNull String priority,
        LocalDateTime deadline,
        UUID executorId,
        @NotNull String status
) {}