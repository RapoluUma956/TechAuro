package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.*;
import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceNetworkRepository deviceNetworkRepository;
    private final InstalledSoftwareRepository installedSoftwareRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;

    public AgentService(
            DeviceRepository deviceRepository,
            DeviceNetworkRepository deviceNetworkRepository,
            InstalledSoftwareRepository installedSoftwareRepository,
            VulnerabilityRepository vulnerabilityRepository,
            DeviceAssignmentRepository deviceAssignmentRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceNetworkRepository = deviceNetworkRepository;
        this.installedSoftwareRepository = installedSoftwareRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
    }

    // =========================================================
    // RECEIVE MULTIPLE AGENT REPORTS (BATCH / MULTI-DEVICE)
    // =========================================================
    @Transactional
    public List<Device> receiveReports(List<AgentReportRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Device> processedDevices = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (AgentReportRequest req : requests) {
            if (req == null) {
                continue;
            }
            try {
                Device device = receiveReport(req);
                processedDevices.add(device);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to process agent report for device serial='{}', assetTag='{}': {}",
                        req.getSerialNumber(), req.getAssetTag(), e.getMessage(), e);
            }
        }

        log.info("Batch report processing completed: {} succeeded, {} failed out of {} total",
                successCount, failureCount, requests.size());
        return processedDevices;
    }

    // =========================================================
    // RECEIVE SINGLE AGENT REPORT
    // =========================================================
    @Transactional
    public Device receiveReport(AgentReportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Agent report cannot be null");
        }

        String rawSerial = request.getSerialNumber();
        if (rawSerial == null || rawSerial.isBlank()) {
            throw new IllegalArgumentException("Serial number is required to identify the device");
        }

        String serialNumber = truncate(rawSerial, 100);
        String assetTag = truncate(request.getAssetTag(), 50);
        String brand = truncate(request.getBrand(), 100);
        String model = truncate(request.getModel(), 150);

        log.info("Processing agent report for Serial Number: '{}', Asset Tag: '{}'", serialNumber, assetTag);

        // =====================================================
        // FIND OR INITIALIZE DEVICE (SINGLE ATOMIC UPSERT)
        // =====================================================
        Optional<Device> deviceOptional = deviceRepository.findBySerialNumber(serialNumber);
        if (deviceOptional.isEmpty() && assetTag != null && !assetTag.isBlank()) {
            deviceOptional = deviceRepository.findByAssetTag(assetTag);
        }

        Device device;
        boolean isNew = false;

        if (deviceOptional.isPresent()) {
            device = deviceOptional.get();
            log.info("Existing device found (ID: {}) for serial: {}", device.getDeviceId(), serialNumber);

            // Update brand & model if provided
            if (brand != null && !brand.isBlank()) {
                device.setBrand(brand);
            }
            if (model != null && !model.isBlank()) {
                device.setModel(model);
            }

            // Update asset tag safely without violating uniqueness
            if (assetTag != null && !assetTag.isBlank()) {
                if (device.getAssetTag() == null || !device.getAssetTag().equalsIgnoreCase(assetTag)) {
                    Optional<Device> conflict = deviceRepository.findByAssetTag(assetTag);
                    if (conflict.isEmpty() || conflict.get().getDeviceId().equals(device.getDeviceId())) {
                        device.setAssetTag(assetTag);
                    }
                }
            } else if (device.getAssetTag() == null || device.getAssetTag().isBlank()) {
                device.setAssetTag(truncate("AUTO-" + serialNumber, 50));
            }
        } else {
            isNew = true;
            device = new Device();
            device.setSerialNumber(serialNumber);
            device.setBrand(brand != null && !brand.isBlank() ? brand : "UNKNOWN");
            device.setModel(model != null && !model.isBlank() ? model : "UNKNOWN");

            String resolvedTag = (assetTag != null && !assetTag.isBlank()) ? assetTag : "AUTO-" + serialNumber;
            // Check if tag is in use by another device
            Optional<Device> conflict = deviceRepository.findByAssetTag(resolvedTag);
            if (conflict.isPresent()) {
                resolvedTag = truncate("AUTO-" + serialNumber + "-" + System.currentTimeMillis() % 10000, 50);
            }
            device.setAssetTag(truncate(resolvedTag, 50));
            device.setDeviceStatus("ACTIVE");
            device.setStatus("ACTIVE");
            device.setCreatedAt(LocalDateTime.now());
            log.info("Creating new device record for serial: {}", serialNumber);
        }

        // =====================================================
        // MAP DEVICE IDENTITY & STATUS
        // =====================================================
        if (request.getDeviceName() != null && !request.getDeviceName().isBlank()) {
            device.setDeviceName(truncate(request.getDeviceName(), 255));
        } else if (device.getDeviceName() == null || device.getDeviceName().isBlank()) {
            String fallbackName = (request.getOs() != null && request.getOs().getHostname() != null)
                    ? request.getOs().getHostname()
                    : (model != null && !model.isBlank() ? model : brand);
            device.setDeviceName(truncate(fallbackName, 255));
        }

        device.setDeviceStatus("ACTIVE");
        device.setStatus("ACTIVE");
        device.setAgentInstalled(true);
        device.setConnectionStatus("ONLINE");
        LocalDateTime now = LocalDateTime.now();
        device.setLastSeenAt(now);
        device.setRecordedAt(now);
        device.setUpdatedAt(now);

        // =====================================================
        // MAP CPU TELEMETRY
        // =====================================================
        CpuData cpu = request.getCpu();
        if (cpu != null) {
            device.setCpuName(truncate(cpu.getName(), 200));
            device.setCpuVendor(truncate(cpu.getVendor(), 100));
            device.setCpuCores(cpu.getCores());
            device.setCpuThreads(cpu.getThreads());
            device.setCpuFrequencyMhz(cpu.getFrequencyMhz());
            device.setCpuUsagePercent(toPercentageBigDecimal(cpu.getUsagePercent()));
            device.setCpuTemperature(toBigDecimal(cpu.getTemperature()));
        }

        // =====================================================
        // MAP MEMORY TELEMETRY (GB CAPACITIES & SPECS)
        // =====================================================
        MemoryData memory = request.getMemory();
        if (memory != null) {
            BigDecimal totalGb = memory.getTotalGb() != null
                    ? toBigDecimal(memory.getTotalGb())
                    : bytesToGbBigDecimal(memory.getTotal());
            BigDecimal usedGb = memory.getUsedGb() != null
                    ? toBigDecimal(memory.getUsedGb())
                    : bytesToGbBigDecimal(memory.getUsed());
            BigDecimal availGb = memory.getAvailableGb() != null
                    ? toBigDecimal(memory.getAvailableGb())
                    : bytesToGbBigDecimal(memory.getAvailable());
            BigDecimal swapTotalGb = memory.getSwapTotalGb() != null
                    ? toBigDecimal(memory.getSwapTotalGb())
                    : bytesToGbBigDecimal(memory.getSwapTotal());
            BigDecimal swapUsedGb = memory.getSwapUsedGb() != null
                    ? toBigDecimal(memory.getSwapUsedGb())
                    : bytesToGbBigDecimal(memory.getSwapUsed());

            device.setRamTotalGb(totalGb);
            device.setRamUsedGb(usedGb);
            device.setRamAvailableGb(availGb);
            device.setRamUsagePercent(toPercentageBigDecimal(memory.getUsagePercent()));
            device.setRamType(truncate(memory.getRamType(), 50));
            device.setRamSpeedMhz(memory.getRamSpeedMhz());
            device.setSwapTotalGb(swapTotalGb);
            device.setSwapUsedGb(swapUsedGb);
        }

        // =====================================================
        // MAP STORAGE TELEMETRY (GB CAPACITIES & SSD SPECS)
        // =====================================================
        StorageData storage = request.getStorage();
        if (storage != null) {
            BigDecimal totalGb = storage.getTotalGb() != null
                    ? toBigDecimal(storage.getTotalGb())
                    : bytesToGbBigDecimal(storage.getTotal());
            BigDecimal usedGb = storage.getUsedGb() != null
                    ? toBigDecimal(storage.getUsedGb())
                    : bytesToGbBigDecimal(storage.getUsed());
            BigDecimal availGb = storage.getAvailableGb() != null
                    ? toBigDecimal(storage.getAvailableGb())
                    : bytesToGbBigDecimal(storage.getAvailable());

            device.setStorageTotalGb(totalGb);
            device.setStorageUsedGb(usedGb);
            device.setStorageAvailableGb(availGb);
            device.setStorageUsagePercent(toPercentageBigDecimal(storage.getUsagePercent()));
            device.setStorageType(truncate(storage.getStorageType(), 30));
            device.setStorageModel(truncate(storage.getModel(), 150));
            device.setStorageHealth(truncate(storage.getHealth(), 30));
            device.setSsdType(truncate(storage.getSsdType(), 255));
            device.setSsdModel(truncate(storage.getSsdModel(), 255));
            device.setSsdHealth(truncate(storage.getSsdHealth(), 255));
        }

        // =====================================================
        // MAP BATTERY TELEMETRY
        // =====================================================
        BatteryData battery = request.getBattery();
        if (battery != null) {
            device.setBatteryPercentage(toPercentageBigDecimal(battery.getPercentage()));
            device.setBatteryCharging(battery.getCharging());
            device.setBatteryHealthPercent(toPercentageBigDecimal(battery.getHealthPercent()));
            device.setBatteryCycleCount(battery.getCycleCount());
            device.setPowerSource(truncate(battery.getPowerSource(), 30));
        }

        // =====================================================
        // MAP OPERATING SYSTEM TELEMETRY
        // =====================================================
        OsData os = request.getOs();
        if (os != null) {
            device.setOsName(truncate(os.getName(), 100));
            device.setOsVersion(truncate(os.getVersion(), 100));
            device.setOsBuild(truncate(os.getBuild(), 100));
            device.setOsArchitecture(truncate(os.getArchitecture(), 30));
            device.setHostname(truncate(os.getHostname(), 150));
            device.setBootTime(os.getBootTime());
            device.setUptimeSeconds(os.getUptimeSeconds());
        }

        // =====================================================
        // MAP PRIMARY IP ADDRESSES
        // =====================================================
        if (request.getNetworks() != null) {
            String selectedIpv4 = null;
            String selectedIpv6 = null;

            for (NetworkData net : request.getNetworks()) {
                if (net == null) {
                    continue;
                }
                boolean isPrimary = net.isPrimary();
                String ipv4 = net.getIpv4Address();
                String ipv6 = net.getIpv6Address();

                if (isPrimary) {
                    if (ipv4 != null && !ipv4.isBlank()) {
                        selectedIpv4 = ipv4.trim();
                    }
                    if (ipv6 != null && !ipv6.isBlank()) {
                        selectedIpv6 = ipv6.trim();
                    }
                    break;
                } else {
                    if (selectedIpv4 == null && ipv4 != null && !ipv4.isBlank() && !ipv4.startsWith("127.")) {
                        selectedIpv4 = ipv4.trim();
                    }
                    if (selectedIpv6 == null && ipv6 != null && !ipv6.isBlank() && !ipv6.equalsIgnoreCase("::1")) {
                        selectedIpv6 = ipv6.trim();
                    }
                }
            }

            if (selectedIpv4 != null) {
                device.setIpv4Address(truncate(selectedIpv4, 45));
            }
            if (selectedIpv6 != null) {
                device.setIpv6Address(truncate(selectedIpv6, 45));
            }
        }

        // =====================================================
        // MAP BIOS / FIRMWARE TELEMETRY
        // =====================================================
        BiosData bios = request.getBios();
        if (bios != null) {
            device.setBiosManufacturer(truncate(bios.getManufacturer(), 100));
            device.setBiosVersion(truncate(bios.getVersion(), 100));
            device.setBiosReleaseDate(bios.getReleaseDate());
        }

        // =====================================================
        // MAP SECURITY TELEMETRY
        // =====================================================
        SecurityData security = request.getSecurity();
        if (security != null) {
            device.setBitlockerEnabled(security.getBitlockerEnabled());
            device.setBitlockerProtectionStatus(truncate(security.getBitlockerProtectionStatus(), 30));
            device.setBitlockerVolumeStatus(truncate(security.getBitlockerVolumeStatus(), 30));

            Double bitlockerPct = security.getBitlockerEncryptionPercent() != null
                    ? security.getBitlockerEncryptionPercent()
                    : security.getBitlockerEncryptionPct();
            device.setBitlockerEncryptionPercent(toPercentageBigDecimal(bitlockerPct));
            device.setBitlockerEncryptionPct(device.getBitlockerEncryptionPercent());

            device.setBitlockerEncryptionMethod(truncate(security.getBitlockerEncryptionMethod(), 50));
            device.setBitlockerKeyProtector(truncate(security.getBitlockerKeyProtector(), 100));
            device.setBitlockerVolume(truncate(security.getBitlockerVolume(), 20));
            device.setSecureBootEnabled(security.getSecureBootEnabled());
            device.setTpmPresent(security.getTpmPresent());
            device.setTpmEnabled(security.getTpmEnabled());
            device.setTpmVersion(truncate(security.getTpmVersion(), 30));
        }

        // =====================================================
        // MAP PERFORMANCE TELEMETRY
        // =====================================================
        PerformanceData performance = request.getPerformance();
        if (performance != null) {
            device.setProcessCount(performance.getProcessCount());
            device.setThreadCount(performance.getThreadCount());
        }

        // =====================================================
        // AGENT METADATA
        // =====================================================
        device.setAgentVersion(truncate(request.getAgentVersion(), 50));

        // Auto-link active assignment user if device is not currently assigned
        if (device.getCurrentUserId() == null && serialNumber != null) {
            deviceAssignmentRepository.findBySerialNumberIgnoreCaseAndStatus(serialNumber, "ACTIVE")
                    .ifPresent(activeDa -> {
                        if (activeDa.getUser() != null) {
                            device.setCurrentUserId(activeDa.getUser().getUserId());
                        }
                    });
        }

        // =====================================================
        // SAVE DEVICE (SINGLE ATOMIC SAVE)
        // =====================================================
        Device savedDevice = deviceRepository.saveAndFlush(device);

        // Ensure active assignment links this device entity and serial number
        if (savedDevice.getSerialNumber() != null) {
            deviceAssignmentRepository.findBySerialNumberIgnoreCaseAndStatus(savedDevice.getSerialNumber(), "ACTIVE")
                    .ifPresent(activeDa -> {
                        boolean modified = false;
                        if (activeDa.getDevice() == null || !activeDa.getDevice().getDeviceId().equals(savedDevice.getDeviceId())) {
                            activeDa.setDevice(savedDevice);
                            modified = true;
                        }
                        if (activeDa.getSerialNumber() == null || !activeDa.getSerialNumber().equalsIgnoreCase(savedDevice.getSerialNumber())) {
                            activeDa.setSerialNumber(savedDevice.getSerialNumber());
                            modified = true;
                        }
                        if (modified) {
                            deviceAssignmentRepository.save(activeDa);
                        }
                    });
        }
        log.info("Device saved successfully (ID: {}, isNew: {})", savedDevice.getDeviceId(), isNew);

        // =====================================================
        // SAVE CHILD ENTITIES
        // =====================================================
        saveNetworkInformation(request, savedDevice);
        saveInstalledSoftware(request, savedDevice);
        saveVulnerabilities(request, savedDevice);

        log.info("Agent report completed for Device ID: {}", savedDevice.getDeviceId());
        return savedDevice;
    }

    // =========================================================
    // SAVE NETWORK INFORMATION
    // =========================================================
    private void saveNetworkInformation(AgentReportRequest request, Device device) {
        List<NetworkData> networks = request.getNetworks();
        if (networks == null || networks.isEmpty()) {
            return;
        }

        List<DeviceNetwork> existingNetworks = deviceNetworkRepository.findByDevice_DeviceId(device.getDeviceId());
        Map<String, DeviceNetwork> networkMap = new HashMap<>();
        for (DeviceNetwork en : existingNetworks) {
            if (en.getInterfaceName() != null) {
                networkMap.put(en.getInterfaceName().trim().toLowerCase(), en);
            }
        }

        int savedCount = 0;
        for (NetworkData networkData : networks) {
            if (networkData == null) {
                continue;
            }

            String interfaceName = truncate(networkData.getInterfaceName(), 100);
            if (interfaceName == null || interfaceName.isBlank()) {
                continue;
            }

            String key = interfaceName.toLowerCase();
            DeviceNetwork network = networkMap.get(key);
            if (network == null) {
                network = new DeviceNetwork();
                network.setDevice(device);
            }

            network.setInterfaceName(interfaceName);
            network.setInterfaceType(truncate(networkData.getInterfaceType(), 50));
            network.setMacAddress(truncate(networkData.getMacAddress(), 17));
            network.setIpv4Address(truncate(networkData.getIpv4Address(), 45));
            network.setIpv6Address(truncate(networkData.getIpv6Address(), 45));
            network.setSubnetMask(truncate(networkData.getSubnetMask(), 45));
            network.setGateway(truncate(networkData.getGateway(), 45));

            if (networkData.getDnsServers() != null && !networkData.getDnsServers().isEmpty()) {
                network.setDnsServers(String.join(", ", networkData.getDnsServers()));
            } else {
                network.setDnsServers(null);
            }

            network.setNetworkName(truncate(networkData.getNetworkName(), 150));
            network.setConnectionStatus(truncate(networkData.getConnectionStatus(), 30));
            network.setPrimary(networkData.isPrimary());
            network.setSpeedMbps(networkData.getSpeedMbps());
            network.setLastSeenAt(networkData.getLastSeenAt() != null ? networkData.getLastSeenAt() : LocalDateTime.now());

            DeviceNetwork saved = deviceNetworkRepository.save(network);
            networkMap.put(key, saved);
            savedCount++;
        }

        log.info("Processed {} network interfaces for device ID: {}", savedCount, device.getDeviceId());
    }

    // =========================================================
    // SAVE INSTALLED SOFTWARE
    // =========================================================
    private void saveInstalledSoftware(AgentReportRequest request, Device device) {
        List<SoftwareData> softwareList = request.getInstalledSoftware();
        if (softwareList == null || softwareList.isEmpty()) {
            return;
        }

        List<InstalledSoftware> existingSoftware = installedSoftwareRepository.findByDevice_DeviceId(device.getDeviceId());
        Map<String, InstalledSoftware> existingSoftwareMap = new HashMap<>();

        for (InstalledSoftware existing : existingSoftware) {
            if (existing.getSoftwareName() != null) {
                existingSoftwareMap.put(existing.getSoftwareName().trim().toLowerCase(), existing);
            }
        }

        Set<String> currentSoftwareNames = new HashSet<>();
        int insertedCount = 0;
        int updatedCount = 0;
        int unchangedCount = 0;
        int uninstalledCount = 0;

        for (SoftwareData softwareData : softwareList) {
            if (softwareData == null) {
                continue;
            }

            String softwareName = truncate(softwareData.getSoftwareName(), 255);
            if (softwareName == null || softwareName.isBlank()) {
                continue;
            }

            String softwareKey = softwareName.toLowerCase();
            currentSoftwareNames.add(softwareKey);

            String version = truncate(softwareData.getVersion(), 255);
            String publisher = truncate(softwareData.getPublisher(), 255);
            String installLocation = truncate(softwareData.getInstallLocation(), 1000);

            InstalledSoftware software = existingSoftwareMap.get(softwareKey);

            if (software == null) {
                software = new InstalledSoftware();
                software.setDevice(device);
                software.setSoftwareName(softwareName);
                software.setVersion(version);
                software.setPublisher(publisher);
                software.setInstallLocation(installLocation);
                software.setInstalledAt(toLocalDateTime(softwareData.getInstalledAt()));

                LocalDateTime lastSeen = toLocalDateTime(softwareData.getLastSeenAt());
                software.setLastSeenAt(lastSeen != null ? lastSeen : LocalDateTime.now());
                software.setStatus("INSTALLED");

                InstalledSoftware savedSoftware = installedSoftwareRepository.save(software);
                // CRUCIAL FIX: update map immediately to prevent duplicate insert in same report
                existingSoftwareMap.put(softwareKey, savedSoftware);
                insertedCount++;
            } else {
                boolean versionChanged = !areEqual(software.getVersion(), version);
                boolean publisherChanged = !areEqual(software.getPublisher(), publisher);
                boolean installLocationChanged = !areEqual(software.getInstallLocation(), installLocation);
                boolean wasUninstalled = !"INSTALLED".equalsIgnoreCase(software.getStatus());

                if (versionChanged || publisherChanged || installLocationChanged || wasUninstalled) {
                    software.setVersion(version);
                    software.setPublisher(publisher);
                    software.setInstallLocation(installLocation);

                    if (software.getInstalledAt() == null) {
                        software.setInstalledAt(toLocalDateTime(softwareData.getInstalledAt()));
                    }

                    LocalDateTime lastSeen = toLocalDateTime(softwareData.getLastSeenAt());
                    software.setLastSeenAt(lastSeen != null ? lastSeen : LocalDateTime.now());
                    software.setStatus("INSTALLED");

                    InstalledSoftware savedSoftware = installedSoftwareRepository.save(software);
                    existingSoftwareMap.put(softwareKey, savedSoftware);
                    updatedCount++;
                } else {
                    unchangedCount++;
                }
            }
        }

        // Identify software that disappeared from current complete inventory
        for (InstalledSoftware existing : existingSoftware) {
            if (existing.getSoftwareName() == null) {
                continue;
            }
            String existingKey = existing.getSoftwareName().trim().toLowerCase();
            if (!currentSoftwareNames.contains(existingKey)) {
                if ("INSTALLED".equalsIgnoreCase(existing.getStatus())) {
                    existing.setStatus("UNINSTALLED");
                    installedSoftwareRepository.save(existing);
                    uninstalledCount++;
                }
            }
        }

        log.info("Software synchronization completed: inserted={}, updated={}, unchanged={}, uninstalled={}",
                insertedCount, updatedCount, unchangedCount, uninstalledCount);
    }

    // =========================================================
    // SAVE VULNERABILITIES
    // =========================================================
    private void saveVulnerabilities(AgentReportRequest request, Device device) {
        List<VulnerabilityData> vulnerabilityList = request.getVulnerabilities();
        if (vulnerabilityList == null || vulnerabilityList.isEmpty()) {
            return;
        }

        List<Vulnerability> existingVulnerabilities = vulnerabilityRepository.findByDevice_DeviceId(device.getDeviceId());
        Map<String, Vulnerability> vulnMap = new HashMap<>();

        for (Vulnerability existing : existingVulnerabilities) {
            if (existing.getCveId() != null && !existing.getCveId().isBlank()) {
                vulnMap.put("cve:" + existing.getCveId().trim().toLowerCase(), existing);
            } else if (existing.getTitle() != null && !existing.getTitle().isBlank()) {
                vulnMap.put("title:" + existing.getTitle().trim().toLowerCase(), existing);
            }
        }

        int savedCount = 0;
        for (VulnerabilityData vulnerabilityData : vulnerabilityList) {
            if (vulnerabilityData == null) {
                continue;
            }

            String cveId = truncate(vulnerabilityData.getCveId(), 30);
            String title = truncate(vulnerabilityData.getTitle(), 255);

            if ((cveId == null || cveId.isBlank()) && (title == null || title.isBlank())) {
                continue;
            }

            Vulnerability vulnerability = null;
            String key = null;

            if (cveId != null && !cveId.isBlank()) {
                key = "cve:" + cveId.toLowerCase();
                vulnerability = vulnMap.get(key);
            }

            if (vulnerability == null && title != null && !title.isBlank()) {
                key = "title:" + title.toLowerCase();
                vulnerability = vulnMap.get(key);
            }

            if (vulnerability == null) {
                vulnerability = new Vulnerability();
                vulnerability.setDevice(device);
            }

            vulnerability.setCveId(cveId);
            vulnerability.setTitle(title);
            vulnerability.setDescription(vulnerabilityData.getDescription()); // columnDefinition = TEXT
            String rawSeverity = vulnerabilityData.getSeverity();
            String resolvedSeverity = (rawSeverity == null || rawSeverity.isBlank() || "UNKNOWN".equalsIgnoreCase(rawSeverity))
                    ? "MEDIUM"
                    : rawSeverity;
            vulnerability.setSeverity(truncate(resolvedSeverity, 20));
            vulnerability.setDetectedAt(toLocalDateTime(vulnerabilityData.getDetectedAt()));

            if (vulnerabilityData.getStatus() != null && !vulnerabilityData.getStatus().isBlank()) {
                vulnerability.setStatus(truncate(vulnerabilityData.getStatus(), 20));
            } else if (vulnerability.getStatus() == null) {
                vulnerability.setStatus("OPEN");
            }

            vulnerability.setResolvedAt(toLocalDateTime(vulnerabilityData.getResolvedAt()));

            Vulnerability saved = vulnerabilityRepository.save(vulnerability);
            if (key != null) {
                vulnMap.put(key, saved);
            }
            savedCount++;
        }

        log.info("Processed {} vulnerabilities for device ID: {}", savedCount, device.getDeviceId());
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength).trim();
    }

    private boolean areEqual(String first, String second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.trim().equalsIgnoreCase(second.trim());
    }

    private LocalDateTime toLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e1) {
            try {
                return OffsetDateTime.parse(value).toLocalDateTime();
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(value).atStartOfDay();
                } catch (Exception e3) {
                    log.debug("Unable to parse datetime string: '{}'", value);
                    return null;
                }
            }
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal toPercentageBigDecimal(Double value) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        double clamped = Math.max(0.0, Math.min(100.0, value));
        return BigDecimal.valueOf(clamped).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal bytesToGbBigDecimal(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return null;
        }
        double gb = (double) bytes / (1024.0 * 1024.0 * 1024.0);
        return BigDecimal.valueOf(gb).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
