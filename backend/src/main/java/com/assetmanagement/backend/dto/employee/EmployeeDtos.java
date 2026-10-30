package com.assetmanagement.backend.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EmployeeDtos {

    public record EmployeeMetricsResponse(
            long totalEmployees,
            long activeDevices,
            long inRepair,
            long inactiveDevices
    ) {}

    public record EmployeeListResponse(
            List<EmployeeSummaryDto> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}

    public record EmployeeSummaryDto(
            Long employeeId,
            String employeeCode,
            String firstName,
            String lastName,
            String fullName,
            String email,
            String phone,
            String designation,
            String role,
            String status,
            EmployeeDeviceSummaryDto device
    ) {}

    public record EmployeeDeviceSummaryDto(
            Long deviceId,
            String deviceName,
            String serialNumber,
            String deviceStatus,
            String connectionStatus,
            LocalDate warrantyExpiryDate
    ) {}

    public record AvailableDevicesResponse(
            long totalElements,
            List<AvailableDeviceDto> devices
    ) {}

    public record AvailableDeviceDto(
            Long deviceId,
            String serialNumber,
            String deviceName,
            String manufacturer,
            String model,
            String deviceStatus,
            String connectionStatus,
            String operatingSystem,
            String processor,
            String assignmentStatus
    ) {}

    public record DeviceLookupResponse(
            Long deviceId,
            String serialNumber,
            String deviceName,
            String manufacturer,
            String model,
            String deviceStatus,
            String connectionStatus,
            String assignmentStatus,
            boolean assignable,
            String message
    ) {}

    public record DeviceAssignmentModeRequest(
            String mode, // "SERIAL_NUMBER" | "DEVICE_ID" | "NONE"
            String serialNumber,
            Long deviceId
    ) {}

    public record CreateEmployeeRequest(
            @NotBlank(message = "Employee code is required")
            String employeeCode,
            @NotBlank(message = "First name is required")
            String firstName,
            String lastName,
            @NotBlank(message = "Email is required")
            @Email(message = "Please enter a valid email address")
            String email,
            String phone,
            String designation,
            LocalDate dateOfJoining,
            DeviceAssignmentModeRequest deviceAssignment,
            String serialNumber,
            Long deviceId,
            String password
    ) {}

    public record CreateEmployeeResponse(
            String message,
            EmployeeCreatedDto employee,
            AssignedDeviceSummaryDto assignedDevice
    ) {}

    public record EmployeeCreatedDto(
            Long employeeId,
            String employeeCode,
            String firstName,
            String lastName,
            String email,
            String designation,
            String status
    ) {}

    public record AssignedDeviceSummaryDto(
            Long deviceId,
            String serialNumber,
            String deviceName,
            String assignmentStatus
    ) {}

    public record EmployeeProfileResponse(
            Long employeeId,
            String employeeCode,
            String firstName,
            String lastName,
            String fullName,
            String email,
            String phone,
            String designation,
            String department,
            String location,
            String employmentStatus,
            LocalDate dateOfJoining,
            EmployeeReportsToDto reportsTo,
            EmployeeAssignedDeviceDto assignedDevice,
            EmployeeSummaryCountsDto summary
    ) {}

    public record EmployeeReportsToDto(
            Long employeeId,
            String fullName,
            String designation
    ) {}

    public record EmployeeAssignedDeviceDto(
            Long deviceId,
            String deviceName,
            String serialNumber,
            String deviceStatus,
            String connectionStatus,
            String operatingSystem
    ) {}

    public record EmployeeSummaryCountsDto(
            int deviceCount,
            int accessoryCount,
            int softwareCount,
            int vulnerabilityCount,
            int licenseCount
    ) {}

    public record EmployeeDeviceDetailsResponse(
            Long employeeId,
            StructuredDeviceDto device
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StructuredDeviceDto(
            Long deviceId,
            String deviceName,
            String assetTag,
            String serialNumber,
            String manufacturer,
            String model,
            String deviceStatus,
            String connectionStatus,
            LocalDateTime lastSeenAt,
            LocalDate warrantyExpiryDate,
            LocalDate purchaseDate,
            String hostname,
            DeviceOsDto operatingSystem,
            DeviceCpuDto cpu,
            DeviceMemoryDto memory,
            DeviceStorageDto storage,
            DeviceBatteryDto battery,
            DeviceSecurityDto security,
            List<DeviceNetworkAdapterDto> networkAdapters,
            DeviceAgentDto agent
    ) {}

    public record DeviceOsDto(
            String name,
            String version,
            String architecture
    ) {}

    public record DeviceCpuDto(
            String processorModel,
            Integer cores,
            Integer threads,
            Integer frequencyMHz,
            Double utilizationPercent,
            Double temperatureCelsius
    ) {}

    public record DeviceMemoryDto(
            Double totalGB,
            Double usedGB,
            Double availableGB,
            Double utilizationPercent,
            String type,
            Integer speedMHz
    ) {}

    public record DeviceStorageDto(
            Double totalGB,
            Double usedGB,
            Double availableGB,
            String healthStatus
    ) {}

    public record DeviceBatteryDto(
            Integer percentage,
            Double healthPercent,
            Boolean charging,
            Integer cycleCount
    ) {}

    public record DeviceSecurityDto(
            String bitLockerProtection,
            String encryptionMethod,
            String tpmVersion,
            Boolean secureBootEnabled
    ) {}

    public record DeviceNetworkAdapterDto(
            String name,
            String macAddress,
            String ipv4Address,
            String ipv6Address,
            String interfaceType,
            String status
    ) {}

    public record DeviceAgentDto(
            String status,
            String version,
            LocalDateTime lastTelemetryAt
    ) {}

    public record DeviceHistoryResponse(
            Long deviceId,
            List<DeviceHistoryEventDto> content,
            int page,
            int size,
            long totalElements
    ) {}

    public record DeviceHistoryEventDto(
            Long eventId,
            String eventType,
            LocalDateTime eventDate,
            String employeeCode,
            String employeeName,
            String description,
            String performedBy
    ) {}

    public record EmployeeAccessoriesResponse(
            Long employeeId,
            long totalElements,
            List<EmployeeAccessoryDto> accessories,
            int page,
            int size,
            int totalPages
    ) {}

    public record EmployeeAccessoryDto(
            Long assignmentId,
            Long accessoryId,
            String accessoryName,
            String assetTag,
            String serialNumber,
            String accessoryType,
            String brand,
            String model,
            LocalDate givenDate,
            LocalDate returnDate,
            String assignmentStatus,
            String condition
    ) {}

    public record EmployeeSoftwareResponse(
            Long employeeId,
            Long deviceId,
            long totalElements,
            List<EmployeeSoftwareDto> software,
            int page,
            int size,
            int totalPages
    ) {}

    public record EmployeeSoftwareDto(
            Long softwareId,
            String softwareName,
            String version,
            String publisher,
            LocalDate installDate,
            String installPath,
            String architecture,
            String updateStatus,
            LocalDateTime lastDetectedAt
    ) {}

    public record EmployeeVulnerabilitiesResponse(
            Long employeeId,
            Long deviceId,
            VulnerabilitySummaryDto summary,
            List<EmployeeVulnerabilityDto> vulnerabilities,
            int page,
            int size,
            long totalElements
    ) {}

    public record VulnerabilitySummaryDto(
            int total,
            int critical,
            int high,
            int medium,
            int low
    ) {}

    public record EmployeeVulnerabilityDto(
            Long vulnerabilityId,
            String cveId,
            String severity,
            String priority,
            Double cvssScore,
            String affectedSoftware,
            String affectedVersion,
            String description,
            LocalDateTime detectedAt,
            String remediationStatus,
            String recommendedAction,
            String assignedTo
    ) {}

    public record EmployeeLicensesResponse(
            Long employeeId,
            long totalElements,
            List<EmployeeLicenseDto> licenses,
            int page,
            int size,
            int totalPages
    ) {}

    public record EmployeeLicenseDto(
            Long assignmentId,
            Long licenseId,
            String softwareName,
            String vendor,
            String licenseType,
            String licenseKey,
            String seatTag,
            LocalDate assignedDate,
            LocalDate expiryDate,
            String renewalType,
            String renewalMethod,
            String status,
            String tier
    ) {}

    public record ReassignDeviceRequest(
            String serialNumber,
            Long deviceId,
            String reason
    ) {}

    public record ReassignDeviceResponse(
            String message,
            Long employeeId,
            DeviceMiniDto previousDevice,
            DeviceMiniDto assignedDevice
    ) {}

    public record DeviceMiniDto(
            Long deviceId,
            String serialNumber,
            String deviceStatus
    ) {}

    public record UnassignDeviceResponse(
            String message,
            Long employeeId,
            Long deviceId,
            String assignmentStatus,
            LocalDateTime unassignedAt
    ) {}
}

