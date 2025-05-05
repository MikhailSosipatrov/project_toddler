package com.toddler.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateTaskPayload(
        @NotBlank @Size(min = 3, max = 255) String title,
        @Size(max = 2000) String description,
        UUID assigneeId,
        String priority,
        String deadline
) {}