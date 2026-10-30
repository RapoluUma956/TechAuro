package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.notification.NotificationDtos.*;
import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceRepository deviceRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final LicenseRepository licenseRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            DeviceRepository deviceRepository,
            VulnerabilityRepository vulnerabilityRepository,
            LicenseRepository licenseRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.deviceRepository = deviceRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.licenseRepository = licenseRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Inspects active devices, telemetry, open vulnerabilities, and licenses
     * to automatically identify alerts and set severity-driven notifications.
     */
    @Transactional
    public void syncSystemNotifications() {
        List<Notification> newNotifications = new ArrayList<>();

        // 1. Evaluate Open Vulnerabilities
        List<Vulnerability> vulnerabilities = vulnerabilityRepository.findAll();
        for (Vulnerability v : vulnerabilities) {
            String vulnStatus = v.getStatus();
            if ("RESOLVED".equalsIgnoreCase(vulnStatus) || "CLOSED".equalsIgnoreCase(vulnStatus)) {
                continue;
            }

            String cveId = v.getCveId() != null ? v.getCveId() : "VULN-" + v.getVulnerabilityId();
            String rawSeverity = v.getSeverity() != null ? v.getSeverity().toUpperCase() : "MEDIUM";
            String priority = switch (rawSeverity) {
                case "CRITICAL" -> "CRITICAL";
                case "HIGH" -> "HIGH";
                case "LOW" -> "LOW";
                default -> "MEDIUM";
            };

            Device dev = v.getDevice();
            Long devId = dev != null ? dev.getDeviceId() : null;
            String assetTag = dev != null && dev.getAssetTag() != null ? dev.getAssetTag() : "Unassigned Device";

            String title = (priority.equals("CRITICAL") ? "Critical Security CVE: " : "Security Vulnerability: ") + cveId;

            boolean exists = devId != null
                    ? notificationRepository.existsByTitleAndDevice_DeviceId(title, devId)
                    : notificationRepository.existsByTitle(title);

            if (!exists) {
                Notification notif = new Notification();
                notif.setTitle(title);
                notif.setMessage(String.format("Vulnerability %s (%s) detected on device %s. Review and apply security remediation.",
                        cveId,
                        v.getAffectedSoftware() != null ? v.getAffectedSoftware() : "System component",
                        assetTag));
                notif.setNotificationType("SECURITY");
                notif.setSeverity(priority);
                notif.setStatus("UNREAD");
                notif.setIsRead(false);
                notif.setDevice(dev);
                notif.setCreatedAt(LocalDateTime.now());
                newNotifications.add(notif);
            }
        }

        // 2. Evaluate Hardware Metrics & Device Connectivity
        List<Device> devices = deviceRepository.findAll();
        for (Device d : devices) {
            String devName = d.getDeviceName() != null ? d.getDeviceName() : "Asset " + d.getAssetTag();

            // A. Storage / Disk utilization
            if (d.getStorageUsagePercent() != null) {
                if (d.getStorageUsagePercent().compareTo(BigDecimal.valueOf(90)) >= 0) {
                    String title = "Critical Storage Capacity: " + d.getAssetTag();
                    if (!notificationRepository.existsByTitleAndDevice_DeviceId(title, d.getDeviceId())) {
                        Notification notif = new Notification();
                        notif.setTitle(title);
                        notif.setMessage(String.format("Storage utilization on %s (%s) has reached %d%% capacity. Immediate disk cleanup required.",
                                devName, d.getAssetTag(), d.getStorageUsagePercent().intValue()));
                        notif.setNotificationType("HARDWARE");
                        notif.setSeverity("CRITICAL");
                        notif.setStatus("UNREAD");
                        notif.setIsRead(false);
                        notif.setDevice(d);
                        notif.setCreatedAt(LocalDateTime.now());
                        newNotifications.add(notif);
                    }
                } else if (d.getStorageUsagePercent().compareTo(BigDecimal.valueOf(80)) >= 0) {
                    String title = "High Storage Utilization: " + d.getAssetTag();
                    if (!notificationRepository.existsByTitleAndDevice_DeviceId(title, d.getDeviceId())) {
                        Notification notif = new Notification();
                        notif.setTitle(title);
                        notif.setMessage(String.format("Storage utilization on %s (%s) is at %d%% capacity.",
                                devName, d.getAssetTag(), d.getStorageUsagePercent().intValue()));
                        notif.setNotificationType("HARDWARE");
                        notif.setSeverity("HIGH");
                        notif.setStatus("UNREAD");
                        notif.setIsRead(false);
                        notif.setDevice(d);
                        notif.setCreatedAt(LocalDateTime.now());
                        newNotifications.add(notif);
                    }
                }
            }

            // B. High CPU Utilization
            if (d.getCpuUsagePercent() != null && d.getCpuUsagePercent().compareTo(BigDecimal.valueOf(90)) >= 0) {
                String title = "Elevated CPU Load: " + d.getAssetTag();
                if (!notificationRepository.existsByTitleAndDevice_DeviceId(title, d.getDeviceId())) {
                    Notification notif = new Notification();
                    notif.setTitle(title);
                    notif.setMessage(String.format("Device %s (%s) reported elevated CPU load at %d%%.",
                            devName, d.getAssetTag(), d.getCpuUsagePercent().intValue()));
                    notif.setNotificationType("HARDWARE");
                    notif.setSeverity("HIGH");
                    notif.setStatus("UNREAD");
                    notif.setIsRead(false);
                    notif.setDevice(d);
                    notif.setCreatedAt(LocalDateTime.now());
                    newNotifications.add(notif);
                }
            }

            // C. High Memory Utilization
            if (d.getMemoryUsagePercent() != null && d.getMemoryUsagePercent().compareTo(BigDecimal.valueOf(90)) >= 0) {
                String title = "Elevated Memory Pressure: " + d.getAssetTag();
                if (!notificationRepository.existsByTitleAndDevice_DeviceId(title, d.getDeviceId())) {
                    Notification notif = new Notification();
                    notif.setTitle(title);
                    notif.setMessage(String.format("Device %s (%s) RAM consumption is at %d%%.",
                            devName, d.getAssetTag(), d.getMemoryUsagePercent().intValue()));
                    notif.setNotificationType("HARDWARE");
                    notif.setSeverity("HIGH");
                    notif.setStatus("UNREAD");
                    notif.setIsRead(false);
                    notif.setDevice(d);
                    notif.setCreatedAt(LocalDateTime.now());
                    newNotifications.add(notif);
                }
            }

            // D. Low Battery
            if (d.getBatteryPercentage() != null && d.getBatteryPercentage().compareTo(BigDecimal.valueOf(15)) < 0 && !Boolean.TRUE.equals(d.getBatteryCharging())) {
                String title = "Low Battery Warning: " + d.getAssetTag();
                if (!notificationRepository.existsByTitleAndDevice_DeviceId(title, d.getDeviceId())) {
                    Notification notif = new Notification();
                    notif.setTitle(title);
                    notif.setMessage(String.format("Device %s (%s) battery is critically low at %d%%.",
                            devName, d.getAssetTag(), d.getBatteryPercentage().intValue()));
                    notif.setNotificationType("HARDWARE");
                    notif.setSeverity("MEDIUM");
                    notif.setStatus("UNREAD");
                    notif.setIsRead(false);
                    notif.setDevice(d);
                    notif.setCreatedAt(LocalDateTime.now());
                    newNotifications.add(notif);
                }
            }

            // E. Offline Status on Assigned Devices
            if (d.getConnectionStatus() != null && "OFFLINE".equalsIgnoreCase(d.getConnectionStatus())) {
                boolean isAssigned = deviceAssignmentRepository.findByDevice_DeviceIdAndStatus(d.getDeviceId(), "ACTIVE").isPresent();
                if (isAssigned) {
                    String title = "Assigned Device Offline: " + d.getAssetTag();
                    if (!notificationRepository.existsByTitleAndDevice_DeviceId(title, d.getDeviceId())) {
                        Notification notif = new Notification();
                        notif.setTitle(title);
                        notif.setMessage(String.format("Assigned laptop %s (%s) has ceased telemetry communication.",
                                devName, d.getAssetTag()));
                        notif.setNotificationType("DEVICE");
                        notif.setSeverity("MEDIUM");
                        notif.setStatus("UNREAD");
                        notif.setIsRead(false);
                        notif.setDevice(d);
                        notif.setCreatedAt(LocalDateTime.now());
                        newNotifications.add(notif);
                    }
                }
            }
        }

        // 3. Evaluate License Expirations
        LocalDate today = LocalDate.now();
        List<License> licenses = licenseRepository.findAll();
        for (License l : licenses) {
            if (l.getExpiryDate() != null) {
                String sw = l.getSoftwareName() != null ? l.getSoftwareName() : "Software License";
                if (l.getExpiryDate().isBefore(today)) {
                    String title = "License Expired: " + sw;
                    if (!notificationRepository.existsByTitle(title)) {
                        Notification notif = new Notification();
                        notif.setTitle(title);
                        notif.setMessage(String.format("Software license for %s expired on %s. Immediate renewal required.",
                                sw, l.getExpiryDate()));
                        notif.setNotificationType("LICENSE");
                        notif.setSeverity("CRITICAL");
                        notif.setStatus("UNREAD");
                        notif.setIsRead(false);
                        notif.setCreatedAt(LocalDateTime.now());
                        newNotifications.add(notif);
                    }
                } else if (!l.getExpiryDate().isAfter(today.plusDays(7))) {
                    long days = ChronoUnit.DAYS.between(today, l.getExpiryDate());
                    String title = "Urgent License Expiry (" + days + "d): " + sw;
                    if (!notificationRepository.existsByTitle(title)) {
                        Notification notif = new Notification();
                        notif.setTitle(title);
                        notif.setMessage(String.format("License for %s will expire in %d day(s) on %s.",
                                sw, days, l.getExpiryDate()));
                        notif.setNotificationType("LICENSE");
                        notif.setSeverity("HIGH");
                        notif.setStatus("UNREAD");
                        notif.setIsRead(false);
                        notif.setCreatedAt(LocalDateTime.now());
                        newNotifications.add(notif);
                    }
                } else if (!l.getExpiryDate().isAfter(today.plusDays(30))) {
                    String title = "Upcoming License Expiry: " + sw;
                    if (!notificationRepository.existsByTitle(title)) {
                        Notification notif = new Notification();
                        notif.setTitle(title);
                        notif.setMessage(String.format("Software license for %s is scheduled to expire on %s.",
                                sw, l.getExpiryDate()));
                        notif.setNotificationType("LICENSE");
                        notif.setSeverity("MEDIUM");
                        notif.setStatus("UNREAD");
                        notif.setIsRead(false);
                        notif.setCreatedAt(LocalDateTime.now());
                        newNotifications.add(notif);
                    }
                }
            }
        }

        if (!newNotifications.isEmpty()) {
            notificationRepository.saveAll(newNotifications);
        }
    }

    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(
            int page,
            int size,
            String severity,
            String type,
            Boolean unreadOnly,
            String search) {

        List<Notification> all = notificationRepository.findAllByOrderByCreatedAtDesc();

        String sevFilter = (severity != null && !severity.isBlank() && !"ALL".equalsIgnoreCase(severity))
                ? severity.trim().toUpperCase() : null;
        String typeFilter = (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type))
                ? type.trim().toUpperCase() : null;
        String searchFilter = (search != null && !search.isBlank())
                ? search.trim().toLowerCase() : null;

        List<NotificationDto> filtered = all.stream()
                .filter(n -> {
                    if (sevFilter != null && !sevFilter.equalsIgnoreCase(n.getSeverity())) {
                        return false;
                    }
                    if (typeFilter != null && !typeFilter.equalsIgnoreCase(n.getNotificationType())) {
                        return false;
                    }
                    if (Boolean.TRUE.equals(unreadOnly) && Boolean.TRUE.equals(n.getIsRead())) {
                        return false;
                    }
                    if (searchFilter != null) {
                        String title = n.getTitle() != null ? n.getTitle().toLowerCase() : "";
                        String msg = n.getMessage() != null ? n.getMessage().toLowerCase() : "";
                        String tag = (n.getDevice() != null && n.getDevice().getAssetTag() != null)
                                ? n.getDevice().getAssetTag().toLowerCase() : "";
                        if (!title.contains(searchFilter) && !msg.contains(searchFilter) && !tag.contains(searchFilter)) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::toDto)
                .collect(Collectors.toList());

        long unreadCount = notificationRepository.countByIsReadFalse();

        int totalElements = filtered.size();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);

        List<NotificationDto> pagedContent = filtered.subList(fromIndex, toIndex);
        int totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / safeSize);
        boolean isLast = (safePage + 1) >= totalPages;

        return new NotificationPageResponse(
                pagedContent,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                isLast,
                unreadCount
        );
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getSummary() {
        long total = notificationRepository.count();
        long unread = notificationRepository.countByIsReadFalse();
        long critical = notificationRepository.countBySeverityIgnoreCaseAndIsReadFalse("CRITICAL");
        long high = notificationRepository.countBySeverityIgnoreCaseAndIsReadFalse("HIGH");
        long medium = notificationRepository.countBySeverityIgnoreCaseAndIsReadFalse("MEDIUM");
        long low = notificationRepository.countBySeverityIgnoreCaseAndIsReadFalse("LOW");

        return new NotificationSummaryResponse(
                total,
                unread,
                critical,
                high,
                medium,
                low
        );
    }

    @Transactional(readOnly = true)
    public NotificationDto getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found with id: " + id));
        return toDto(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found with id: " + id));
        notification.setStatus("READ");
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Transactional
    public void markAllAsRead() {
        List<Notification> unreadList = notificationRepository.findByIsReadFalse();
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : unreadList) {
            n.setStatus("READ");
            n.setIsRead(true);
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unreadList);
    }

    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    @Transactional
    public NotificationDto createNotification(CreateNotificationRequest request) {
        Notification notif = new Notification();
        notif.setTitle(request.title());
        notif.setMessage(request.message());
        notif.setNotificationType(request.notificationType() != null ? request.notificationType() : "SYSTEM");
        notif.setSeverity(request.severity() != null ? request.severity() : "LOW");
        notif.setStatus("UNREAD");
        notif.setIsRead(false);
        notif.setCreatedAt(LocalDateTime.now());

        if (request.deviceId() != null) {
            deviceRepository.findById(request.deviceId()).ifPresent(notif::setDevice);
        }
        if (request.userId() != null) {
            userRepository.findById(request.userId()).ifPresent(notif::setUser);
        }

        Notification saved = notificationRepository.save(notif);
        return toDto(saved);
    }

    private NotificationDto toDto(Notification n) {
        Long devId = null;
        String devTag = null;
        String devName = null;
        String devSerial = null;

        if (n.getDevice() != null) {
            try {
                devId = n.getDevice().getDeviceId();
                devTag = n.getDevice().getAssetTag();
                devName = n.getDevice().getDeviceName();
                devSerial = n.getDevice().getSerialNumber();
            } catch (Exception ignored) {}
        }

        Long uId = null;
        String uName = null;
        String empCode = null;

        if (n.getUser() != null) {
            try {
                uId = n.getUser().getUserId();
                String first = n.getUser().getFirstName() != null ? n.getUser().getFirstName() : "";
                String last = n.getUser().getLastName() != null ? n.getUser().getLastName() : "";
                uName = (first + " " + last).trim();
                empCode = n.getUser().getEmployeeCode();
            } catch (Exception ignored) {}
        }

        return new NotificationDto(
                n.getNotificationId(),
                n.getNotificationType(),
                n.getTitle(),
                n.getMessage(),
                n.getSeverity(),
                n.getStatus(),
                n.getIsRead(),
                n.getCreatedAt(),
                n.getReadAt(),
                devId,
                devTag,
                devName,
                devSerial,
                uId,
                uName,
                empCode
        );
    }
}
