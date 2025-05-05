package com.toddler.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskCommentPayload(
        @NotBlank @Size(min = 1, max = 1000) String text
) {}
