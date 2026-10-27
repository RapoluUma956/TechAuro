package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.dashboard.*;
import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final LicenseRepository licenseRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final NotificationRepository notificationRepository;
    private final HealthThresholdRepository healthThresholdRepository;

    public DashboardService(
            DeviceRepository deviceRepository,
            VulnerabilityRepository vulnerabilityRepository,
            LicenseRepository licenseRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            NotificationRepository notificationRepository,
            HealthThresholdRepository healthThresholdRepository) {
        this.deviceRepository = deviceRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.licenseRepository = licenseRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.notificationRepository = notificationRepository;
        this.healthThresholdRepository = healthThresholdRepository;
    }

    // =========================================================
    // 1) GET /api/dashboard/summary
    // =========================================================
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        HealthThreshold thresholds = getOrCreateThresholds();
        List<Device> devices = deviceRepository.findAll();
        List<Vulnerability> vulnerabilities = vulnerabilityRepository.findAll();
        List<License> licenses = licenseRepository.findAll();

        long totalAssets = devices.size();
        long onlineAssets = 0;
        long offlineAssets = 0;

        long healthyCount = 0;
        long warningCount = 0;
        long criticalCount = 0;
        long unknownCount = 0;

        LocalDateTime latestTimestamp = null;
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        // Group open vulnerabilities by device ID
        Map<Long, List<Vulnerability>> openVulnsByDevice = vulnerabilities.stream()
                .filter(this::isOpenVulnerability)
                .filter(v -> v.getDevice() != null)
                .collect(Collectors.groupingBy(v -> v.getDevice().getDeviceId()));

        for (Device device : devices) {
            boolean isOnline = computeIsOnline(device, thresholds.getOfflineTimeoutMinutes());
            if (isOnline) {
                onlineAssets++;
            } else {
                offlineAssets++;
            }

            LocalDateTime deviceRecordedAt = device.getRecordedAt() != null ? device.getRecordedAt() : device.getUpdatedAt();
            if (deviceRecordedAt != null && (latestTimestamp == null || deviceRecordedAt.isAfter(latestTimestamp))) {
                latestTimestamp = deviceRecordedAt;
            }

            List<Vulnerability> devVulns = openVulnsByDevice.getOrDefault(device.getDeviceId(), Collections.emptyList());
            String health = computeDeviceHealth(device, isOnline, devVulns, thresholds, today);

            switch (health) {
                case "CRITICAL" -> criticalCount++;
                case "WARNING" -> warningCount++;
                case "HEALTHY" -> healthyCount++;
                default -> unknownCount++;
            }
        }

        int onlinePercentage = totalAssets > 0 ? (int) Math.round((onlineAssets * 100.0) / totalAssets) : 0;
        int offlinePercentage = totalAssets > 0 ? (100 - onlinePercentage) : 0;

        int healthyPercentage = totalAssets > 0 ? (int) Math.round((healthyCount * 100.0) / totalAssets) : 0;
        int warningPercentage = totalAssets > 0 ? (int) Math.round((warningCount * 100.0) / totalAssets) : 0;
        int criticalPercentage = totalAssets > 0 ? (int) Math.round((criticalCount * 100.0) / totalAssets) : 0;
        int unknownPercentage = totalAssets > 0 ? Math.max(0, 100 - healthyPercentage - warningPercentage - criticalPercentage) : 0;

        // Open vulnerabilities overview
        List<Vulnerability> openVulns = vulnerabilities.stream().filter(this::isOpenVulnerability).toList();
        long totalOpenVulns = openVulns.size();
        long criticalVulns = openVulns.stream().filter(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity())).count();
        long highVulns = openVulns.stream().filter(v -> "HIGH".equalsIgnoreCase(v.getSeverity())).count();
        long mediumVulns = openVulns.stream().filter(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity())).count();
        long lowVulns = openVulns.stream().filter(v -> "LOW".equalsIgnoreCase(v.getSeverity())).count();

        // Licenses overview
        long totalLicenses = licenses.size();
        long expiredLicenses = 0;
        long expiringWithin7Days = 0;
        long expiringWithin30Days = 0;
        long activeLicenses = 0;

        for (License license : licenses) {
            LocalDate exp = license.getExpiryDate();
            if (exp == null) {
                activeLicenses++;
                continue;
            }

            if (exp.isBefore(today)) {
                expiredLicenses++;
            } else if (!exp.isAfter(today.plusDays(7))) {
                expiringWithin7Days++;
            } else if (!exp.isAfter(today.plusDays(thresholds.getLicenseWarningDays()))) {
                expiringWithin30Days++;
            } else {
                activeLicenses++;
            }
        }

        long licensesExpiringSoon = expiredLicenses + expiringWithin7Days + expiringWithin30Days;

        OffsetDateTime lastUpdatedOffset = latestTimestamp != null
                ? latestTimestamp.atZone(zoneId).toOffsetDateTime()
                : OffsetDateTime.now();

        return new DashboardSummaryResponse(
                totalAssets,
                onlineAssets,
                offlineAssets,
                totalOpenVulns,
                licensesExpiringSoon,
                lastUpdatedOffset,
                new DashboardSummaryResponse.DeviceConnectivityDto(
                        onlineAssets,
                        offlineAssets,
                        onlinePercentage,
                        offlinePercentage
                ),
                new DashboardSummaryResponse.AssetHealthDto(
                        new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(healthyCount, healthyPercentage),
                        new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(warningCount, warningPercentage),
                        new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(criticalCount, criticalPercentage),
                        new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(unknownCount, unknownPercentage)
                ),
                new DashboardSummaryResponse.VulnerabilityOverviewDto(
                        totalOpenVulns,
                        criticalVulns,
                        highVulns,
                        mediumVulns,
                        lowVulns
                ),
                new DashboardSummaryResponse.LicenseOverviewDto(
                        totalLicenses,
                        expiredLicenses,
                        expiringWithin7Days,
                        expiringWithin30Days,
                        activeLicenses
                )
        );
    }

    // =========================================================
    // 2) GET /api/dashboard/recent-alerts?limit=5
    // =========================================================
    @Transactional(readOnly = true)
    public DashboardAlertsResponse getRecentAlerts(int limit) {
        HealthThreshold thresholds = getOrCreateThresholds();
        List<Device> devices = deviceRepository.findAll();
        List<Vulnerability> vulnerabilities = vulnerabilityRepository.findAll();
        List<License> licenses = licenseRepository.findAll();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        List<DashboardAlertItem> alerts = new ArrayList<>();

        // 1. Critical and High Vulnerability Alerts
        for (Vulnerability v : vulnerabilities) {
            if (!isOpenVulnerability(v)) continue;
            String sev = v.getSeverity() != null ? v.getSeverity().toUpperCase() : "MEDIUM";
            if ("CRITICAL".equals(sev) || "HIGH".equals(sev)) {
                String title = "CRITICAL".equals(sev) ? "Critical vulnerability detected" : "High severity security risk";
                String msg = v.getDescription() != null ? v.getDescription() : "Security review required";
                Long devId = v.getDevice() != null ? v.getDevice().getDeviceId() : null;
                String tag = v.getDevice() != null ? v.getDevice().getAssetTag() : null;
                LocalDateTime detected = v.getDetectedAt() != null ? v.getDetectedAt() : LocalDateTime.now();

                alerts.add(new DashboardAlertItem(
                        "VULNERABILITY-" + v.getVulnerabilityId(),
                        "VULNERABILITY",
                        sev,
                        title,
                        msg,
                        devId,
                        tag,
                        detected.atZone(zoneId).toOffsetDateTime(),
                        false
                ));
            }
        }

        // 2. High Disk Utilization Alerts
        for (Device d : devices) {
            BigDecimal storageUsage = d.getStorageUsagePercent();
            if (storageUsage != null && storageUsage.compareTo(thresholds.getDiskCriticalPercent()) >= 0) {
                LocalDateTime time = d.getRecordedAt() != null ? d.getRecordedAt() : d.getUpdatedAt();
                alerts.add(new DashboardAlertItem(
                        "DISK-" + d.getDeviceId(),
                        "DISK",
                        "WARNING",
                        "High disk utilization",
                        "Device " + d.getAssetTag() + " has low free storage (" + storageUsage.intValue() + "% used)",
                        d.getDeviceId(),
                        d.getAssetTag(),
                        time != null ? time.atZone(zoneId).toOffsetDateTime() : OffsetDateTime.now(),
                        false
                ));
            }
        }

        // 3. Offline Device Alerts
        for (Device d : devices) {
            boolean isOnline = computeIsOnline(d, thresholds.getOfflineTimeoutMinutes());
            if (!isOnline) {
                LocalDateTime time = d.getRecordedAt() != null ? d.getRecordedAt() : d.getUpdatedAt();
                alerts.add(new DashboardAlertItem(
                        "OFFLINE-" + d.getDeviceId(),
                        "DEVICE",
                        "WARNING",
                        "Device offline",
                        "Device " + d.getAssetTag() + " has not reported within configured timeout",
                        d.getDeviceId(),
                        d.getAssetTag(),
                        time != null ? time.atZone(zoneId).toOffsetDateTime() : OffsetDateTime.now(),
                        false
                ));
            }
        }

        // 4. Software License Expiring Alerts
        for (License l : licenses) {
            if (l.getExpiryDate() != null) {
                long daysUntil = Period.between(today, l.getExpiryDate()).getDays();
                boolean isExpired = l.getExpiryDate().isBefore(today);
                boolean isExpiringSoon = !isExpired && !l.getExpiryDate().isAfter(today.plusDays(thresholds.getLicenseWarningDays()));

                if (isExpired || isExpiringSoon) {
                    String title = isExpired ? "Software license expired" : "Software license expiring";
                    String msg = isExpired
                            ? l.getSoftwareName() + " license expired on " + l.getExpiryDate()
                            : l.getSoftwareName() + " expires within " + Math.max(1, daysUntil) + " days";
                    LocalDateTime time = l.getUpdatedAt() != null ? l.getUpdatedAt() : LocalDateTime.now();

                    alerts.add(new DashboardAlertItem(
                            "LICENSE-" + l.getLicenseId(),
                            "LICENSE",
                            isExpired ? "CRITICAL" : "WARNING",
                            title,
                            msg,
                            null,
                            null,
                            time.atZone(zoneId).toOffsetDateTime(),
                            false
                    ));
                }
            }
        }

        // 5. Warranty Approaching Expiry Alerts
        for (Device d : devices) {
            if (d.getWarrantyEndDate() != null) {
                boolean isWarrantyExpiring = !d.getWarrantyEndDate().isBefore(today) &&
                        !d.getWarrantyEndDate().isAfter(today.plusDays(thresholds.getWarrantyWarningDays()));
                if (isWarrantyExpiring) {
                    long daysUntil = Period.between(today, d.getWarrantyEndDate()).getDays();
                    LocalDateTime time = d.getUpdatedAt() != null ? d.getUpdatedAt() : LocalDateTime.now();
                    alerts.add(new DashboardAlertItem(
                            "WARRANTY-" + d.getDeviceId(),
                            "WARRANTY",
                            "WARNING",
                            "Warranty approaching expiry",
                            "Device " + d.getAssetTag() + " warranty expires in " + Math.max(1, daysUntil) + " days",
                            d.getDeviceId(),
                            d.getAssetTag(),
                            time.atZone(zoneId).toOffsetDateTime(),
                            false
                    ));
                }
            }
        }

        // Sort: CRITICAL first, then WARNING, then by createdAt desc
        alerts.sort((a, b) -> {
            int sevCompare = severityRank(a.severity()) - severityRank(b.severity());
            if (sevCompare != 0) return sevCompare;
            return b.createdAt().compareTo(a.createdAt());
        });

        int total = alerts.size();
        List<DashboardAlertItem> limited = alerts.stream().limit(Math.max(1, limit)).toList();
        return new DashboardAlertsResponse(limited, total);
    }

    // =========================================================
    // 3) GET /api/dashboard/assets/attention
    // =========================================================
    @Transactional(readOnly = true)
    public AttentionAssetsResponse getAssetsRequiringAttention(
            String filter,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        HealthThreshold thresholds = getOrCreateThresholds();
        List<Device> devices = deviceRepository.findAll();
        List<Vulnerability> vulnerabilities = vulnerabilityRepository.findAll();
        List<DeviceAssignment> assignments = deviceAssignmentRepository.findAll();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        // Active assignments by device ID
        Map<Long, DeviceAssignment> activeAssignments = new HashMap<>();
        for (DeviceAssignment da : assignments) {
            if (da.getDevice() != null && da.getUser() != null) {
                if ("ACTIVE".equalsIgnoreCase(da.getStatus()) || "CURRENT".equalsIgnoreCase(da.getStatus())) {
                    activeAssignments.put(da.getDevice().getDeviceId(), da);
                }
            }
        }

        // Open vulnerabilities by device ID
        Map<Long, List<Vulnerability>> openVulnsByDevice = vulnerabilities.stream()
                .filter(this::isOpenVulnerability)
                .filter(v -> v.getDevice() != null)
                .collect(Collectors.groupingBy(v -> v.getDevice().getDeviceId()));

        List<AttentionAssetItem> attentionList = new ArrayList<>();

        for (Device device : devices) {
            boolean isOnline = computeIsOnline(device, thresholds.getOfflineTimeoutMinutes());
            String connStatus = isOnline ? "ONLINE" : "OFFLINE";
            List<Vulnerability> devVulns = openVulnsByDevice.getOrDefault(device.getDeviceId(), Collections.emptyList());
            String health = computeDeviceHealth(device, isOnline, devVulns, thresholds, today);

            // Asset requires attention if it is CRITICAL, WARNING, or OFFLINE
            boolean requiresAttention = "CRITICAL".equalsIgnoreCase(health) ||
                    "WARNING".equalsIgnoreCase(health) ||
                    "OFFLINE".equalsIgnoreCase(connStatus);

            if (!requiresAttention) {
                continue;
            }

            DeviceAssignment da = activeAssignments.get(device.getDeviceId());
            Long empId = da != null ? da.getUser().getUserId() : null;
            String empName = da != null
                    ? (da.getUser().getFirstName() + (da.getUser().getLastName() != null ? " " + da.getUser().getLastName() : ""))
                    : "Unassigned";
            String designation = da != null && da.getUser().getDesignation() != null
                    ? da.getUser().getDesignation()
                    : "N/A";

            Integer cpu = device.getCpuUsagePercent() != null ? device.getCpuUsagePercent().intValue() : 0;
            Integer ram = device.getMemoryUsagePercent() != null ? device.getMemoryUsagePercent().intValue() : 0;
            Integer disk = device.getStorageUsagePercent() != null ? device.getStorageUsagePercent().intValue() : 0;

            LocalDateTime lastSeen = device.getRecordedAt() != null ? device.getRecordedAt() : device.getUpdatedAt();
            OffsetDateTime lastSeenOffset = lastSeen != null
                    ? lastSeen.atZone(zoneId).toOffsetDateTime()
                    : OffsetDateTime.now();

            attentionList.add(new AttentionAssetItem(
                    device.getDeviceId(),
                    device.getAssetTag(),
                    empId,
                    empName,
                    designation,
                    health,
                    connStatus,
                    cpu,
                    ram,
                    disk,
                    lastSeenOffset
            ));
        }

        // Apply Tab Filter
        String tabFilter = filter != null ? filter.trim().toUpperCase() : "ALL";
        List<AttentionAssetItem> filtered = attentionList.stream()
                .filter(item -> {
                    return switch (tabFilter) {
                        case "CRITICAL" -> "CRITICAL".equalsIgnoreCase(item.healthStatus());
                        case "WARNING" -> "WARNING".equalsIgnoreCase(item.healthStatus());
                        case "OFFLINE" -> "OFFLINE".equalsIgnoreCase(item.connectionStatus());
                        default -> true;
                    };
                })
                .toList();

        // Apply Search Filter
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(item ->
                            (item.assetTag() != null && item.assetTag().toLowerCase().contains(q)) ||
                                    (item.employeeName() != null && item.employeeName().toLowerCase().contains(q)) ||
                                    (item.designation() != null && item.designation().toLowerCase().contains(q))
                    )
                    .toList();
        }

        // Apply Sorting
        Comparator<AttentionAssetItem> comparator = getComparator(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        List<AttentionAssetItem> sorted = filtered.stream().sorted(comparator).toList();

        // Apply Pagination
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        long totalElements = sorted.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = Math.min(safePage * safeSize, (int) totalElements);
        int toIndex = Math.min(fromIndex + safeSize, (int) totalElements);

        List<AttentionAssetItem> pageItems = sorted.subList(fromIndex, toIndex);

        return new AttentionAssetsResponse(
                pageItems,
                new PaginationDto(safePage, safeSize, totalElements, totalPages)
        );
    }

    // =========================================================
    // 4) GET /api/dashboard/thresholds
    // =========================================================
    @Transactional
    public ThresholdDto getThresholds() {
        HealthThreshold thresholds = getOrCreateThresholds();
        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime updated = thresholds.getUpdatedAt() != null
                ? thresholds.getUpdatedAt().atZone(zoneId).toOffsetDateTime()
                : OffsetDateTime.now();

        return new ThresholdDto(
                thresholds.getThresholdId(),
                thresholds.getCpuWarningPercent().intValue(),
                thresholds.getRamWarningPercent().intValue(),
                thresholds.getDiskCriticalPercent().intValue(),
                thresholds.getOfflineTimeoutMinutes(),
                thresholds.getWarrantyWarningDays(),
                thresholds.getLicenseWarningDays(),
                updated
        );
    }

    // =========================================================
    // 5) PUT /api/dashboard/thresholds
    // =========================================================
    @Transactional
    public ThresholdDto updateThresholds(UpdateThresholdRequest request) {
        HealthThreshold thresholds = getOrCreateThresholds();

        thresholds.setCpuWarningPercent(BigDecimal.valueOf(request.cpuWarningPercent()));
        thresholds.setRamWarningPercent(BigDecimal.valueOf(request.ramWarningPercent()));
        thresholds.setDiskCriticalPercent(BigDecimal.valueOf(request.diskCriticalPercent()));
        thresholds.setOfflineTimeoutMinutes(request.offlineTimeoutMinutes());
        thresholds.setWarrantyWarningDays(request.warrantyWarningDays());
        thresholds.setLicenseWarningDays(request.licenseWarningDays());
        thresholds.setUpdatedAt(LocalDateTime.now());

        thresholds = healthThresholdRepository.save(thresholds);

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime updated = thresholds.getUpdatedAt().atZone(zoneId).toOffsetDateTime();

        return new ThresholdDto(
                thresholds.getThresholdId(),
                thresholds.getCpuWarningPercent().intValue(),
                thresholds.getRamWarningPercent().intValue(),
                thresholds.getDiskCriticalPercent().intValue(),
                thresholds.getOfflineTimeoutMinutes(),
                thresholds.getWarrantyWarningDays(),
                thresholds.getLicenseWarningDays(),
                updated
        );
    }

    // =========================================================
    // INTERNAL HELPERS
    // =========================================================
    public HealthThreshold getOrCreateThresholds() {
        return healthThresholdRepository.findFirstByOrderByThresholdIdAsc()
                .orElseGet(() -> {
                    HealthThreshold defaults = new HealthThreshold(90, 90, 90, 5, 30, 30);
                    defaults.setUpdatedAt(LocalDateTime.now());
                    return healthThresholdRepository.save(defaults);
                });
    }

    private boolean computeIsOnline(Device device, int timeoutMinutes) {
        if ("OFFLINE".equalsIgnoreCase(device.getConnectionStatus())) {
            return false;
        }
        if (device.getRecordedAt() == null) {
            return false;
        }
        long minutesAgo = Duration.between(device.getRecordedAt(), LocalDateTime.now()).toMinutes();
        return minutesAgo <= timeoutMinutes;
    }

    private String computeDeviceHealth(
            Device device,
            boolean isOnline,
            List<Vulnerability> devVulns,
            HealthThreshold thresholds,
            LocalDate today) {

        int cpu = device.getCpuUsagePercent() != null ? device.getCpuUsagePercent().intValue() : -1;
        int ram = device.getMemoryUsagePercent() != null ? device.getMemoryUsagePercent().intValue() : -1;
        int disk = device.getStorageUsagePercent() != null ? device.getStorageUsagePercent().intValue() : -1;

        if (cpu == -1 && ram == -1 && disk == -1 && devVulns.isEmpty()) {
            return "UNKNOWN";
        }

        boolean hasCriticalVuln = devVulns.stream().anyMatch(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity()));
        boolean hasHighVuln = devVulns.stream().anyMatch(v -> "HIGH".equalsIgnoreCase(v.getSeverity()));
        boolean hasMediumVuln = devVulns.stream().anyMatch(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity()));

        // CRITICAL condition
        if (hasCriticalVuln || (disk >= thresholds.getDiskCriticalPercent().intValue()) || cpu >= 95 || ram >= 95) {
            return "CRITICAL";
        }

        // WARNING condition
        boolean isWarrantyExpiring = device.getWarrantyEndDate() != null &&
                !device.getWarrantyEndDate().isBefore(today) &&
                !device.getWarrantyEndDate().isAfter(today.plusDays(thresholds.getWarrantyWarningDays()));

        if (!isOnline ||
                hasHighVuln ||
                hasMediumVuln ||
                (cpu >= thresholds.getCpuWarningPercent().intValue()) ||
                (ram >= thresholds.getRamWarningPercent().intValue()) ||
                (disk >= 80) ||
                isWarrantyExpiring) {
            return "WARNING";
        }

        return "HEALTHY";
    }

    private boolean isOpenVulnerability(Vulnerability v) {
        if (v == null) return false;
        if (v.getResolvedAt() != null) return false;
        if (v.getStatus() != null && "RESOLVED".equalsIgnoreCase(v.getStatus())) return false;
        return true;
    }

    private int severityRank(String sev) {
        if ("CRITICAL".equalsIgnoreCase(sev)) return 1;
        if ("HIGH".equalsIgnoreCase(sev)) return 2;
        if ("WARNING".equalsIgnoreCase(sev)) return 3;
        if ("MEDIUM".equalsIgnoreCase(sev)) return 4;
        if ("LOW".equalsIgnoreCase(sev)) return 5;
        return 6;
    }

    private Comparator<AttentionAssetItem> getComparator(String sortBy) {
        if (sortBy == null) return Comparator.comparing(AttentionAssetItem::lastSeenAt, Comparator.nullsLast(Comparator.naturalOrder()));
        return switch (sortBy.toLowerCase()) {
            case "assettag", "tag" -> Comparator.comparing(AttentionAssetItem::assetTag, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "employeename", "employee" -> Comparator.comparing(AttentionAssetItem::employeeName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "healthstatus", "health" -> Comparator.comparing(AttentionAssetItem::healthStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "connectionstatus", "connection" -> Comparator.comparing(AttentionAssetItem::connectionStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "cpuusage", "cpu" -> Comparator.comparing(AttentionAssetItem::cpuUsage, Comparator.nullsLast(Comparator.naturalOrder()));
            case "ramusage", "ram" -> Comparator.comparing(AttentionAssetItem::ramUsage, Comparator.nullsLast(Comparator.naturalOrder()));
            case "diskusage", "disk" -> Comparator.comparing(AttentionAssetItem::diskUsage, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(AttentionAssetItem::lastSeenAt, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }
}
