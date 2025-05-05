package com.toddler.controller.payload;

public record UserProfileUpdateRequest(
        String firstName,
        String lastName,
        String username,
        String email,
        String password
) {}
