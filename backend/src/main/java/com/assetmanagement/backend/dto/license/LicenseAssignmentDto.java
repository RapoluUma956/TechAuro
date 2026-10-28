package com.assetmanagement.backend.dto.license;

import java.time.LocalDateTime;

public record LicenseAssignmentDto(
        Long assignmentId,
        Long licenseId,
        Long userId,
        String employeeName,
        String employeeCode,
        LocalDateTime assignedAt,
        LocalDateTime returnedAt,
        String status,
        String notes
) {}
