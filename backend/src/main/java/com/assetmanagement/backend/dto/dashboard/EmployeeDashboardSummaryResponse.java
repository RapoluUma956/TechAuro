package com.assetmanagement.backend.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EmployeeDashboardSummaryResponse(
        EmployeeProfileDto employee,
        AssignedLaptopDto laptop,
        CpuInfoDto cpu,
        RamInfoDto ram,
        SsdInfoDto ssd,
        BatteryInfoDto battery,
        List<NetworkInfoDto> network,
        List<AccessoryInfoDto> accessories,
        List<LicenseInfoDto> licenses,
        int installedSoftwareCount,
        VulnerabilitySummaryDto vulnerabilities,
        List<NotificationInfoDto> notifications
) {
    public record EmployeeProfileDto(
            Long userId,
            String employeeCode,
            String fullName,
            String email,
            String designation,
            String accountStatus,
            LocalDate dateOfJoining
    ) {}

    public record AssignedLaptopDto(
            Long deviceId,
            String assetTag,
            String serialNumber,
            String deviceName,
            String brand,
            String model,
            String status,
            String connectionStatus,
            String osName,
            LocalDateTime lastSeenAt
    ) {}

    public record CpuInfoDto(
            String cpuModel,
            Integer cores,
            Integer threads,
            Double usagePercent,
            Double temperature
    ) {}

    public record RamInfoDto(
            Double totalGb,
            Double usedGb,
            Double availableGb,
            Double usagePercent,
            String ramType
    ) {}

    public record SsdInfoDto(
            Double totalGb,
            Double usedGb,
            Double availableGb,
            String health
    ) {}

    public record BatteryInfoDto(
            Integer percentage,
            Double healthPercent,
            Boolean charging,
            Integer cycleCount
    ) {}

    public record NetworkInfoDto(
            String interfaceName,
            String macAddress,
            String ipv4Address,
            String connectionStatus
    ) {}

    public record AccessoryInfoDto(
            Long accessoryId,
            String accessoryName,
            String category,
            String brand,
            String model,
            String serialNumber
    ) {}

    public record LicenseInfoDto(
            Long licenseId,
            String softwareName,
            String publisher,
            String licenseType,
            LocalDate expiryDate,
            String status
    ) {}

    public record VulnerabilitySummaryDto(
            long total,
            long critical,
            long high,
            long medium,
            long low,
            List<VulnerabilityItemDto> items
    ) {
        public record VulnerabilityItemDto(
                Long vulnerabilityId,
                String cveId,
                String title,
                String severity,
                String status
        ) {}
    }

    public record NotificationInfoDto(
            Long notificationId,
            String title,
            String message,
            String severity,
            LocalDateTime createdAt
    ) {}
}

