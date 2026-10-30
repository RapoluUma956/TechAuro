package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.employee.EmployeeDtos.*;
import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final AccessoryAssignmentRepository accessoryAssignmentRepository;
    private final LicenseAssignmentRepository licenseAssignmentRepository;
    private final DeviceNetworkRepository deviceNetworkRepository;
    private final InstalledSoftwareRepository installedSoftwareRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            UserRepository userRepository,
            DeviceRepository deviceRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            AccessoryAssignmentRepository accessoryAssignmentRepository,
            LicenseAssignmentRepository licenseAssignmentRepository,
            DeviceNetworkRepository deviceNetworkRepository,
            InstalledSoftwareRepository installedSoftwareRepository,
            VulnerabilityRepository vulnerabilityRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.accessoryAssignmentRepository = accessoryAssignmentRepository;
        this.licenseAssignmentRepository = licenseAssignmentRepository;
        this.deviceNetworkRepository = deviceNetworkRepository;
        this.installedSoftwareRepository = installedSoftwareRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static Double toDouble(BigDecimal val) {
        return val != null ? Double.valueOf(val.doubleValue()) : null;
    }

    private static Integer toInteger(BigDecimal val) {
        return val != null ? Integer.valueOf(val.intValue()) : null;
    }

    private static Integer toInteger(Long val) {
        return val != null ? Integer.valueOf(val.intValue()) : null;
    }

    private static Integer toInteger(Integer val) {
        return val;
    }

    // =========================================================
    // 1. EMPLOYEE METRICS
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeMetricsResponse getEmployeeMetrics() {
        long totalEmployees = userRepository.count();

        List<Device> allDevices = deviceRepository.findAll();
        long activeDevices = allDevices.stream()
                .filter(d -> "ONLINE".equalsIgnoreCase(d.getConnectionStatus()) || "ACTIVE".equalsIgnoreCase(d.getDeviceStatus()))
                .count();

        long inRepair = allDevices.stream()
                .filter(d -> "REPAIR".equalsIgnoreCase(d.getDeviceStatus()) || "IN_REPAIR".equalsIgnoreCase(d.getDeviceStatus()))
                .count();

        long inactiveDevices = allDevices.stream()
                .filter(d -> "OFFLINE".equalsIgnoreCase(d.getConnectionStatus()) || "INACTIVE".equalsIgnoreCase(d.getDeviceStatus()))
                .count();

        return new EmployeeMetricsResponse(totalEmployees, activeDevices, inRepair, inactiveDevices);
    }

    // =========================================================
    // 2. EMPLOYEE LIST WITH SEARCH, FILTERING, PAGINATION
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeListResponse getEmployees(
            String search,
            String status,
            String role,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        List<User> users = userRepository.findAll();
        Map<Long, DeviceAssignment> activeAssignmentsByUser = deviceAssignmentRepository.findByStatus("ACTIVE")
                .stream()
                .filter(da -> da.getUser() != null)
                .collect(Collectors.toMap(
                        da -> da.getUser().getUserId(),
                        da -> da,
                        (existing, replacement) -> existing
                ));

        // Map devices by currentUserId as backup
        Map<Long, Device> devicesByUserId = deviceRepository.findAll().stream()
                .filter(d -> d.getCurrentUserId() != null)
                .collect(Collectors.toMap(Device::getCurrentUserId, d -> d, (e, r) -> e));

        List<EmployeeSummaryDto> list = new ArrayList<>();
        for (User user : users) {
            DeviceAssignment activeDa = activeAssignmentsByUser.get(user.getUserId());
            Device device = activeDa != null ? activeDa.getDevice() : devicesByUserId.get(user.getUserId());

            String fullName = java.util.stream.Stream.of(user.getFirstName(), user.getLastName())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "))
                    .trim();
            if (fullName.isBlank()) {
                fullName = user.getFirstName() != null ? user.getFirstName() : "Employee " + user.getUserId();
            }

            EmployeeDeviceSummaryDto deviceDto = null;
            if (device != null) {
                String devName = device.getDeviceName() != null && !device.getDeviceName().isBlank()
                        ? device.getDeviceName()
                        : ((device.getBrand() != null ? device.getBrand() : "") + " " + (device.getModel() != null ? device.getModel() : "Laptop")).trim();

                deviceDto = new EmployeeDeviceSummaryDto(
                        device.getDeviceId(),
                        devName,
                        device.getSerialNumber(),
                        device.getDeviceStatus() != null ? device.getDeviceStatus() : "ACTIVE",
                        device.getConnectionStatus() != null ? device.getConnectionStatus() : "OFFLINE",
                        device.getWarrantyEndDate()
                );
            }

            EmployeeSummaryDto dto = new EmployeeSummaryDto(
                    user.getUserId(),
                    user.getEmployeeCode(),
                    user.getFirstName(),
                    user.getLastName(),
                    fullName,
                    user.getEmail(),
                    user.getPhone(),
                    user.getDesignation() != null ? user.getDesignation() : "Employee",
                    user.getDesignation() != null ? user.getDesignation() : "Employee",
                    user.getAccountStatus() != null ? user.getAccountStatus() : "ACTIVE",
                    deviceDto
            );
            list.add(dto);
        }

        // Apply Status Filter
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(e -> status.equalsIgnoreCase(e.status()))
                    .collect(Collectors.toList());
        }

        // Apply Role Filter
        if (role != null && !role.isBlank() && !"ALL".equalsIgnoreCase(role)) {
            list = list.stream()
                    .filter(e -> role.equalsIgnoreCase(e.role()) || role.equalsIgnoreCase(e.designation()))
                    .collect(Collectors.toList());
        }

        // Apply Search Filter
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            list = list.stream().filter(e -> {
                boolean matchesName = e.fullName() != null && e.fullName().toLowerCase().contains(q);
                boolean matchesCode = e.employeeCode() != null && e.employeeCode().toLowerCase().contains(q);
                boolean matchesEmail = e.email() != null && e.email().toLowerCase().contains(q);
                boolean matchesRole = e.role() != null && e.role().toLowerCase().contains(q);
                boolean matchesDevice = e.device() != null && (
                        (e.device().deviceName() != null && e.device().deviceName().toLowerCase().contains(q)) ||
                        (e.device().serialNumber() != null && e.device().serialNumber().toLowerCase().contains(q))
                );
                return matchesName || matchesCode || matchesEmail || matchesRole || matchesDevice;
            }).collect(Collectors.toList());
        }

        // Sorting
        Comparator<EmployeeSummaryDto> comparator = Comparator.comparing(
                e -> e.employeeCode() != null ? e.employeeCode() : "",
                String.CASE_INSENSITIVE_ORDER
        );
        if ("firstName".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(e -> e.firstName() != null ? e.firstName() : "", String.CASE_INSENSITIVE_ORDER);
        } else if ("fullName".equalsIgnoreCase(sortBy) || "employeeName".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(e -> e.fullName() != null ? e.fullName() : "", String.CASE_INSENSITIVE_ORDER);
        } else if ("email".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(e -> e.email() != null ? e.email() : "", String.CASE_INSENSITIVE_ORDER);
        } else if ("designation".equalsIgnoreCase(sortBy) || "role".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(e -> e.role() != null ? e.role() : "", String.CASE_INSENSITIVE_ORDER);
        }
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        list.sort(comparator);

        long totalElements = list.size();
        int safePage = Math.max(0, page);
        int safeSize = size > 0 ? size : 10;
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = safePage * safeSize;
        List<EmployeeSummaryDto> pagedContent;
        if (fromIndex >= totalElements) {
            pagedContent = Collections.emptyList();
        } else {
            int toIndex = Math.min(fromIndex + safeSize, (int) totalElements);
            pagedContent = list.subList(fromIndex, toIndex);
        }

        boolean last = (safePage + 1) >= totalPages;
        return new EmployeeListResponse(pagedContent, safePage, safeSize, totalElements, totalPages, last);
    }

    // =========================================================
    // 3. AVAILABLE DEVICES FOR ASSIGNMENT
    // =========================================================
    @Transactional(readOnly = true)
    public AvailableDevicesResponse getAvailableDevices() {
        Set<Long> activelyAssignedDeviceIds = deviceAssignmentRepository.findByStatus("ACTIVE")
                .stream()
                .filter(da -> da.getDevice() != null)
                .map(da -> da.getDevice().getDeviceId())
                .collect(Collectors.toSet());

        List<AvailableDeviceDto> available = deviceRepository.findAll().stream()
                .filter(d -> d.getCurrentUserId() == null && !activelyAssignedDeviceIds.contains(d.getDeviceId()))
                .filter(d -> !"REPAIR".equalsIgnoreCase(d.getDeviceStatus()) && !"RETIRED".equalsIgnoreCase(d.getDeviceStatus()))
                .map(d -> {
                    String devName = d.getDeviceName() != null && !d.getDeviceName().isBlank()
                            ? d.getDeviceName()
                            : ((d.getBrand() != null ? d.getBrand() : "") + " " + (d.getModel() != null ? d.getModel() : "Laptop")).trim();

                    String os = d.getOsName() != null ? d.getOsName() : "Windows 11";
                    String cpu = d.getCpu() != null ? d.getCpu() : (d.getCpuName() != null ? d.getCpuName() : "Intel Core i7");

                    return new AvailableDeviceDto(
                            d.getDeviceId(),
                            d.getSerialNumber(),
                            devName,
                            d.getBrand() != null ? d.getBrand() : "Unknown",
                            d.getModel() != null ? d.getModel() : "Laptop",
                            d.getDeviceStatus() != null ? d.getDeviceStatus() : "ACTIVE",
                            d.getConnectionStatus() != null ? d.getConnectionStatus() : "OFFLINE",
                            os,
                            cpu,
                            "UNASSIGNED"
                    );
                })
                .toList();

        return new AvailableDevicesResponse(available.size(), available);
    }

    // =========================================================
    // 4. DEVICE SERIAL LOOKUP & ASSIGNABILITY VALIDATION
    // =========================================================
    @Transactional(readOnly = true)
    public DeviceLookupResponse getDeviceBySerial(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serial number cannot be blank");
        }

        String cleanSerial = serialNumber.trim();
        Device device = deviceRepository.findBySerialNumberIgnoreCase(cleanSerial)
                .orElseGet(() -> deviceRepository.findBySerialNumber(cleanSerial)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No registered device found with this serial number: " + cleanSerial)));

        String devName = device.getDeviceName() != null && !device.getDeviceName().isBlank()
                ? device.getDeviceName()
                : ((device.getBrand() != null ? device.getBrand() : "") + " " + (device.getModel() != null ? device.getModel() : "Laptop")).trim();

        boolean isAssigned = device.getCurrentUserId() != null ||
                deviceAssignmentRepository.findBySerialNumberIgnoreCaseAndStatus(cleanSerial, "ACTIVE").isPresent();

        boolean isRepair = "REPAIR".equalsIgnoreCase(device.getDeviceStatus()) || "IN_REPAIR".equalsIgnoreCase(device.getDeviceStatus());
        boolean isRetired = "RETIRED".equalsIgnoreCase(device.getDeviceStatus());

        boolean assignable = !isAssigned && !isRepair && !isRetired;
        String assignmentStatus = isAssigned ? "ASSIGNED" : "UNASSIGNED";

        String message;
        if (isAssigned) {
            message = "Device is already assigned to an employee";
        } else if (isRepair) {
            message = "Device is currently in repair and cannot be assigned";
        } else if (isRetired) {
            message = "Device is retired and cannot be assigned";
        } else {
            message = "Device is available for assignment";
        }

        return new DeviceLookupResponse(
                device.getDeviceId(),
                device.getSerialNumber(),
                devName,
                device.getBrand() != null ? device.getBrand() : "OEM",
                device.getModel() != null ? device.getModel() : "Workstation",
                device.getDeviceStatus() != null ? device.getDeviceStatus() : "ACTIVE",
                device.getConnectionStatus() != null ? device.getConnectionStatus() : "OFFLINE",
                assignmentStatus,
                assignable,
                message
        );
    }

    // =========================================================
    // 5. CREATE EMPLOYEE
    // =========================================================
    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (userRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists: " + request.employeeCode());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email address already exists: " + request.email());
        }

        User user = new User();
        user.setEmployeeCode(request.employeeCode().trim());
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName() != null ? request.lastName().trim() : "");
        user.setEmail(request.email().trim());
        user.setPhone(request.phone() != null ? request.phone().trim() : "");
        user.setDesignation(request.designation() != null ? request.designation().trim() : "Software Engineer");
        user.setDateOfJoining(request.dateOfJoining() != null ? request.dateOfJoining() : LocalDate.now());
        user.setAccountStatus("ACTIVE");
        user.setMustChangePassword(true);

        String rawPassword = (request.password() != null && !request.password().isBlank()) ? request.password() : "Password@123";
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        User savedUser = userRepository.save(user);

        // Resolve device assignment mode
        String mode = "NONE";
        String targetSerial = null;
        Long targetDeviceId = null;

        if (request.deviceAssignment() != null) {
            mode = request.deviceAssignment().mode() != null ? request.deviceAssignment().mode().toUpperCase() : "NONE";
            targetSerial = request.deviceAssignment().serialNumber();
            targetDeviceId = request.deviceAssignment().deviceId();
        } else if (request.serialNumber() != null && !request.serialNumber().isBlank()) {
            mode = "SERIAL_NUMBER";
            targetSerial = request.serialNumber();
        } else if (request.deviceId() != null) {
            mode = "DEVICE_ID";
            targetDeviceId = request.deviceId();
        }

        AssignedDeviceSummaryDto assignedDeviceDto = null;

        final String finalSerial = targetSerial;
        final Long finalDeviceId = targetDeviceId;

        if (!"NONE".equalsIgnoreCase(mode)) {
            Device device = null;
            if ("SERIAL_NUMBER".equalsIgnoreCase(mode) && finalSerial != null && !finalSerial.isBlank()) {
                device = deviceRepository.findBySerialNumberIgnoreCase(finalSerial.trim())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Device not found with serial number: " + finalSerial));
            } else if (finalDeviceId != null) {
                device = deviceRepository.findById(finalDeviceId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Device not found with ID: " + finalDeviceId));
            }

            if (device != null) {
                // Check if device is already actively assigned
                boolean isAlreadyAssigned = device.getCurrentUserId() != null ||
                        deviceAssignmentRepository.findBySerialNumberIgnoreCaseAndStatus(device.getSerialNumber(), "ACTIVE").isPresent();
                if (isAlreadyAssigned) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Device " + device.getSerialNumber() + " is already assigned to another user");
                }

                // Check lifecycle status
                if ("REPAIR".equalsIgnoreCase(device.getDeviceStatus()) || "RETIRED".equalsIgnoreCase(device.getDeviceStatus())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device " + device.getSerialNumber() + " is not available for assignment (status: " + device.getDeviceStatus() + ")");
                }

                // Assign
                device.setCurrentUserId(savedUser.getUserId());
                device.setDeviceStatus("ACTIVE");
                device = deviceRepository.save(device);

                DeviceAssignment da = new DeviceAssignment();
                da.setDevice(device);
                da.setUser(savedUser);
                da.setSerialNumber(device.getSerialNumber());
                da.setAssignedAt(LocalDateTime.now());
                da.setStatus("ACTIVE");
                deviceAssignmentRepository.save(da);

                String devName = device.getDeviceName() != null ? device.getDeviceName() : (device.getBrand() + " " + device.getModel());
                assignedDeviceDto = new AssignedDeviceSummaryDto(
                        device.getDeviceId(),
                        device.getSerialNumber(),
                        devName,
                        "ASSIGNED"
                );
            }
        }

        EmployeeCreatedDto empDto = new EmployeeCreatedDto(
                savedUser.getUserId(),
                savedUser.getEmployeeCode(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getDesignation(),
                savedUser.getAccountStatus()
        );

        return new CreateEmployeeResponse("Employee created successfully", empDto, assignedDeviceDto);
    }

    // =========================================================
    // 6. EMPLOYEE PROFILE DETAILS
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeProfileResponse getEmployeeProfile(Long employeeId) {
        User user = getUserOrThrow(employeeId);

        DeviceAssignment activeDa = deviceAssignmentRepository.findByUser_UserIdAndStatus(user.getUserId(), "ACTIVE")
                .orElse(null);

        Device device = activeDa != null ? activeDa.getDevice() : deviceRepository.findByCurrentUserId(user.getUserId()).orElse(null);

        EmployeeAssignedDeviceDto assignedDeviceDto = null;
        if (device != null) {
            String devName = device.getDeviceName() != null && !device.getDeviceName().isBlank()
                    ? device.getDeviceName()
                    : ((device.getBrand() != null ? device.getBrand() : "") + " " + (device.getModel() != null ? device.getModel() : "Laptop")).trim();

            String os = device.getOsName();

            assignedDeviceDto = new EmployeeAssignedDeviceDto(
                    device.getDeviceId(),
                    devName,
                    device.getSerialNumber(),
                    device.getDeviceStatus() != null ? device.getDeviceStatus() : "ACTIVE",
                    device.getConnectionStatus() != null ? device.getConnectionStatus() : "OFFLINE",
                    os
            );
        }

        // Summary counts
        int deviceCount = device != null ? 1 : 0;
        int accessoryCount = (int) accessoryAssignmentRepository.findByUser_UserId(user.getUserId())
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
        int softwareCount = device != null ? installedSoftwareRepository.findByDevice_DeviceId(device.getDeviceId()).size() : 0;
        int vulnerabilityCount = device != null ? vulnerabilityRepository.findByDevice_DeviceId(device.getDeviceId()).size() : 0;
        int licenseCount = (int) licenseAssignmentRepository.findByUser_UserId(user.getUserId())
                .stream().filter(la -> "ACTIVE".equalsIgnoreCase(la.getStatus())).count();

        EmployeeSummaryCountsDto summaryCounts = new EmployeeSummaryCountsDto(
                deviceCount, accessoryCount, softwareCount, vulnerabilityCount, licenseCount
        );

        String fullName = java.util.stream.Stream.of(user.getFirstName(), user.getLastName())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
        if (fullName.isBlank()) {
            fullName = user.getFirstName();
        }

        EmployeeReportsToDto reportsTo = new EmployeeReportsToDto(
                1L,
                "IT Director",
                "Director of Infrastructure"
        );

        return new EmployeeProfileResponse(
                user.getUserId(),
                user.getEmployeeCode(),
                user.getFirstName(),
                user.getLastName(),
                fullName,
                user.getEmail(),
                user.getPhone() != null ? user.getPhone() : "+1 (555) 010-0000",
                user.getDesignation() != null ? user.getDesignation() : "Software Engineer",
                "Enterprise Technology & Engineering",
                "Corporate Headquarters",
                user.getAccountStatus() != null ? user.getAccountStatus() : "ACTIVE",
                user.getDateOfJoining() != null ? user.getDateOfJoining() : LocalDate.of(2023, 11, 18),
                reportsTo,
                assignedDeviceDto,
                summaryCounts
        );
    }

    // =========================================================
    // 7. EMPLOYEE DEVICE DETAILS TAB
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeDeviceDetailsResponse getEmployeeDevice(Long employeeId) {
        User user = getUserOrThrow(employeeId);

        DeviceAssignment activeDa = deviceAssignmentRepository.findByUser_UserIdAndStatus(user.getUserId(), "ACTIVE")
                .orElse(null);
        Device device = activeDa != null ? activeDa.getDevice() : deviceRepository.findByCurrentUserId(user.getUserId()).orElse(null);

        if (device == null) {
            return new EmployeeDeviceDetailsResponse(user.getUserId(), null);
        }

        // Build Structured Device DTO
        String devName = device.getDeviceName() != null && !device.getDeviceName().isBlank()
                ? device.getDeviceName()
                : ((device.getBrand() != null ? device.getBrand() : "") + " " + (device.getModel() != null ? device.getModel() : "Laptop")).trim();

        DeviceOsDto osDto = (device.getOsName() != null || device.getOsVersion() != null)
                ? new DeviceOsDto(device.getOsName(), device.getOsVersion(), device.getOsArchitecture())
                : null;

        DeviceCpuDto cpuDto = (device.getCpuName() != null || device.getCpu() != null || device.getCpuCores() != null)
                ? new DeviceCpuDto(
                        device.getCpuName() != null ? device.getCpuName() : device.getCpu(),
                        device.getCpuCores(),
                        device.getCpuThreads(),
                        toInteger(device.getCpuFrequencyMhz()),
                        toDouble(device.getCpuUsagePercent()),
                        toDouble(device.getCpuTemperature())
                )
                : null;

        BigDecimal rawMem = device.getRamTotalGb() != null ? device.getRamTotalGb() : device.getRamGb();
        Double memTotal = toDouble(rawMem);
        DeviceMemoryDto memDto = memTotal != null
                ? new DeviceMemoryDto(
                        memTotal,
                        toDouble(device.getRamUsedGb()),
                        toDouble(device.getRamAvailableGb()),
                        toDouble(device.getRamUsagePercent()),
                        device.getRamType(),
                        toInteger(device.getRamSpeedMhz())
                )
                : null;

        BigDecimal rawStorage = device.getStorageTotalGb() != null ? device.getStorageTotalGb() : device.getSsdCapacityGb();
        Double storageTotal = toDouble(rawStorage);
        DeviceStorageDto storageDto = storageTotal != null
                ? new DeviceStorageDto(
                        storageTotal,
                        toDouble(device.getStorageUsedGb() != null ? device.getStorageUsedGb() : device.getSsdUsedGb()),
                        toDouble(device.getStorageAvailableGb() != null ? device.getStorageAvailableGb() : device.getSsdAvailableGb()),
                        device.getStorageHealth() != null ? device.getStorageHealth() : device.getSsdHealth()
                )
                : null;

        DeviceBatteryDto batteryDto = device.getBatteryPercentage() != null
                ? new DeviceBatteryDto(
                        toInteger(device.getBatteryPercentage()),
                        toDouble(device.getBatteryHealthPercent()),
                        device.getBatteryCharging(),
                        device.getBatteryCycleCount()
                )
                : null;

        DeviceSecurityDto secDto = (device.getBitlockerEnabled() != null || device.getTpmVersion() != null)
                ? new DeviceSecurityDto(
                        device.getBitlockerEnabled() != null ? (Boolean.TRUE.equals(device.getBitlockerEnabled()) ? "ON" : "OFF") : null,
                        device.getBitlockerEncryptionMethod(),
                        device.getTpmVersion(),
                        device.getSecureBootEnabled()
                )
                : null;

        List<DeviceNetworkAdapterDto> netAdapters = deviceNetworkRepository.findByDevice_DeviceId(device.getDeviceId())
                .stream()
                .map(n -> new DeviceNetworkAdapterDto(
                        n.getInterfaceName() != null ? n.getInterfaceName() : "Network Adapter",
                        n.getMacAddress() != null ? n.getMacAddress() : "—",
                        n.getIpv4Address() != null ? n.getIpv4Address() : "—",
                        n.getIpv6Address() != null ? n.getIpv6Address() : "—",
                        n.getInterfaceType() != null ? n.getInterfaceType() : "Physical",
                        n.getConnectionStatus() != null ? n.getConnectionStatus() : "OFFLINE"
                ))
                .toList();

        DeviceAgentDto agentDto = new DeviceAgentDto(
                device.getConnectionStatus() != null ? device.getConnectionStatus() : "OFFLINE",
                device.getAgentVersion(),
                device.getLastSeenAt()
        );

        StructuredDeviceDto structured = new StructuredDeviceDto(
                device.getDeviceId(),
                devName,
                device.getAssetTag(),
                device.getSerialNumber(),
                device.getBrand(),
                device.getModel(),
                device.getDeviceStatus() != null ? device.getDeviceStatus() : "ACTIVE",
                device.getConnectionStatus() != null ? device.getConnectionStatus() : "OFFLINE",
                device.getLastSeenAt(),
                device.getWarrantyEndDate(),
                device.getPurchaseDate(),
                device.getHostname(),
                osDto,
                cpuDto,
                memDto,
                storageDto,
                batteryDto,
                secDto,
                netAdapters,
                agentDto
        );

        return new EmployeeDeviceDetailsResponse(user.getUserId(), structured);
    }

    // =========================================================
    // 8. DEVICE HISTORY
    // =========================================================
    @Transactional(readOnly = true)
    public DeviceHistoryResponse getDeviceHistory(Long deviceId, int page, int size) {
        List<DeviceAssignment> assignments = deviceAssignmentRepository.findByDevice_DeviceIdOrderByAssignedAtDesc(deviceId);

        List<DeviceHistoryEventDto> content = assignments.stream().map(a -> {
            String empCode = a.getUser() != null ? a.getUser().getEmployeeCode() : "N/A";
            String empName = a.getUser() != null
                    ? ((a.getUser().getFirstName() != null ? a.getUser().getFirstName() : "") + " " + (a.getUser().getLastName() != null ? a.getUser().getLastName() : "")).trim()
                    : "Employee";

            String eventType = "ACTIVE".equalsIgnoreCase(a.getStatus()) ? "DEVICE_ASSIGNED" : "DEVICE_RETURNED";
            String desc = "ACTIVE".equalsIgnoreCase(a.getStatus()) ? "Device assigned to employee" : "Device unassigned / returned to inventory";

            return new DeviceHistoryEventDto(
                    a.getAssignmentId(),
                    eventType,
                    a.getAssignedAt(),
                    empCode,
                    empName,
                    desc,
                    "IT Administrator"
            );
        }).toList();

        return new DeviceHistoryResponse(deviceId, content, Math.max(0, page), size > 0 ? size : 10, content.size());
    }

    // =========================================================
    // 9. EMPLOYEE ACCESSORIES
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeAccessoriesResponse getEmployeeAccessories(
            Long employeeId,
            String search,
            String type,
            String status,
            int page,
            int size) {

        User user = getUserOrThrow(employeeId);
        List<AccessoryAssignment> assignments = accessoryAssignmentRepository.findByUser_UserId(user.getUserId());

        List<EmployeeAccessoryDto> list = assignments.stream().map(a -> {
            Accessory acc = a.getAccessory();
            String name = acc != null ? (acc.getAccessoryName() != null ? acc.getAccessoryName() : acc.getProductName()) : "Accessory";
            String accType = acc != null ? (acc.getCategory() != null ? acc.getCategory().toUpperCase() : "PERIPHERAL") : "PERIPHERAL";
            String brand = acc != null ? acc.getBrand() : "Generic";
            String model = acc != null ? acc.getModel() : "Standard";

            LocalDate givenDate = a.getAssignedAt() != null ? a.getAssignedAt().toLocalDate() : LocalDate.now();
            LocalDate returnDate = a.getReturnedAt() != null ? a.getReturnedAt().toLocalDate() : null;

            return new EmployeeAccessoryDto(
                    a.getAssignmentId(),
                    acc != null ? acc.getAccessoryId() : null,
                    name,
                    "ACC-" + a.getAssignmentId(),
                    "SN-ACC-" + a.getAssignmentId(),
                    accType,
                    brand,
                    model,
                    givenDate,
                    returnDate,
                    a.getStatus() != null ? a.getStatus() : "ASSIGNED",
                    "GOOD"
            );
        }).toList();

        // Filters
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            list = list.stream().filter(item -> status.equalsIgnoreCase(item.assignmentStatus())).toList();
        }
        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
            list = list.stream().filter(item -> type.equalsIgnoreCase(item.accessoryType())).toList();
        }
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            list = list.stream().filter(item ->
                    (item.accessoryName() != null && item.accessoryName().toLowerCase().contains(q)) ||
                    (item.brand() != null && item.brand().toLowerCase().contains(q)) ||
                    (item.model() != null && item.model().toLowerCase().contains(q)) ||
                    (item.serialNumber() != null && item.serialNumber().toLowerCase().contains(q))
            ).toList();
        }

        int totalElements = list.size();
        int safePage = Math.max(0, page);
        int safeSize = size > 0 ? size : 10;
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = safePage * safeSize;
        List<EmployeeAccessoryDto> paged = fromIndex >= totalElements ? Collections.emptyList() : list.subList(fromIndex, Math.min(fromIndex + safeSize, totalElements));

        return new EmployeeAccessoriesResponse(user.getUserId(), totalElements, paged, safePage, safeSize, totalPages);
    }

    // =========================================================
    // 10. EMPLOYEE SOFTWARE
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeSoftwareResponse getEmployeeSoftware(
            Long employeeId,
            String search,
            String publisher,
            String updateStatus,
            int page,
            int size) {

        User user = getUserOrThrow(employeeId);
        DeviceAssignment activeDa = deviceAssignmentRepository.findByUser_UserIdAndStatus(user.getUserId(), "ACTIVE").orElse(null);
        Device device = activeDa != null ? activeDa.getDevice() : deviceRepository.findByCurrentUserId(user.getUserId()).orElse(null);

        if (device == null) {
            return new EmployeeSoftwareResponse(user.getUserId(), null, 0, Collections.emptyList(), page, size, 1);
        }

        List<InstalledSoftware> softwareList = installedSoftwareRepository.findByDevice_DeviceId(device.getDeviceId());

        List<EmployeeSoftwareDto> dtos = softwareList.stream().map(sw -> new EmployeeSoftwareDto(
                sw.getSoftwareId(),
                sw.getSoftwareName(),
                sw.getVersion() != null ? sw.getVersion() : "1.0",
                sw.getPublisher() != null ? sw.getPublisher() : "Unknown",
                sw.getInstallDate(),
                sw.getInstallLocation() != null ? sw.getInstallLocation() : "C:\\Program Files",
                "x64",
                "UP_TO_DATE",
                sw.getLastSeenAt() != null ? sw.getLastSeenAt() : (sw.getDetectedAt() != null ? sw.getDetectedAt() : LocalDateTime.now())
        )).toList();

        // Filters
        if (publisher != null && !publisher.isBlank() && !"ALL".equalsIgnoreCase(publisher)) {
            dtos = dtos.stream().filter(s -> publisher.equalsIgnoreCase(s.publisher())).toList();
        }
        if (updateStatus != null && !updateStatus.isBlank() && !"ALL".equalsIgnoreCase(updateStatus)) {
            dtos = dtos.stream().filter(s -> updateStatus.equalsIgnoreCase(s.updateStatus())).toList();
        }
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            dtos = dtos.stream().filter(s ->
                    (s.softwareName() != null && s.softwareName().toLowerCase().contains(q)) ||
                    (s.publisher() != null && s.publisher().toLowerCase().contains(q)) ||
                    (s.installPath() != null && s.installPath().toLowerCase().contains(q))
            ).toList();
        }

        int totalElements = dtos.size();
        int safePage = Math.max(0, page);
        int safeSize = size > 0 ? size : 10;
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = safePage * safeSize;
        List<EmployeeSoftwareDto> paged = fromIndex >= totalElements ? Collections.emptyList() : dtos.subList(fromIndex, Math.min(fromIndex + safeSize, totalElements));

        return new EmployeeSoftwareResponse(user.getUserId(), device.getDeviceId(), totalElements, paged, safePage, safeSize, totalPages);
    }

    // =========================================================
    // 11. EMPLOYEE VULNERABILITIES
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeVulnerabilitiesResponse getEmployeeVulnerabilities(
            Long employeeId,
            String severity,
            String priority,
            String status,
            String search,
            int page,
            int size) {

        User user = getUserOrThrow(employeeId);
        DeviceAssignment activeDa = deviceAssignmentRepository.findByUser_UserIdAndStatus(user.getUserId(), "ACTIVE").orElse(null);
        Device device = activeDa != null ? activeDa.getDevice() : deviceRepository.findByCurrentUserId(user.getUserId()).orElse(null);

        if (device == null) {
            VulnerabilitySummaryDto emptySummary = new VulnerabilitySummaryDto(0, 0, 0, 0, 0);
            return new EmployeeVulnerabilitiesResponse(user.getUserId(), null, emptySummary, Collections.emptyList(), page, size, 0);
        }

        List<Vulnerability> allVulns = vulnerabilityRepository.findByDevice_DeviceId(device.getDeviceId());

        int total = allVulns.size();
        int critical = (int) allVulns.stream().filter(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity())).count();
        int high = (int) allVulns.stream().filter(v -> "HIGH".equalsIgnoreCase(v.getSeverity())).count();
        int medium = (int) allVulns.stream().filter(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity())).count();
        int low = (int) allVulns.stream().filter(v -> "LOW".equalsIgnoreCase(v.getSeverity())).count();

        VulnerabilitySummaryDto summary = new VulnerabilitySummaryDto(total, critical, high, medium, low);

        List<EmployeeVulnerabilityDto> list = allVulns.stream().map(v -> {
            String sev = v.getSeverity() != null ? v.getSeverity().toUpperCase() : "MEDIUM";
            String pri = "CRITICAL".equals(sev) ? "P1" : ("HIGH".equals(sev) ? "P2" : ("MEDIUM".equals(sev) ? "P3" : "P4"));
            Double score = "CRITICAL".equals(sev) ? 9.5 : ("HIGH".equals(sev) ? 8.2 : 5.4);

            return new EmployeeVulnerabilityDto(
                    v.getVulnerabilityId(),
                    v.getCveId() != null ? v.getCveId() : ("CVE-" + v.getVulnerabilityId()),
                    sev,
                    pri,
                    score,
                    v.getAffectedSoftware() != null ? v.getAffectedSoftware() : "System Package",
                    "Current",
                    v.getDescription() != null ? v.getDescription() : "Vendor security notice",
                    v.getDetectedAt() != null ? v.getDetectedAt() : LocalDateTime.now(),
                    v.getStatus() != null ? v.getStatus() : "PATCH_AVAILABLE",
                    "Upgrade to the latest security patch recommended by the vendor",
                    "IT Security Team"
            );
        }).toList();

        // Filters
        if (severity != null && !severity.isBlank() && !"ALL".equalsIgnoreCase(severity)) {
            list = list.stream().filter(v -> severity.equalsIgnoreCase(v.severity())).toList();
        }
        if (priority != null && !priority.isBlank() && !"ALL".equalsIgnoreCase(priority)) {
            list = list.stream().filter(v -> priority.equalsIgnoreCase(v.priority())).toList();
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            list = list.stream().filter(v -> status.equalsIgnoreCase(v.remediationStatus())).toList();
        }
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            list = list.stream().filter(v ->
                    (v.cveId() != null && v.cveId().toLowerCase().contains(q)) ||
                    (v.affectedSoftware() != null && v.affectedSoftware().toLowerCase().contains(q)) ||
                    (v.description() != null && v.description().toLowerCase().contains(q))
            ).toList();
        }

        int totalElements = list.size();
        int safePage = Math.max(0, page);
        int safeSize = size > 0 ? size : 10;
        int fromIndex = safePage * safeSize;
        List<EmployeeVulnerabilityDto> paged = fromIndex >= totalElements ? Collections.emptyList() : list.subList(fromIndex, Math.min(fromIndex + safeSize, totalElements));

        return new EmployeeVulnerabilitiesResponse(user.getUserId(), device.getDeviceId(), summary, paged, safePage, safeSize, totalElements);
    }

    // =========================================================
    // 12. EMPLOYEE LICENSES
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeLicensesResponse getEmployeeLicenses(
            Long employeeId,
            String search,
            String status,
            int page,
            int size) {

        User user = getUserOrThrow(employeeId);
        List<LicenseAssignment> assignments = licenseAssignmentRepository.findByUser_UserId(user.getUserId());

        List<EmployeeLicenseDto> list = assignments.stream().map(la -> {
            License lic = la.getLicense();
            String name = lic != null ? (lic.getSoftwareName() != null ? lic.getSoftwareName() : lic.getLicenseName()) : "Software License";
            String vendor = lic != null ? (lic.getPublisher() != null ? lic.getPublisher() : "Vendor") : "Vendor";
            String type = lic != null ? (lic.getLicenseType() != null ? lic.getLicenseType() : "SUBSCRIPTION") : "SUBSCRIPTION";

            LocalDate assignedDate = la.getAssignedAt() != null ? la.getAssignedAt().toLocalDate() : LocalDate.now();
            LocalDate expiryDate = lic != null ? (lic.getExpiryDate() != null ? lic.getExpiryDate() : lic.getWarrantyEndDate()) : null;

            return new EmployeeLicenseDto(
                    la.getAssignmentId(),
                    lic != null ? lic.getLicenseId() : null,
                    name,
                    vendor,
                    type,
                    null, // Do not expose license keys
                    "SEAT-" + la.getAssignmentId(),
                    assignedDate,
                    expiryDate,
                    lic != null && lic.getRenewalType() != null ? lic.getRenewalType() : "AUTOMATIC",
                    "VENDOR_BILLING",
                    la.getStatus() != null ? la.getStatus() : "ACTIVE",
                    "ENTERPRISE"
            );
        }).toList();

        // Filters
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            list = list.stream().filter(l -> status.equalsIgnoreCase(l.status())).toList();
        }
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            list = list.stream().filter(l ->
                    (l.softwareName() != null && l.softwareName().toLowerCase().contains(q)) ||
                    (l.vendor() != null && l.vendor().toLowerCase().contains(q)) ||
                    (l.licenseType() != null && l.licenseType().toLowerCase().contains(q))
            ).toList();
        }

        int totalElements = list.size();
        int safePage = Math.max(0, page);
        int safeSize = size > 0 ? size : 10;
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = safePage * safeSize;
        List<EmployeeLicenseDto> paged = fromIndex >= totalElements ? Collections.emptyList() : list.subList(fromIndex, Math.min(fromIndex + safeSize, totalElements));

        return new EmployeeLicensesResponse(user.getUserId(), totalElements, paged, safePage, safeSize, totalPages);
    }

    // =========================================================
    // 13. REASSIGN / CHANGE DEVICE
    // =========================================================
    @Transactional
    public ReassignDeviceResponse reassignDevice(Long employeeId, ReassignDeviceRequest request) {
        User user = getUserOrThrow(employeeId);

        if ((request.serialNumber() == null || request.serialNumber().isBlank()) && request.deviceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid serial number or device ID is required for reassignment");
        }

        // 1. Close current assignment if any
        DeviceAssignment currentAssignment = deviceAssignmentRepository.findByUser_UserIdAndStatus(user.getUserId(), "ACTIVE").orElse(null);
        Device previousDevice = currentAssignment != null ? currentAssignment.getDevice() : deviceRepository.findByCurrentUserId(user.getUserId()).orElse(null);

        if (currentAssignment != null) {
            currentAssignment.setStatus("RETURNED");
            currentAssignment.setReturnedAt(LocalDateTime.now());
            deviceAssignmentRepository.save(currentAssignment);
        }
        if (previousDevice != null) {
            previousDevice.setCurrentUserId(null);
            deviceRepository.save(previousDevice);
        }

        // 2. Find new device
        Device newDevice = null;
        if (request.serialNumber() != null && !request.serialNumber().isBlank()) {
            newDevice = deviceRepository.findBySerialNumberIgnoreCase(request.serialNumber().trim())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Device not found with serial number: " + request.serialNumber()));
        } else if (request.deviceId() != null) {
            newDevice = deviceRepository.findById(request.deviceId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Device not found with ID: " + request.deviceId()));
        }

        if (newDevice == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target device not found");
        }

        // Verify not assigned to someone else
        if (newDevice.getCurrentUserId() != null && !newDevice.getCurrentUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Device " + newDevice.getSerialNumber() + " is already assigned to another employee");
        }

        // Close any lingering active assignments for this new device
        deviceAssignmentRepository.findBySerialNumberIgnoreCaseAndStatus(newDevice.getSerialNumber(), "ACTIVE")
                .ifPresent(a -> {
                    a.setStatus("RETURNED");
                    a.setReturnedAt(LocalDateTime.now());
                    deviceAssignmentRepository.save(a);
                });

        // 3. Link new device
        newDevice.setCurrentUserId(user.getUserId());
        newDevice.setDeviceStatus("ACTIVE");
        newDevice = deviceRepository.save(newDevice);

        DeviceAssignment newAssignment = new DeviceAssignment();
        newAssignment.setDevice(newDevice);
        newAssignment.setUser(user);
        newAssignment.setSerialNumber(newDevice.getSerialNumber());
        newAssignment.setAssignedAt(LocalDateTime.now());
        newAssignment.setStatus("ACTIVE");
        deviceAssignmentRepository.save(newAssignment);

        DeviceMiniDto prevDto = previousDevice != null ? new DeviceMiniDto(previousDevice.getDeviceId(), previousDevice.getSerialNumber(), previousDevice.getDeviceStatus()) : null;
        DeviceMiniDto nextDto = new DeviceMiniDto(newDevice.getDeviceId(), newDevice.getSerialNumber(), newDevice.getDeviceStatus());

        return new ReassignDeviceResponse("Device reassigned successfully", user.getUserId(), prevDto, nextDto);
    }

    // =========================================================
    // 14. UNASSIGN DEVICE
    // =========================================================
    @Transactional
    public UnassignDeviceResponse unassignDevice(Long employeeId) {
        User user = getUserOrThrow(employeeId);

        List<DeviceAssignment> userActive = deviceAssignmentRepository.findByUser_UserId(user.getUserId())
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).toList();

        Long deviceId = null;
        LocalDateTime unassignedAt = LocalDateTime.now();

        for (DeviceAssignment a : userActive) {
            if (a.getDevice() != null) {
                deviceId = a.getDevice().getDeviceId();
            }
            a.setStatus("RETURNED");
            a.setReturnedAt(unassignedAt);
            deviceAssignmentRepository.save(a);
        }

        deviceRepository.findByCurrentUserId(user.getUserId()).ifPresent(d -> {
            d.setCurrentUserId(null);
            deviceRepository.save(d);
        });

        return new UnassignDeviceResponse("Device unassigned successfully", user.getUserId(), deviceId, "UNASSIGNED", unassignedAt);
    }

    // =========================================================
    // HELPER
    // =========================================================
    private User getUserOrThrow(Long employeeId) {
        return userRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with ID: " + employeeId));
    }
}
