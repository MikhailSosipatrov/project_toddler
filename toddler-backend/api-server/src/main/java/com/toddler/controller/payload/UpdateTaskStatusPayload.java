package com.toddler.controller.payload;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusPayload(
        @NotNull String status
) {}