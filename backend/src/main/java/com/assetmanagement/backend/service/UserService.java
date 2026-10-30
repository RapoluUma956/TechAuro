package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.*;
import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final DeviceNetworkRepository deviceNetworkRepository;
    private final InstalledSoftwareRepository installedSoftwareRepository;
    private final VulnerabilityRepository vulnerabilityRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DeviceRepository deviceRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            DeviceNetworkRepository deviceNetworkRepository,
            InstalledSoftwareRepository installedSoftwareRepository,
            VulnerabilityRepository vulnerabilityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.deviceRepository = deviceRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.deviceNetworkRepository = deviceNetworkRepository;
        this.installedSoftwareRepository = installedSoftwareRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<DeviceAssignment> activeAssignments = deviceAssignmentRepository.findByStatus("ACTIVE");
        Map<Long, DeviceAssignment> userAssignmentMap = activeAssignments.stream()
                .filter(da -> da.getUser() != null)
                .collect(Collectors.toMap(da -> da.getUser().getUserId(), da -> da, (a1, a2) -> a1));

        Map<Long, Device> userDeviceMap = new HashMap<>();
        for (Device d : deviceRepository.findAll()) {
            if (d.getCurrentUserId() != null) {
                userDeviceMap.putIfAbsent(d.getCurrentUserId(), d);
            }
        }

        return users.stream()
                .map(u -> toResponse(u, userAssignmentMap.get(u.getUserId()), userDeviceMap.get(u.getUserId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = getUserEntityById(id);
        DeviceAssignment da = deviceAssignmentRepository.findByUser_UserIdAndStatus(id, "ACTIVE").orElse(null);
        Device dev = da != null ? da.getDevice() : deviceRepository.findByCurrentUserId(id).orElse(null);
        return toResponse(user, da, dev);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmployeeCode(String employeeCode) {
        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with employee code: " + employeeCode));
        DeviceAssignment da = deviceAssignmentRepository.findByUser_UserIdAndStatus(user.getUserId(), "ACTIVE").orElse(null);
        Device dev = da != null ? da.getDevice() : deviceRepository.findByCurrentUserId(user.getUserId()).orElse(null);
        return toResponse(user, da, dev);
    }

    @Transactional(readOnly = true)
    public EmployeeDetailResponse getEmployeeDetail(Long id) {
        User user = getUserEntityById(id);
        return buildEmployeeDetail(user);
    }

    @Transactional(readOnly = true)
    public EmployeeDetailResponse getEmployeeDetailByCode(String employeeCode) {
        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with employee code: " + employeeCode));
        return buildEmployeeDetail(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee code already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists");
        }

        User user = new User();
        user.setEmployeeCode(request.employeeCode());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());

        String rawPassword = (request.password() != null && !request.password().isBlank())
                ? request.password()
                : "Password@123";
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        user.setDesignation(request.designation());
        user.setDateOfJoining(request.dateOfJoining() != null ? request.dateOfJoining() : LocalDate.now());
        user.setAccountStatus("ACTIVE");
        user.setMustChangePassword(true);

        User savedUser = userRepository.save(user);

        DeviceAssignment assignment = null;
        if ((request.serialNumber() != null && !request.serialNumber().isBlank()) || request.deviceId() != null) {
            assignment = assignDeviceToUser(savedUser, request.serialNumber(), request.deviceId());
        }

        Device dev = assignment != null ? assignment.getDevice() : null;
        return toResponse(savedUser, assignment, dev);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserRequest request) {
        User user = getUserEntityById(userId);

        if (!user.getEmployeeCode().equalsIgnoreCase(request.employeeCode())
                && userRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee code already exists");
        }

        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists");
        }

        user.setEmployeeCode(request.employeeCode());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        user.setDesignation(request.designation());
        if (request.dateOfJoining() != null) {
            user.setDateOfJoining(request.dateOfJoining());
        }

        User savedUser = userRepository.save(user);

        DeviceAssignment assignment = null;
        if ((request.serialNumber() != null && !request.serialNumber().isBlank()) || request.deviceId() != null) {
            assignment = assignDeviceToUser(savedUser, request.serialNumber(), request.deviceId());
        } else {
            assignment = deviceAssignmentRepository.findByUser_UserIdAndStatus(userId, "ACTIVE").orElse(null);
        }

        Device dev = assignment != null ? assignment.getDevice() : deviceRepository.findByCurrentUserId(userId).orElse(null);
        return toResponse(savedUser, assignment, dev);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntityById(id);

        // Unlink any devices currently linked to this user
        deviceRepository.findByCurrentUserId(id).ifPresent(d -> {
            d.setCurrentUserId(null);
            deviceRepository.save(d);
        });

        // Delete device assignments for this user so foreign key constraint is satisfied
        List<DeviceAssignment> assignments = deviceAssignmentRepository.findByUser_UserId(id);
        if (!assignments.isEmpty()) {
            deviceAssignmentRepository.deleteAll(assignments);
        }

        userRepository.delete(user);
    }

    @Transactional
    public UserResponse assignDevice(Long userId, AssignDeviceRequest request) {
        User user = getUserEntityById(userId);
        DeviceAssignment da = assignDeviceToUser(user, request.serialNumber(), request.deviceId());
        if (da == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Device not found with serial number: " + request.serialNumber() + " or ID: " + request.deviceId());
        }
        return toResponse(user, da, da.getDevice());
    }

    @Transactional
    public UserResponse unassignDevice(Long userId) {
        User user = getUserEntityById(userId);

        List<DeviceAssignment> userActive = deviceAssignmentRepository.findByUser_UserId(userId)
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).toList();
        for (DeviceAssignment a : userActive) {
            a.setStatus("RETURNED");
            a.setReturnedAt(LocalDateTime.now());
            deviceAssignmentRepository.save(a);
        }
        deviceRepository.findByCurrentUserId(userId).ifPresent(d -> {
            d.setCurrentUserId(null);
            deviceRepository.save(d);
        });

        return toResponse(user, null, null);
    }

    private DeviceAssignment assignDeviceToUser(User user, String serialNumber, Long deviceId) {
        Device device = null;
        if (serialNumber != null && !serialNumber.isBlank()) {
            device = deviceRepository.findBySerialNumberIgnoreCase(serialNumber.trim())
                    .orElseGet(() -> deviceRepository.findBySerialNumber(serialNumber.trim()).orElse(null));
        } else if (deviceId != null) {
            device = deviceRepository.findById(deviceId).orElse(null);
        }

        if (device == null) {
            return null;
        }

        // 1. Return any active assignment for this device
        List<DeviceAssignment> deviceActive = deviceAssignmentRepository.findByDevice_DeviceId(device.getDeviceId())
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).toList();
        for (DeviceAssignment a : deviceActive) {
            a.setStatus("RETURNED");
            a.setReturnedAt(LocalDateTime.now());
            deviceAssignmentRepository.save(a);
        }

        // 2. Return any active assignment for this user
        List<DeviceAssignment> userActive = deviceAssignmentRepository.findByUser_UserId(user.getUserId())
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).toList();
        for (DeviceAssignment a : userActive) {
            a.setStatus("RETURNED");
            a.setReturnedAt(LocalDateTime.now());
            deviceAssignmentRepository.save(a);
        }

        // 3. Clear any other device referencing this user
        final Long targetDeviceId = device.getDeviceId();
        deviceRepository.findByCurrentUserId(user.getUserId()).ifPresent(d -> {
            if (!d.getDeviceId().equals(targetDeviceId)) {
                d.setCurrentUserId(null);
                deviceRepository.save(d);
            }
        });

        // 4. Update the device
        device.setCurrentUserId(user.getUserId());
        device.setDeviceStatus("ACTIVE");
        device = deviceRepository.save(device);

        // 5. Create new active assignment
        DeviceAssignment assignment = new DeviceAssignment();
        assignment.setDevice(device);
        assignment.setUser(user);
        assignment.setSerialNumber(device.getSerialNumber());
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus("ACTIVE");
        return deviceAssignmentRepository.save(assignment);
    }

    private EmployeeDetailResponse buildEmployeeDetail(User user) {
        DeviceAssignment activeDa = deviceAssignmentRepository.findByUser_UserIdAndStatus(user.getUserId(), "ACTIVE")
                .orElse(null);

        Device device = null;
        if (activeDa != null) {
            device = activeDa.getDevice();
        } else {
            device = deviceRepository.findByCurrentUserId(user.getUserId()).orElse(null);
        }

        DeviceAssignmentDto currentAssignmentDto = null;
        List<DeviceAssignmentDto> assignmentHistory = Collections.emptyList();
        List<DeviceNetwork> networkAdapters = Collections.emptyList();
        List<InstalledSoftware> installedSoftware = Collections.emptyList();
        List<Vulnerability> vulnerabilities = Collections.emptyList();

        if (device != null) {
            if (activeDa != null) {
                currentAssignmentDto = toAssignmentDto(activeDa);
            }
            assignmentHistory = deviceAssignmentRepository.findByDevice_DeviceIdOrderByAssignedAtDesc(device.getDeviceId())
                    .stream()
                    .map(this::toAssignmentDto)
                    .toList();
            networkAdapters = deviceNetworkRepository.findByDevice_DeviceId(device.getDeviceId());
            installedSoftware = installedSoftwareRepository.findByDevice_DeviceId(device.getDeviceId());
            vulnerabilities = vulnerabilityRepository.findByDevice_DeviceId(device.getDeviceId());
        }

        return new EmployeeDetailResponse(
                user.getUserId(),
                user.getEmployeeCode(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getDesignation(),
                user.getAccountStatus(),
                user.getDateOfJoining(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                device,
                currentAssignmentDto,
                assignmentHistory,
                networkAdapters,
                installedSoftware,
                vulnerabilities
        );
    }

    private DeviceAssignmentDto toAssignmentDto(DeviceAssignment da) {
        Device d = da.getDevice();
        User u = da.getUser();
        String devName = d != null ? (d.getDeviceName() != null ? d.getDeviceName() : (d.getBrand() + " " + d.getModel())) : "Unknown";
        String userName = u != null ? (u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : "")).trim() : "Unknown";
        return new DeviceAssignmentDto(
                da.getAssignmentId(),
                d != null ? d.getDeviceId() : null,
                da.getSerialNumber() != null ? da.getSerialNumber() : (d != null ? d.getSerialNumber() : null),
                devName,
                u != null ? u.getUserId() : null,
                u != null ? u.getEmployeeCode() : null,
                userName,
                da.getAssignedAt(),
                da.getReturnedAt(),
                da.getStatus()
        );
    }

    public UserResponse toResponse(User user) {
        return toResponse(user, null, null);
    }

    public UserResponse toResponse(User user, DeviceAssignment da, Device dev) {
        if (dev == null && da != null) {
            dev = da.getDevice();
        }

        Long assignedDeviceId = dev != null ? dev.getDeviceId() : null;
        String assignedSerialNumber = dev != null ? dev.getSerialNumber() : null;
        String assignedDeviceName = dev != null
                ? (dev.getDeviceName() != null && !dev.getDeviceName().isBlank()
                        ? dev.getDeviceName()
                        : (dev.getBrand() + " " + dev.getModel()).trim())
                : null;
        String assignedDeviceStatus = dev != null ? dev.getDeviceStatus() : null;
        String assignedConnectionStatus = dev != null ? dev.getConnectionStatus() : null;
        LocalDate assignedWarrantyDate = dev != null ? dev.getWarrantyEndDate() : null;

        return new UserResponse(
                user.getUserId(),
                user.getEmployeeCode(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getDesignation(),
                user.getDateOfJoining(),
                user.getAccountStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                assignedDeviceId,
                assignedSerialNumber,
                assignedDeviceName,
                assignedDeviceStatus,
                assignedConnectionStatus,
                assignedWarrantyDate
        );
    }
}
