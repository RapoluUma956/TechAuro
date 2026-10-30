package com.assetmanagement.backend.dto;

import java.time.LocalDateTime;

public record DeviceAssignmentDto(
        Long assignmentId,
        Long deviceId,
        String serialNumber,
        String deviceName,
        Long userId,
        String employeeCode,
        String employeeName,
        LocalDateTime assignedAt,
        LocalDateTime returnedAt,
        String status
) {}

