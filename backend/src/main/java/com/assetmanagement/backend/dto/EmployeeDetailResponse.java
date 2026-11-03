package com.assetmanagement.backend.dto;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.DeviceNetwork;
import com.assetmanagement.backend.entity.InstalledSoftware;
import com.assetmanagement.backend.entity.Vulnerability;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EmployeeDetailResponse(
        Long userId,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String phone,
        String designation,
        String accountStatus,
        LocalDate dateOfJoining,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Device device,
        DeviceAssignmentDto currentAssignment,
        List<DeviceAssignmentDto> assignmentHistory,
        List<DeviceNetwork> networkAdapters,
        List<InstalledSoftware> installedSoftware,
        List<Vulnerability> vulnerabilities
) {}

