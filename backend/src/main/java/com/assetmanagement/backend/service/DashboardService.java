
package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.dashboard.*;
import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.repository.UserRepository;
import com.assetmanagement.backend.entity.Accessory;
import com.assetmanagement.backend.repository.AccessoryRepository;
import com.assetmanagement.backend.entity.AccessoryAssignment;
import com.assetmanagement.backend.repository.AccessoryAssignmentRepository;
import com.assetmanagement.backend.entity.LicenseAssignment;
import com.assetmanagement.backend.repository.LicenseAssignmentRepository;
import com.assetmanagement.backend.entity.DeviceNetwork;
import com.assetmanagement.backend.repository.DeviceNetworkRepository;
import com.assetmanagement.backend.entity.InstalledSoftware;
import com.assetmanagement.backend.repository.InstalledSoftwareRepository;
import com.assetmanagement.backend.security.AuthorizationService;
import com.assetmanagement.backend.security.AuthorizedScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.PageRequest;
import java.util.Objects;
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
    private final UserRepository userRepository;
    private final AccessoryRepository accessoryRepository;
    private final AccessoryAssignmentRepository accessoryAssignmentRepository;
    private final LicenseAssignmentRepository licenseAssignmentRepository;
    private final DeviceNetworkRepository deviceNetworkRepository;
    private final InstalledSoftwareRepository installedSoftwareRepository;
    private final AuthorizationService authorizationService;
    private final DeviceHeartbeatMonitorService heartbeatMonitorService;

    public DashboardService(
            DeviceRepository deviceRepository,
            VulnerabilityRepository vulnerabilityRepository,
            LicenseRepository licenseRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            NotificationRepository notificationRepository,
            HealthThresholdRepository healthThresholdRepository,
            UserRepository userRepository,
            AccessoryRepository accessoryRepository,
            AccessoryAssignmentRepository accessoryAssignmentRepository,
            LicenseAssignmentRepository licenseAssignmentRepository,
            DeviceNetworkRepository deviceNetworkRepository,
            InstalledSoftwareRepository installedSoftwareRepository,
            AuthorizationService authorizationService,
            DeviceHeartbeatMonitorService heartbeatMonitorService) {
        this.deviceRepository = deviceRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.licenseRepository = licenseRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.notificationRepository = notificationRepository;
        this.healthThresholdRepository = healthThresholdRepository;
        this.userRepository = userRepository;
        this.accessoryRepository = accessoryRepository;
        this.accessoryAssignmentRepository = accessoryAssignmentRepository;
        this.licenseAssignmentRepository = licenseAssignmentRepository;
        this.deviceNetworkRepository = deviceNetworkRepository;
        this.installedSoftwareRepository = installedSoftwareRepository;
        this.authorizationService = authorizationService;
        this.heartbeatMonitorService = heartbeatMonitorService;
    }

    // =========================================================
    // 1) GET /api/dashboard/summary
    // =========================================================
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        AuthorizedScope scope = authorizationService.getAuthorizedScope("DASHBOARD");
        HealthThreshold thresholds = getOrCreateThresholds();
        List<Device> devices;
        List<Vulnerability> vulnerabilities;
        List<License> licenses;

        if (scope.isAll()) {
            devices = deviceRepository.findAll();
            vulnerabilities = vulnerabilityRepository.findAll();
            licenses = licenseRepository.findAll();
        } else if (scope.isEmpty()) {
            devices = Collections.emptyList();
            vulnerabilities = Collections.emptyList();
            licenses = Collections.emptyList();
        } else {
            Set<Long> deviceIds = authorizationService.resolveAuthorizedDeviceIds(scope);
            devices = (deviceIds == null || deviceIds.isEmpty())
                    ? Collections.emptyList()
                    : deviceRepository.findByDeviceIdIn(deviceIds);
            vulnerabilities = (deviceIds == null || deviceIds.isEmpty())
                    ? Collections.emptyList()
                    : vulnerabilityRepository.findByDevice_DeviceIdIn(deviceIds);
            licenses = licenseAssignmentRepository.findByUser_UserIdInAndStatus(scope.employeeIds(), "ACTIVE")
                    .stream()
                    .map(LicenseAssignment::getLicense)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }

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

            LocalDateTime deviceRecordedAt =
                    device.getRecordedAt() != null ? device.getRecordedAt() : device.getUpdatedAt();

            if (deviceRecordedAt != null
                    && (latestTimestamp == null || deviceRecordedAt.isAfter(latestTimestamp))) {
                latestTimestamp = deviceRecordedAt;
            }

            List<Vulnerability> devVulns =
                    openVulnsByDevice.getOrDefault(device.getDeviceId(), Collections.emptyList());

            String health = computeDeviceHealth(device, isOnline, devVulns, thresholds, today);

            switch (health) {
                case "CRITICAL" -> criticalCount++;
                case "WARNING" -> warningCount++;
                case "HEALTHY" -> healthyCount++;
                default -> unknownCount++;
            }
        }

        int onlinePercentage = totalAssets > 0
                ? (int) Math.round((onlineAssets * 100.0) / totalAssets) : 0;

        int offlinePercentage = totalAssets > 0 ? (100 - onlinePercentage) : 0;

        int healthyPercentage = totalAssets > 0
                ? (int) Math.round((healthyCount * 100.0) / totalAssets) : 0;

        int warningPercentage = totalAssets > 0
                ? (int) Math.round((warningCount * 100.0) / totalAssets) : 0;

        int criticalPercentage = totalAssets > 0
                ? (int) Math.round((criticalCount * 100.0) / totalAssets) : 0;

        int unknownPercentage = totalAssets > 0
                ? Math.max(0, 100 - healthyPercentage - warningPercentage - criticalPercentage) : 0;

        List<Vulnerability> openVulns =
                vulnerabilities.stream().filter(this::isOpenVulnerability).toList();

        long totalOpenVulns = openVulns.size();
        long criticalVulns = openVulns.stream()
                .filter(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity())).count();
        long highVulns = openVulns.stream()
                .filter(v -> "HIGH".equalsIgnoreCase(v.getSeverity())).count();
        long mediumVulns = openVulns.stream()
                .filter(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity())).count();
        long lowVulns = openVulns.stream()
                .filter(v -> "LOW".equalsIgnoreCase(v.getSeverity())).count();

        // Categorize any unclassified or unknown severities as MEDIUM so the sum always matches totalOpenVulns
        long categorized = criticalVulns + highVulns + mediumVulns + lowVulns;
        if (categorized < totalOpenVulns) {
            mediumVulns += (totalOpenVulns - categorized);
        }

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

        long licensesExpiringSoon =
                expiredLicenses + expiringWithin7Days + expiringWithin30Days;

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
                        new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(
                                healthyCount, healthyPercentage),
                        new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(
                                warningCount, warningPercentage),
                        new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(
                                criticalCount, criticalPercentage),
                        new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(
                                unknownCount, unknownPercentage)
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
        AuthorizedScope scope = authorizationService.getAuthorizedScope("DASHBOARD");
        HealthThreshold thresholds = getOrCreateThresholds();
        List<Device> devices;
        List<Vulnerability> vulnerabilities;
        List<License> licenses;

        if (scope.isAll()) {
            devices = deviceRepository.findAll();
            vulnerabilities = vulnerabilityRepository.findAll();
            licenses = licenseRepository.findAll();
        } else if (scope.isEmpty()) {
            devices = Collections.emptyList();
            vulnerabilities = Collections.emptyList();
            licenses = Collections.emptyList();
        } else {
            Set<Long> deviceIds = authorizationService.resolveAuthorizedDeviceIds(scope);
            devices = (deviceIds == null || deviceIds.isEmpty())
                    ? Collections.emptyList()
                    : deviceRepository.findByDeviceIdIn(deviceIds);
            vulnerabilities = (deviceIds == null || deviceIds.isEmpty())
                    ? Collections.emptyList()
                    : vulnerabilityRepository.findByDevice_DeviceIdIn(deviceIds);
            licenses = licenseAssignmentRepository.findByUser_UserIdInAndStatus(scope.employeeIds(), "ACTIVE")
                    .stream()
                    .map(LicenseAssignment::getLicense)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        List<DashboardAlertItem> alerts = new ArrayList<>();

        // 1. Critical and High Vulnerability Alerts
        for (Vulnerability v : vulnerabilities) {
            if (!isOpenVulnerability(v)) {
                continue;
            }

            String sev = v.getSeverity() != null
                    ? v.getSeverity().toUpperCase() : "MEDIUM";

            if ("CRITICAL".equals(sev) || "HIGH".equals(sev)) {
                String title = "CRITICAL".equals(sev)
                        ? "Critical vulnerability detected"
                        : "High severity security risk";

                String msg = v.getDescription() != null
                        ? v.getDescription() : "Security review required";

                Long devId = v.getDevice() != null
                        ? v.getDevice().getDeviceId() : null;

                String tag = v.getDevice() != null
                        ? v.getDevice().getAssetTag() : null;

                LocalDateTime detected = v.getDetectedAt() != null
                        ? v.getDetectedAt() : LocalDateTime.now();

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

            // FIX: Convert the Integer threshold to BigDecimal before comparing.
            if (storageUsage != null
                    && thresholds.getDiskCriticalPercent() != null
                    && storageUsage.compareTo(
                    BigDecimal.valueOf(thresholds.getDiskCriticalPercent())
            ) >= 0) {

                LocalDateTime time = d.getRecordedAt() != null
                        ? d.getRecordedAt() : d.getUpdatedAt();

                alerts.add(new DashboardAlertItem(
                        "DISK-" + d.getDeviceId(),
                        "DISK",
                        "WARNING",
                        "High disk utilization",
                        "Device " + d.getAssetTag()
                                + " has low free storage (" + storageUsage.intValue() + "% used)",
                        d.getDeviceId(),
                        d.getAssetTag(),
                        time != null
                                ? time.atZone(zoneId).toOffsetDateTime()
                                : OffsetDateTime.now(),
                        false
                ));
            }
        }

        // 3. Offline Device Alerts
        for (Device d : devices) {
            boolean isOnline = computeIsOnline(d, thresholds.getOfflineTimeoutMinutes());

            if (!isOnline) {
                LocalDateTime time = d.getRecordedAt() != null
                        ? d.getRecordedAt() : d.getUpdatedAt();

                alerts.add(new DashboardAlertItem(
                        "OFFLINE-" + d.getDeviceId(),
                        "DEVICE",
                        "WARNING",
                        "Device offline",
                        "Device " + d.getAssetTag()
                                + " has not reported within configured timeout",
                        d.getDeviceId(),
                        d.getAssetTag(),
                        time != null
                                ? time.atZone(zoneId).toOffsetDateTime()
                                : OffsetDateTime.now(),
                        false
                ));
            }
        }

        // 4. Software License Expiring Alerts
        for (License l : licenses) {
            if (l.getExpiryDate() != null) {
                long daysUntil = java.time.temporal.ChronoUnit.DAYS
                        .between(today, l.getExpiryDate());

                boolean isExpired = l.getExpiryDate().isBefore(today);

                boolean isExpiringSoon = !isExpired
                        && !l.getExpiryDate().isAfter(
                        today.plusDays(thresholds.getLicenseWarningDays()));

                if (isExpired || isExpiringSoon) {
                    String title = isExpired
                            ? "Software license expired"
                            : "Software license expiring";

                    String msg = isExpired
                            ? l.getSoftwareName() + " license expired on " + l.getExpiryDate()
                            : l.getSoftwareName() + " expires within "
                            + Math.max(1, daysUntil) + " days";

                    LocalDateTime time = l.getUpdatedAt() != null
                            ? l.getUpdatedAt() : LocalDateTime.now();

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
                boolean isWarrantyExpiring =
                        !d.getWarrantyEndDate().isBefore(today)
                                && !d.getWarrantyEndDate().isAfter(
                                today.plusDays(thresholds.getWarrantyWarningDays()));

                if (isWarrantyExpiring) {
                    long daysUntil = java.time.temporal.ChronoUnit.DAYS
                            .between(today, d.getWarrantyEndDate());

                    LocalDateTime time = d.getUpdatedAt() != null
                            ? d.getUpdatedAt() : LocalDateTime.now();

                    alerts.add(new DashboardAlertItem(
                            "WARRANTY-" + d.getDeviceId(),
                            "WARRANTY",
                            "WARNING",
                            "Warranty approaching expiry",
                            "Device " + d.getAssetTag()
                                    + " warranty expires in " + Math.max(1, daysUntil) + " days",
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

            if (sevCompare != 0) {
                return sevCompare;
            }

            return b.createdAt().compareTo(a.createdAt());
        });

        int total = alerts.size();

        List<DashboardAlertItem> limited = alerts.stream()
                .limit(Math.max(1, limit))
                .toList();

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

        AuthorizedScope scope = authorizationService.getAuthorizedScope("DASHBOARD");
        HealthThreshold thresholds = getOrCreateThresholds();

        List<Device> devices;
        List<Vulnerability> vulnerabilities;
        List<DeviceAssignment> assignments;

        if (scope.isAll()) {
            devices = deviceRepository.findAll();
            vulnerabilities = vulnerabilityRepository.findAll();
            assignments = deviceAssignmentRepository.findAll();
        } else if (scope.isEmpty()) {
            return new AttentionAssetsResponse(Collections.emptyList(), new PaginationDto(page, size, 0, 1));
        } else {
            Set<Long> deviceIds = authorizationService.resolveAuthorizedDeviceIds(scope);
            devices = (deviceIds == null || deviceIds.isEmpty())
                    ? Collections.emptyList()
                    : deviceRepository.findByDeviceIdIn(deviceIds);
            vulnerabilities = (deviceIds == null || deviceIds.isEmpty())
                    ? Collections.emptyList()
                    : vulnerabilityRepository.findByDevice_DeviceIdIn(deviceIds);
            assignments = deviceAssignmentRepository.findByUser_UserIdInAndStatus(scope.employeeIds(), "ACTIVE");
        }

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        // Active assignments by device ID
        Map<Long, DeviceAssignment> activeAssignments = new HashMap<>();

        for (DeviceAssignment da : assignments) {
            if (da.getDevice() != null && da.getUser() != null) {
                if ("ACTIVE".equalsIgnoreCase(da.getStatus())
                        || "CURRENT".equalsIgnoreCase(da.getStatus())) {
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
            boolean isOnline = computeIsOnline(
                    device, thresholds.getOfflineTimeoutMinutes());

            String connStatus = isOnline ? "ONLINE" : "OFFLINE";

            List<Vulnerability> devVulns = openVulnsByDevice.getOrDefault(
                    device.getDeviceId(), Collections.emptyList());

            String health = computeDeviceHealth(
                    device, isOnline, devVulns, thresholds, today);

            boolean requiresAttention = "CRITICAL".equalsIgnoreCase(health)
                    || "WARNING".equalsIgnoreCase(health)
                    || "OFFLINE".equalsIgnoreCase(connStatus);

            if (!requiresAttention) {
                continue;
            }

            DeviceAssignment da = activeAssignments.get(device.getDeviceId());

            Long empId = da != null ? da.getUser().getUserId() : null;

            String empName = da != null
                    ? (da.getUser().getFirstName()
                    + (da.getUser().getLastName() != null
                    ? " " + da.getUser().getLastName() : ""))
                    : "Unassigned";

            String designation = da != null && da.getUser().getDesignation() != null
                    ? da.getUser().getDesignation()
                    : "N/A";

            Integer cpu = device.getCpuUsagePercent() != null
                    ? device.getCpuUsagePercent().intValue() : 0;

            Integer ram = device.getMemoryUsagePercent() != null
                    ? device.getMemoryUsagePercent().intValue() : 0;

            Integer disk = device.getStorageUsagePercent() != null
                    ? device.getStorageUsagePercent().intValue() : 0;

            LocalDateTime lastSeen = device.getRecordedAt() != null
                    ? device.getRecordedAt() : device.getUpdatedAt();

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
        String tabFilter = filter != null
                ? filter.trim().toUpperCase() : "ALL";

        List<AttentionAssetItem> filtered = attentionList.stream()
                .filter(item -> switch (tabFilter) {
                    case "CRITICAL" -> "CRITICAL".equalsIgnoreCase(item.healthStatus());
                    case "WARNING" -> "WARNING".equalsIgnoreCase(item.healthStatus());
                    case "OFFLINE" -> "OFFLINE".equalsIgnoreCase(item.connectionStatus());
                    default -> true;
                })
                .toList();

        // Apply Search Filter
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();

            filtered = filtered.stream()
                    .filter(item ->
                            (item.assetTag() != null
                                    && item.assetTag().toLowerCase().contains(q))
                                    || (item.employeeName() != null
                                    && item.employeeName().toLowerCase().contains(q))
                                    || (item.designation() != null
                                    && item.designation().toLowerCase().contains(q))
                    )
                    .toList();
        }

        // Apply Sorting
        Comparator<AttentionAssetItem> comparator = getComparator(sortBy);

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        List<AttentionAssetItem> sorted = filtered.stream()
                .sorted(comparator)
                .toList();

        // Apply Pagination
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);

        long totalElements = sorted.size();

        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        if (totalPages == 0) {
            totalPages = 1;
        }

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

        thresholds.setCpuWarningPercent(
                BigDecimal.valueOf(request.cpuWarningPercent()));

        thresholds.setRamWarningPercent(
                BigDecimal.valueOf(request.ramWarningPercent()));

        thresholds.setDiskCriticalPercent(
                BigDecimal.valueOf(request.diskCriticalPercent()));

        thresholds.setOfflineTimeoutMinutes(request.offlineTimeoutMinutes());
        thresholds.setWarrantyWarningDays(request.warrantyWarningDays());
        thresholds.setLicenseWarningDays(request.licenseWarningDays());

        thresholds.setUpdatedAt(LocalDateTime.now());

        thresholds = healthThresholdRepository.save(thresholds);

        // Immediately trigger background heartbeat sweep so all devices immediately adopt the updated offline timeout threshold
        try {
            heartbeatMonitorService.sweepDeviceHeartbeats();
        } catch (Exception sweepErr) {
            // sweep will also run periodically every 15 seconds
        }

        ZoneId zoneId = ZoneId.systemDefault();

        OffsetDateTime updated = thresholds.getUpdatedAt()
                .atZone(zoneId).toOffsetDateTime();

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
                    HealthThreshold defaults =
                            new HealthThreshold(90, 90, 90, 5, 30, 30);

                    defaults.setUpdatedAt(LocalDateTime.now());

                    return healthThresholdRepository.save(defaults);
                });
    }

    private boolean computeIsOnline(Device device, int timeoutMinutes) {
        if (device == null) {
            return false;
        }
        return "ONLINE".equalsIgnoreCase(heartbeatMonitorService.resolveEffectiveStatus(device, timeoutMinutes));
    }

    private boolean unused_computeIsOnline(Device device, int timeoutMinutes) {
        if (device == null) {
            return false;
        }

        LocalDateTime lastSeen = device.getRecordedAt() != null
                ? device.getRecordedAt()
                : (device.getLastSeenAt() != null ? device.getLastSeenAt() : device.getUpdatedAt());

        if (lastSeen == null) {
            return false;
        }

        long minutesAgo = Duration.between(lastSeen, LocalDateTime.now()).toMinutes();
        return minutesAgo <= timeoutMinutes;
    }

    private String computeDeviceHealth(
            Device device,
            boolean isOnline,
            List<Vulnerability> devVulns,
            HealthThreshold thresholds,
            LocalDate today) {

        int cpu = device.getCpuUsagePercent() != null
                ? device.getCpuUsagePercent().intValue() : -1;

        int ram = device.getMemoryUsagePercent() != null
                ? device.getMemoryUsagePercent().intValue() : -1;

        int disk = device.getStorageUsagePercent() != null
                ? device.getStorageUsagePercent().intValue() : -1;

        if (cpu == -1 && ram == -1 && disk == -1 && devVulns.isEmpty()) {
            return "UNKNOWN";
        }

        boolean hasCriticalVuln = devVulns.stream()
                .anyMatch(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity()));

        boolean hasHighVuln = devVulns.stream()
                .anyMatch(v -> "HIGH".equalsIgnoreCase(v.getSeverity()));

        boolean hasMediumVuln = devVulns.stream()
                .anyMatch(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity()));

        // CRITICAL condition
        if (hasCriticalVuln
                || disk >= thresholds.getDiskCriticalPercent().intValue()
                || cpu >= 95
                || ram >= 95) {
            return "CRITICAL";
        }

        // WARNING condition
        boolean isWarrantyExpiring = device.getWarrantyEndDate() != null
                && !device.getWarrantyEndDate().isBefore(today)
                && !device.getWarrantyEndDate().isAfter(
                today.plusDays(thresholds.getWarrantyWarningDays()));

        if (!isOnline
                || hasHighVuln
                || hasMediumVuln
                || cpu >= thresholds.getCpuWarningPercent().intValue()
                || ram >= thresholds.getRamWarningPercent().intValue()
                || disk >= 80
                || isWarrantyExpiring) {
            return "WARNING";
        }

        return "HEALTHY";
    }

    private boolean isOpenVulnerability(Vulnerability v) {
        if (v == null) {
            return false;
        }

        if (v.getResolvedAt() != null) {
            return false;
        }

        if (v.getStatus() != null
                && "RESOLVED".equalsIgnoreCase(v.getStatus())) {
            return false;
        }

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
        if (sortBy == null) {
            return Comparator.comparing(
                    AttentionAssetItem::lastSeenAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        }

        return switch (sortBy.toLowerCase()) {
            case "assettag", "tag" -> Comparator.comparing(
                    AttentionAssetItem::assetTag,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

            case "employeename", "employee" -> Comparator.comparing(
                    AttentionAssetItem::employeeName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

            case "healthstatus", "health" -> Comparator.comparing(
                    AttentionAssetItem::healthStatus,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

            case "connectionstatus", "connection" -> Comparator.comparing(
                    AttentionAssetItem::connectionStatus,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

            case "cpuusage", "cpu" -> Comparator.comparing(
                    AttentionAssetItem::cpuUsage,
                    Comparator.nullsLast(Comparator.naturalOrder()));

            case "ramusage", "ram" -> Comparator.comparing(
                    AttentionAssetItem::ramUsage,
                    Comparator.nullsLast(Comparator.naturalOrder()));

            case "diskusage", "disk" -> Comparator.comparing(
                    AttentionAssetItem::diskUsage,
                    Comparator.nullsLast(Comparator.naturalOrder()));

            default -> Comparator.comparing(
                    AttentionAssetItem::lastSeenAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    // =========================================================
    // ADMIN DASHBOARD SUMMARY
    // =========================================================
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getAdminSummary() {
        AuthorizedScope scope = authorizationService.getAuthorizedScope("DASHBOARD");
        if (!scope.isAll()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: Admin dashboard summary requires ALL data scope.");
        }

        long totalEmployees = userRepository.count();
        List<Device> allDevices = deviceRepository.findAll();
        long totalDevices = allDevices.size();
        long assignedDevices = allDevices.stream().filter(d -> d.getCurrentUserId() != null).count();
        long inRepairDevices = allDevices.stream().filter(d -> "REPAIR".equalsIgnoreCase(d.getDeviceStatus())).count();
        long unassignedDevices = Math.max(0, totalDevices - assignedDevices - inRepairDevices);

        List<Accessory> accessories = accessoryRepository.findAll();
        long totalAccessories = accessories.stream().mapToLong(a -> a.getTotalQuantity() != null ? a.getTotalQuantity() : 0).sum();
        long assignedAccessories = accessoryAssignmentRepository.countByStatus("ACTIVE");
        long availableAccessories = Math.max(0, totalAccessories - assignedAccessories);

        List<License> licenses = licenseRepository.findAll();
        long totalLicenses = licenses.size();
        long assignedLicenses = licenseAssignmentRepository.countByStatus("ACTIVE");
        Long totalSeats = licenseRepository.sumTotalSeats();
        long availableLicenses = Math.max(0, (totalSeats != null ? totalSeats : 0) - assignedLicenses);
        LocalDate today = LocalDate.now();
        long licensesExpiringSoon = licenseRepository.countExpiringSoon(today, today.plusDays(30));

        DashboardSummaryResponse fullSummary = getSummary();

        List<AdminDashboardSummaryResponse.DashboardAlertItem> globalAlerts = notificationRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5))
                .getContent().stream()
                .map(n -> new AdminDashboardSummaryResponse.DashboardAlertItem(
                        n.getNotificationId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getSeverity(),
                        n.getNotificationType(),
                        n.getCreatedAt()
                ))
                .toList();

        return new AdminDashboardSummaryResponse(
                totalEmployees,
                totalDevices,
                assignedDevices,
                unassignedDevices,
                inRepairDevices,
                totalAccessories,
                assignedAccessories,
                availableAccessories,
                totalLicenses,
                assignedLicenses,
                availableLicenses,
                licensesExpiringSoon,
                fullSummary.assetHealth(),
                fullSummary.vulnerabilityOverview(),
                fullSummary.lastUpdatedAt(),
                globalAlerts
        );
    }

    // =========================================================
    // EMPLOYEE DASHBOARD (MY SUMMARY)
    // =========================================================
    @Transactional(readOnly = true)
    public EmployeeDashboardSummaryResponse getMySummary() {
        User currentUser = authorizationService.requireCurrentUser();

        // 1. Profile
        String fullName = java.util.stream.Stream.of(currentUser.getFirstName(), currentUser.getLastName())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
        if (fullName.isBlank()) fullName = currentUser.getFirstName();

        EmployeeDashboardSummaryResponse.EmployeeProfileDto profileDto = new EmployeeDashboardSummaryResponse.EmployeeProfileDto(
                currentUser.getUserId(),
                currentUser.getEmployeeCode(),
                fullName,
                currentUser.getEmail(),
                currentUser.getDesignation() != null ? currentUser.getDesignation() : "Employee",
                currentUser.getAccountStatus() != null ? currentUser.getAccountStatus() : "ACTIVE",
                currentUser.getDateOfJoining()
        );

        // 2. Assigned Device
        DeviceAssignment da = deviceAssignmentRepository.findByUser_UserIdAndStatus(currentUser.getUserId(), "ACTIVE").orElse(null);
        Device device = da != null ? da.getDevice() : deviceRepository.findByCurrentUserId(currentUser.getUserId()).orElse(null);

        EmployeeDashboardSummaryResponse.AssignedLaptopDto laptopDto = null;
        EmployeeDashboardSummaryResponse.CpuInfoDto cpuDto = null;
        EmployeeDashboardSummaryResponse.RamInfoDto ramDto = null;
        EmployeeDashboardSummaryResponse.SsdInfoDto ssdDto = null;
        EmployeeDashboardSummaryResponse.BatteryInfoDto batteryDto = null;
        List<EmployeeDashboardSummaryResponse.NetworkInfoDto> networkList = Collections.emptyList();
        int softwareCount = 0;
        List<Vulnerability> vulns = Collections.emptyList();

        if (device != null) {
            String effConn = heartbeatMonitorService.resolveEffectiveStatus(device);
            String devName = (device.getDeviceName() != null && !device.getDeviceName().isBlank())
                    ? device.getDeviceName()
                    : ((device.getBrand() != null ? device.getBrand() : "") + " " + (device.getModel() != null ? device.getModel() : "Laptop")).trim();

            laptopDto = new EmployeeDashboardSummaryResponse.AssignedLaptopDto(
                    device.getDeviceId(),
                    device.getAssetTag(),
                    device.getSerialNumber(),
                    devName,
                    device.getBrand(),
                    device.getModel(),
                    device.getDeviceStatus() != null ? device.getDeviceStatus() : "ACTIVE",
                    effConn,
                    device.getOsName(),
                    device.getLastSeenAt()
            );

            if (device.getCpuName() != null || device.getCpu() != null || device.getCpuUsagePercent() != null) {
                cpuDto = new EmployeeDashboardSummaryResponse.CpuInfoDto(
                        device.getCpuName() != null ? device.getCpuName() : device.getCpu(),
                        device.getCpuCores(),
                        device.getCpuThreads(),
                        device.getCpuUsagePercent() != null ? device.getCpuUsagePercent().doubleValue() : null,
                        device.getCpuTemperature() != null ? device.getCpuTemperature().doubleValue() : null
                );
            }

            BigDecimal ramTotal = device.getRamTotalGb() != null ? device.getRamTotalGb() : device.getRamGb();
            if (ramTotal != null) {
                ramDto = new EmployeeDashboardSummaryResponse.RamInfoDto(
                        ramTotal.doubleValue(),
                        device.getRamUsedGb() != null ? device.getRamUsedGb().doubleValue() : null,
                        device.getRamAvailableGb() != null ? device.getRamAvailableGb().doubleValue() : null,
                        device.getRamUsagePercent() != null ? device.getRamUsagePercent().doubleValue() : null,
                        device.getRamType()
                );
            }

            BigDecimal storageTotal = device.getStorageTotalGb() != null ? device.getStorageTotalGb() : device.getSsdCapacityGb();
            if (storageTotal != null) {
                ssdDto = new EmployeeDashboardSummaryResponse.SsdInfoDto(
                        storageTotal.doubleValue(),
                        device.getStorageUsedGb() != null ? device.getStorageUsedGb().doubleValue() : (device.getSsdUsedGb() != null ? device.getSsdUsedGb().doubleValue() : null),
                        device.getStorageAvailableGb() != null ? device.getStorageAvailableGb().doubleValue() : (device.getSsdAvailableGb() != null ? device.getSsdAvailableGb().doubleValue() : null),
                        device.getStorageHealth() != null ? device.getStorageHealth() : device.getSsdHealth()
                );
            }

            if (device.getBatteryPercentage() != null) {
                batteryDto = new EmployeeDashboardSummaryResponse.BatteryInfoDto(
                        device.getBatteryPercentage().intValue(),
                        device.getBatteryHealthPercent() != null ? device.getBatteryHealthPercent().doubleValue() : null,
                        device.getBatteryCharging(),
                        device.getBatteryCycleCount()
                );
            }

            networkList = deviceNetworkRepository.findByDevice_DeviceId(device.getDeviceId()).stream()
                    .map(n -> new EmployeeDashboardSummaryResponse.NetworkInfoDto(
                            n.getInterfaceName(),
                            n.getMacAddress(),
                            n.getIpv4Address(),
                            n.getConnectionStatus()
                    )).toList();

            softwareCount = installedSoftwareRepository.findByDevice_DeviceId(device.getDeviceId()).size();
            vulns = vulnerabilityRepository.findByDevice_DeviceId(device.getDeviceId());
        }

        // 3. Accessories
        List<EmployeeDashboardSummaryResponse.AccessoryInfoDto> accessoriesList = accessoryAssignmentRepository.findByUser_UserId(currentUser.getUserId()).stream()
                .filter(aa -> "ACTIVE".equalsIgnoreCase(aa.getStatus()) && aa.getAccessory() != null)
                .map(aa -> {
                    Accessory a = aa.getAccessory();
                    return new EmployeeDashboardSummaryResponse.AccessoryInfoDto(
                            a.getAccessoryId(),
                            a.getAccessoryName() != null ? a.getAccessoryName() : a.getProductName(),
                            a.getCategory(),
                            a.getBrand(),
                            a.getModel(),
                            a.getSerialNumber()
                    );
                }).toList();

        // 4. Licenses
        List<EmployeeDashboardSummaryResponse.LicenseInfoDto> licenseList = licenseAssignmentRepository.findByUser_UserId(currentUser.getUserId()).stream()
                .filter(la -> "ACTIVE".equalsIgnoreCase(la.getStatus()) && la.getLicense() != null)
                .map(la -> {
                    License l = la.getLicense();
                    return new EmployeeDashboardSummaryResponse.LicenseInfoDto(
                            l.getLicenseId(),
                            l.getSoftwareName(),
                            l.getPublisher(),
                            l.getLicenseType(),
                            l.getExpiryDate(),
                            la.getStatus()
                    );
                }).toList();

        // 5. Vulnerabilities Summary
        long totalVulns = vulns.size();
        long critVulns = vulns.stream().filter(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity())).count();
        long highVulns = vulns.stream().filter(v -> "HIGH".equalsIgnoreCase(v.getSeverity())).count();
        long medVulns = vulns.stream().filter(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity())).count();
        long lowVulns = vulns.stream().filter(v -> "LOW".equalsIgnoreCase(v.getSeverity())).count();
        List<EmployeeDashboardSummaryResponse.VulnerabilitySummaryDto.VulnerabilityItemDto> vulnItems = vulns.stream()
                .map(v -> new EmployeeDashboardSummaryResponse.VulnerabilitySummaryDto.VulnerabilityItemDto(
                        v.getVulnerabilityId(),
                        v.getCveId(),
                        v.getTitle() != null ? v.getTitle() : v.getDescription(),
                        v.getSeverity(),
                        v.getStatus()
                )).toList();

        EmployeeDashboardSummaryResponse.VulnerabilitySummaryDto vulnSummary = new EmployeeDashboardSummaryResponse.VulnerabilitySummaryDto(
                totalVulns, critVulns, highVulns, medVulns, lowVulns, vulnItems
        );

        // 6. Notifications
        List<Long> devIdList = device != null ? List.of(device.getDeviceId()) : Collections.emptyList();
        List<EmployeeDashboardSummaryResponse.NotificationInfoDto> notificationsList = notificationRepository
                .findByUser_UserIdInOrDevice_DeviceIdInOrderByCreatedAtDesc(List.of(currentUser.getUserId()), devIdList)
                .stream().limit(10)
                .map(n -> new EmployeeDashboardSummaryResponse.NotificationInfoDto(
                        n.getNotificationId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getSeverity(),
                        n.getCreatedAt()
                )).toList();

        return new EmployeeDashboardSummaryResponse(
                profileDto,
                laptopDto,
                cpuDto,
                ramDto,
                ssdDto,
                batteryDto,
                networkList,
                accessoriesList,
                licenseList,
                softwareCount,
                vulnSummary,
                notificationsList
        );
    }

    // =========================================================
    // IT MANAGER DASHBOARD SUMMARY
    // =========================================================
    @Transactional(readOnly = true)
    public ManagerDashboardSummaryResponse getManagerSummary() {
        authorizationService.validateAccess("DASHBOARD", "READ");
        String scopeName = authorizationService.getEffectiveDataScope("DASHBOARD");
        if ("OWN".equalsIgnoreCase(scopeName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: Manager dashboard summary is not accessible with OWN data scope.");
        }

        AuthorizedScope scope = authorizationService.getAuthorizedScope("DASHBOARD");
        Set<Long> userIds;
        List<Device> devices;
        Set<Long> deviceIds;

        if (scope.isAll()) {
            userIds = userRepository.findAll().stream().map(User::getUserId).collect(Collectors.toSet());
            devices = deviceRepository.findAll();
            deviceIds = devices.stream().map(Device::getDeviceId).collect(Collectors.toSet());
        } else if (scope.isEmpty()) {
            return new ManagerDashboardSummaryResponse(
                    0, 0,
                    new DashboardSummaryResponse.AssetHealthDto(
                            new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(0, 0),
                            new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(0, 0),
                            new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(0, 0),
                            new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(0, 0)
                    ),
                    0.0, 0.0, 0, 0,
                    new DashboardSummaryResponse.VulnerabilityOverviewDto(0, 0, 0, 0, 0),
                    Collections.emptyList()
            );
        } else {
            userIds = scope.employeeIds();
            deviceIds = authorizationService.resolveAuthorizedDeviceIds(scope);
            devices = (deviceIds == null || deviceIds.isEmpty())
                    ? Collections.emptyList()
                    : deviceRepository.findByDeviceIdIn(deviceIds);
        }

        long assignedEmployeeCount = (userIds != null) ? userIds.size() : 0;
        long assignedDeviceCount = devices.size();

        HealthThreshold thresholds = getOrCreateThresholds();
        List<Vulnerability> vulnerabilities = (deviceIds == null || deviceIds.isEmpty())
                ? Collections.emptyList()
                : vulnerabilityRepository.findByDevice_DeviceIdIn(deviceIds);

        Map<Long, List<Vulnerability>> openVulnsByDevice = vulnerabilities.stream()
                .filter(this::isOpenVulnerability)
                .filter(v -> v.getDevice() != null)
                .collect(Collectors.groupingBy(v -> v.getDevice().getDeviceId()));

        long healthy = 0, warning = 0, critical = 0, unknown = 0;
        LocalDate today = LocalDate.now();

        for (Device d : devices) {
            boolean isOnline = computeIsOnline(d, thresholds.getOfflineTimeoutMinutes());
            List<Vulnerability> devVulns = openVulnsByDevice.getOrDefault(d.getDeviceId(), Collections.emptyList());
            String health = computeDeviceHealth(d, isOnline, devVulns, thresholds, today);
            switch (health) {
                case "CRITICAL" -> critical++;
                case "WARNING" -> warning++;
                case "HEALTHY" -> healthy++;
                default -> unknown++;
            }
        }

        int hPct = assignedDeviceCount > 0 ? (int) Math.round((healthy * 100.0) / assignedDeviceCount) : 0;
        int wPct = assignedDeviceCount > 0 ? (int) Math.round((warning * 100.0) / assignedDeviceCount) : 0;
        int cPct = assignedDeviceCount > 0 ? (int) Math.round((critical * 100.0) / assignedDeviceCount) : 0;
        int uPct = assignedDeviceCount > 0 ? Math.max(0, 100 - hPct - wPct - cPct) : 0;

        DashboardSummaryResponse.AssetHealthDto healthDto = new DashboardSummaryResponse.AssetHealthDto(
                new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(healthy, hPct),
                new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(warning, wPct),
                new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(critical, cPct),
                new DashboardSummaryResponse.AssetHealthDto.HealthItemDto(unknown, uPct)
        );

        double avgCpu = devices.stream()
                .filter(d -> d.getCpuUsagePercent() != null)
                .mapToDouble(d -> d.getCpuUsagePercent().doubleValue())
                .average()
                .orElse(0.0);

        double avgRam = devices.stream()
                .filter(d -> d.getRamUsagePercent() != null)
                .mapToDouble(d -> d.getRamUsagePercent().doubleValue())
                .average()
                .orElse(0.0);

        long assignedAccessoriesCount = (userIds != null && !userIds.isEmpty())
                ? accessoryAssignmentRepository.findByUser_UserIdInAndStatus(userIds, "ACTIVE").size()
                : 0;

        long licenseAllocationsCount = (userIds != null && !userIds.isEmpty())
                ? licenseAssignmentRepository.findByUser_UserIdInAndStatus(userIds, "ACTIVE").size()
                : 0;

        List<Vulnerability> openVulns = vulnerabilities.stream().filter(this::isOpenVulnerability).toList();
        long totalOpenVulns = openVulns.size();
        long critVulns = openVulns.stream().filter(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity())).count();
        long highVulns = openVulns.stream().filter(v -> "HIGH".equalsIgnoreCase(v.getSeverity())).count();
        long medVulns = openVulns.stream().filter(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity())).count();
        long lowVulns = openVulns.stream().filter(v -> "LOW".equalsIgnoreCase(v.getSeverity())).count();
        DashboardSummaryResponse.VulnerabilityOverviewDto vulnOverview = new DashboardSummaryResponse.VulnerabilityOverviewDto(
                totalOpenVulns, critVulns, highVulns, medVulns, lowVulns
        );

        List<AdminDashboardSummaryResponse.DashboardAlertItem> alerts = (userIds != null && !userIds.isEmpty())
                ? notificationRepository.findByUser_UserIdInOrDevice_DeviceIdInOrderByCreatedAtDesc(userIds, deviceIds != null ? deviceIds : Collections.emptyList())
                        .stream().limit(5)
                        .map(n -> new AdminDashboardSummaryResponse.DashboardAlertItem(
                                n.getNotificationId(), n.getTitle(), n.getMessage(), n.getSeverity(), n.getNotificationType(), n.getCreatedAt()
                        )).toList()
                : Collections.emptyList();

        return new ManagerDashboardSummaryResponse(
                assignedEmployeeCount,
                assignedDeviceCount,
                healthDto,
                Math.round(avgCpu * 10.0) / 10.0,
                Math.round(avgRam * 10.0) / 10.0,
                assignedAccessoriesCount,
                licenseAllocationsCount,
                vulnOverview,
                alerts
        );
    }

}
