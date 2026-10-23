package com.assetmanagement.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Please enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}
