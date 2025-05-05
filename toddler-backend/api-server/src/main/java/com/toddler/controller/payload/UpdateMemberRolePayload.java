package com.toddler.controller.payload;

import jakarta.validation.constraints.NotNull;

public record UpdateMemberRolePayload(
        @NotNull String role
) {}
