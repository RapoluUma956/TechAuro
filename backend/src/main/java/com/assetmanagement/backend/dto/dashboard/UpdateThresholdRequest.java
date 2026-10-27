package com.assetmanagement.backend.dto.dashboard;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateThresholdRequest(
        @NotNull(message = "CPU warning percentage is required")
        @Min(value = 1, message = "CPU warning percent must be between 1 and 100")
        @Max(value = 100, message = "CPU warning percent must be between 1 and 100")
        Integer cpuWarningPercent,

        @NotNull(message = "RAM warning percentage is required")
        @Min(value = 1, message = "RAM warning percent must be between 1 and 100")
        @Max(value = 100, message = "RAM warning percent must be between 1 and 100")
        Integer ramWarningPercent,

        @NotNull(message = "Disk critical percentage is required")
        @Min(value = 1, message = "Disk critical percent must be between 1 and 100")
        @Max(value = 100, message = "Disk critical percent must be between 1 and 100")
        Integer diskCriticalPercent,

        @NotNull(message = "Offline timeout minutes is required")
        @Min(value = 1, message = "Offline timeout must be at least 1 minute")
        Integer offlineTimeoutMinutes,

        @NotNull(message = "Warranty warning days is required")
        @Min(value = 1, message = "Warranty warning days must be at least 1")
        Integer warrantyWarningDays,

        @NotNull(message = "License warning days is required")
        @Min(value = 1, message = "License warning days must be at least 1")
        Integer licenseWarningDays
) {}
