package com.assetmanagement.backend.dto.license;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AssignLicenseRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        LocalDate assignedAt,

        String notes
) {}
