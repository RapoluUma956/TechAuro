package com.assetmanagement.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {}
