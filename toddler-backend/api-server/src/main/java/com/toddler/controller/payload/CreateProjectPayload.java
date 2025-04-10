package com.toddler.controller.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateProjectPayload (
        @NotBlank(message = "Название не должно быть пустым")
        String name,
        String description
) {}
