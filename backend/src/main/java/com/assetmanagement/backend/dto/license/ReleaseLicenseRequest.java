package com.assetmanagement.backend.dto.license;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReleaseLicenseRequest(
        @NotNull(message = "Assignment ID is required")
        Long assignmentId,

        LocalDate returnedAt
) {}
