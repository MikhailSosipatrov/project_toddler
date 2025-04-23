package com.toddler.controller.payload;

import jakarta.validation.constraints.NotBlank;

public record UpdateProjectPayload (
        @NotBlank(message = "Название не должно быть пустым")
        String name,
        String description
) {}