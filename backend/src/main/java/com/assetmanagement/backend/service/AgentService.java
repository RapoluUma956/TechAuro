package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.assetmanagement.backend.dto.AgentReportRequest;
import com.assetmanagement.backend.dto.BatteryData;
import com.assetmanagement.backend.dto.BiosData;
import com.assetmanagement.backend.dto.CpuData;
import com.assetmanagement.backend.dto.MemoryData;
import com.assetmanagement.backend.dto.OsData;
import com.assetmanagement.backend.dto.SecurityData;
import com.assetmanagement.backend.dto.SoftwareData;
import com.assetmanagement.backend.dto.StorageData;
import com.assetmanagement.backend.dto.VulnerabilityData;
import com.assetmanagement.backend.dto.NetworkData;
import com.assetmanagement.backend.dto.PerformanceData;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AgentService {

    private final DeviceRepository deviceRepository;

    private final DeviceNetworkRepository deviceNetworkRepository;

    private final InstalledSoftwareRepository installedSoftwareRepository;

    private final VulnerabilityRepository vulnerabilityRepository;


    public AgentService(
            DeviceRepository deviceRepository,
            DeviceNetworkRepository deviceNetworkRepository,
            InstalledSoftwareRepository installedSoftwareRepository,
            VulnerabilityRepository vulnerabilityRepository) {

        this.deviceRepository =
                deviceRepository;

        this.deviceNetworkRepository =
                deviceNetworkRepository;

        this.installedSoftwareRepository =
                installedSoftwareRepository;

        this.vulnerabilityRepository =
                vulnerabilityRepository;
    }


    // =========================================================
    // RECEIVE AGENT REPORT
    // =========================================================

    @Transactional
    public void receiveReport(
            AgentReportRequest request) {

        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "          PROCESSING AGENT REPORT"
        );

        System.out.println(
                "=============================================="
        );


        // =====================================================
        // VALIDATE REQUEST
        // =====================================================

        if (request == null) {

            throw new IllegalArgumentException(
                    "Agent report cannot be null"
            );
        }


        // =====================================================
        // DEVICE IDENTITY
        // =====================================================

        String serialNumber =
                request.getSerialNumber();

        String assetTag =
                request.getAssetTag();

        String brand =
                request.getBrand();

        String model =
                request.getModel();


        System.out.println();

        System.out.println(
                "Serial Number : "
                        + serialNumber
        );

        System.out.println(
                "Asset Tag     : "
                        + assetTag
        );

        System.out.println(
                "Brand         : "
                        + brand
        );

        System.out.println(
                "Model         : "
                        + model
        );


        // =====================================================
        // VALIDATE SERIAL NUMBER
        // =====================================================

        if (serialNumber == null ||
                serialNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Serial number is required to identify the device"
            );
        }


        // =====================================================
        // FIND DEVICE
        // =====================================================

        Optional<Device> deviceOptional =
                deviceRepository.findBySerialNumber(
                        serialNumber
                );


        Device device;


        // =====================================================
        // CREATE DEVICE IF NOT FOUND
        // =====================================================

        if (deviceOptional.isEmpty()) {

            System.out.println();

            System.out.println(
                    "Device not found."
            );

            System.out.println(
                    "Creating new device..."
            );


            device =
                    new Device();


            device.setSerialNumber(
                    serialNumber.trim()
            );


            if (brand != null &&
                    !brand.isBlank()) {

                device.setBrand(
                        brand.trim()
                );

            } else {

                device.setBrand(
                        "UNKNOWN"
                );
            }


            if (model != null &&
                    !model.isBlank()) {

                device.setModel(
                        model.trim()
                );

            } else {

                device.setModel(
                        "UNKNOWN"
                );
            }


            if (assetTag == null ||
                    assetTag.isBlank()) {

                assetTag =
                        "AUTO-"
                                + serialNumber.trim();
            }


            device.setAssetTag(
                    assetTag.trim()
            );


            /*
             * Device.java @PrePersist automatically
             * sets:
             *
             * status = ACTIVE
             * createdAt
             * updatedAt
             */

            device =
                    deviceRepository.save(
                            device
                    );


            System.out.println();

            System.out.println(
                    "New device created successfully."
            );

        } else {

            // =================================================
            // EXISTING DEVICE
            // =================================================

            device =
                    deviceOptional.get();


            System.out.println();

            System.out.println(
                    "Existing device found."
            );


            /*
             * Update identity information if the
             * agent provides it.
             */

            if (brand != null &&
                    !brand.isBlank()) {

                device.setBrand(
                        brand.trim()
                );
            }


            if (model != null &&
                    !model.isBlank()) {

                device.setModel(
                        model.trim()
                );
            }


            if (assetTag != null &&
                    !assetTag.isBlank()) {

                device.setAssetTag(
                        assetTag.trim()
                );
            }


            device =
                    deviceRepository.save(
                            device
                    );
        }


        // =====================================================
        // DEVICE INFORMATION
        // =====================================================

        System.out.println();

        System.out.println(
                "Device ID     : "
                        + device.getDeviceId()
        );

        System.out.println(
                "Device Asset  : "
                        + device.getAssetTag()
        );

        System.out.println(
                "Device Serial : "
                        + device.getSerialNumber()
        );


        // Monitoring values are now stored directly on Device.

        // =====================================================
        // CPU
        // =====================================================

        CpuData cpu =
                request.getCpu();


        if (cpu != null) {

            device.setCpuName(
                    cpu.getName()
            );

            device.setCpuVendor(
                    cpu.getVendor()
            );

            device.setCpuCores(
                    cpu.getCores()
            );

            device.setCpuThreads(
                    cpu.getThreads()
            );

            device.setCpuFrequencyMhz(
                    cpu.getFrequencyMhz()
            );

            device.setCpuUsagePercent(
                    toBigDecimal(
                            cpu.getUsagePercent()
                    )
            );

            device.setCpuTemperature(
                    toBigDecimal(
                            cpu.getTemperature()
                    )
            );
        }


        // =====================================================
        // MEMORY
        // =====================================================

        MemoryData memory =
                request.getMemory();


        if (memory != null) {

            device.setMemoryTotal(
                    memory.getTotal()
            );

            device.setMemoryUsed(
                    memory.getUsed()
            );

            device.setMemoryAvailable(
                    memory.getAvailable()
            );

            device.setMemoryUsagePercent(
                    toBigDecimal(
                            memory.getUsagePercent()
                    )
            );

            device.setSwapTotal(
                    memory.getSwapTotal()
            );

            device.setSwapUsed(
                    memory.getSwapUsed()
            );
        }


        // =====================================================
        // STORAGE
        // =====================================================

        StorageData storage =
                request.getStorage();


        if (storage != null) {

            device.setStorageTotal(
                    storage.getTotal()
            );

            device.setStorageUsed(
                    storage.getUsed()
            );

            device.setStorageAvailable(
                    storage.getAvailable()
            );

            device.setStorageUsagePercent(
                    toBigDecimal(
                            storage.getUsagePercent()
                    )
            );

            device.setStorageType(
                    storage.getStorageType()
            );

            device.setStorageModel(
                    storage.getModel()
            );

            device.setStorageHealth(
                    storage.getHealth()
            );
        }


        // =====================================================
        // BATTERY
        // =====================================================

        BatteryData battery =
                request.getBattery();


        if (battery != null) {

            device.setBatteryPercentage(
                    toBigDecimal(
                            battery.getPercentage()
                    )
            );

            device.setBatteryCharging(
                    battery.getCharging()
            );

            device.setBatteryHealthPercent(
                    toBigDecimal(
                            battery.getHealthPercent()
                    )
            );

            device.setBatteryCycleCount(
                    battery.getCycleCount()
            );

            device.setPowerSource(
                    battery.getPowerSource()
            );
        }


        // =====================================================
        // OPERATING SYSTEM
        // =====================================================

        OsData os =
                request.getOs();


        if (os != null) {

            device.setOsName(
                    os.getName()
            );

            device.setOsVersion(
                    os.getVersion()
            );

            device.setOsBuild(
                    os.getBuild()
            );

            device.setOsArchitecture(
                    os.getArchitecture()
            );

            device.setHostname(
                    os.getHostname()
            );

            device.setBootTime(
                    os.getBootTime()
            );

            device.setUptimeSeconds(
                    os.getUptimeSeconds()
            );
        }


        // =====================================================
        // BIOS
        // =====================================================

        BiosData bios =
                request.getBios();


        if (bios != null) {

            device.setBiosManufacturer(
                    bios.getManufacturer()
            );

            device.setBiosVersion(
                    bios.getVersion()
            );

            device.setBiosReleaseDate(
                    bios.getReleaseDate()
            );
        }


        // =====================================================
        // SECURITY
        // =====================================================

        SecurityData security =
                request.getSecurity();


        if (security != null) {

            device.setBitlockerEnabled(
                    security.getBitlockerEnabled()
            );

            device.setBitlockerProtectionStatus(
                    security.getBitlockerProtectionStatus()
            );

            device.setBitlockerVolumeStatus(
                    security.getBitlockerVolumeStatus()
            );

            device.setBitlockerEncryptionPct(
                    toBigDecimal(
                            security.getBitlockerEncryptionPct()
                    )
            );

            device.setBitlockerEncryptionMethod(
                    security.getBitlockerEncryptionMethod()
            );

            device.setBitlockerKeyProtector(
                    security.getBitlockerKeyProtector()
            );

            device.setBitlockerVolume(
                    security.getBitlockerVolume()
            );

            device.setSecureBootEnabled(
                    security.getSecureBootEnabled()
            );

            device.setTpmPresent(
                    security.getTpmPresent()
            );

            device.setTpmEnabled(
                    security.getTpmEnabled()
            );

            device.setTpmVersion(
                    security.getTpmVersion()
            );
        }


        // =====================================================
        // PERFORMANCE
        // =====================================================

        PerformanceData performance =
                request.getPerformance();


        if (performance != null) {

            device.setProcessCount(
                    performance.getProcessCount()
            );

            device.setThreadCount(
                    performance.getThreadCount()
            );
        }


        // =====================================================
        // AGENT INFORMATION
        // =====================================================

        device.setAgentVersion(
                request.getAgentVersion()
        );

        device.setConnectionStatus(
                "CONNECTED"
        );

        // The latest monitoring timestamp is saved on Device.
        device.setRecordedAt(
                LocalDateTime.now()
        );


        // =====================================================
        // SAVE DEVICE WITH LATEST MONITORING VALUES
        // =====================================================

        device.setRecordedAt(LocalDateTime.now());
        device.setAgentVersion(request.getAgentVersion());
        device.setConnectionStatus("CONNECTED");

        Device savedDevice = deviceRepository.save(device);

        System.out.println();
        System.out.println("==============================================");
        System.out.println("       DEVICE DATA SAVED SUCCESSFULLY");
        System.out.println("==============================================");
        System.out.println("Device ID     : " + savedDevice.getDeviceId());
        System.out.println("Serial Number : " + savedDevice.getSerialNumber());
        System.out.println("Asset Tag     : " + savedDevice.getAssetTag());
        System.out.println("Recorded At   : " + savedDevice.getRecordedAt());


        // =====================================================
        // SAVE NETWORKS
        // =====================================================

        saveNetworkInformation(
                request,
                device
        );


        // =====================================================
        // SAVE INSTALLED SOFTWARE
        // =====================================================

        saveInstalledSoftware(
                request,
                device
        );


        // =====================================================
        // SAVE VULNERABILITIES
        // =====================================================

        saveVulnerabilities(
                request,
                device
        );


        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "          AGENT REPORT COMPLETED"
        );

        System.out.println(
                "=============================================="
        );
    }


    // =========================================================
    // SAVE NETWORK INFORMATION
    // =========================================================

    private void saveNetworkInformation(
            AgentReportRequest request,
            Device device) {

        List<NetworkData> networks =
                request.getNetworks();


        if (networks == null ||
                networks.isEmpty()) {

            System.out.println();

            System.out.println(
                    "No network information received."
            );

            return;
        }


        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "       SAVING NETWORK INFORMATION"
        );

        System.out.println(
                "=============================================="
        );


        List<DeviceNetwork> existingNetworks =
                deviceNetworkRepository
                        .findByDevice_DeviceId(
                                device.getDeviceId()
                        );


        int savedCount = 0;


        for (
                NetworkData networkData :
                networks) {


            if (networkData == null) {
                continue;
            }


            String interfaceName =
                    networkData.getInterfaceName();


            DeviceNetwork network =
                    null;


            // =================================================
            // FIND EXISTING NETWORK
            // =================================================

            if (interfaceName != null &&
                    !interfaceName.isBlank()) {

                for (
                        DeviceNetwork existingNetwork :
                        existingNetworks) {


                    if (
                            existingNetwork
                                    .getInterfaceName()
                                    != null
                                    &&
                                    existingNetwork
                                            .getInterfaceName()
                                            .equalsIgnoreCase(
                                                    interfaceName
                                            )
                    ) {

                        network =
                                existingNetwork;

                        break;
                    }
                }
            }


            // =================================================
            // CREATE NEW NETWORK
            // =================================================

            if (network == null) {

                network =
                        new DeviceNetwork();
            }


            network.setDevice(
                    device
            );


            network.setInterfaceName(
                    networkData.getInterfaceName()
            );

            network.setInterfaceType(
                    networkData.getInterfaceType()
            );

            network.setMacAddress(
                    networkData.getMacAddress()
            );

            network.setIpv4Address(
                    networkData.getIpv4Address()
            );

            network.setIpv6Address(
                    networkData.getIpv6Address()
            );

            network.setSubnetMask(
                    networkData.getSubnetMask()
            );

            network.setGateway(
                    networkData.getGateway()
            );


            // =================================================
            // DNS SERVERS
            // =================================================

            if (networkData.getDnsServers() != null &&
                    !networkData.getDnsServers().isEmpty()) {

                network.setDnsServers(
                        String.join(
                                ", ",
                                networkData.getDnsServers()
                        )
                );

            } else {

                network.setDnsServers(
                        null
                );
            }


            network.setNetworkName(
                    networkData.getNetworkName()
            );

            network.setConnectionStatus(
                    networkData.getConnectionStatus()
            );


            network.setPrimary(
                    networkData.isPrimary()
            );


            network.setSpeedMbps(
                    networkData.getSpeedMbps()
            );

            network.setLastSeenAt(
                    networkData.getLastSeenAt()
            );


            DeviceNetwork savedNetwork =
                    deviceNetworkRepository.save(
                            network
                    );


            savedCount++;


            System.out.println();

            System.out.println(
                    "Network saved: "
                            + savedNetwork
                            .getInterfaceName()
            );

            System.out.println(
                    "  Type       : "
                            + savedNetwork
                            .getInterfaceType()
            );

            System.out.println(
                    "  MAC        : "
                            + savedNetwork
                            .getMacAddress()
            );

            System.out.println(
                    "  IPv4       : "
                            + savedNetwork
                            .getIpv4Address()
            );

            System.out.println(
                    "  IPv6       : "
                            + savedNetwork
                            .getIpv6Address()
            );

            System.out.println(
                    "  Gateway    : "
                            + savedNetwork
                            .getGateway()
            );

            System.out.println(
                    "  Status     : "
                            + savedNetwork
                            .getConnectionStatus()
            );
        }


        System.out.println();

        System.out.println(
                "Networks processed: "
                        + savedCount
        );
    }


    // =========================================================
    // SAVE INSTALLED SOFTWARE
    // =========================================================

    private void saveInstalledSoftware(
            AgentReportRequest request,
            Device device) {

        List<SoftwareData> softwareList =
                request.getInstalledSoftware();


        /*
         * The agent sends the full current software inventory on every cycle.
         * A null/empty list is not sufficient evidence that every application
         * was uninstalled, so do not change database statuses in that case.
         * The collector throws on registry-source failures, preventing a
         * partial scan from being sent as a complete inventory.
         */

        if (softwareList == null ||
                softwareList.isEmpty()) {

            System.out.println();

            System.out.println(
                    "No software changes to process."
            );

            return;
        }


        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "       SYNCHRONIZING INSTALLED SOFTWARE"
        );

        System.out.println(
                "=============================================="
        );


        // =====================================================
        // LOAD CURRENT DATABASE SOFTWARE
        // =====================================================

        List<InstalledSoftware> existingSoftware =
                installedSoftwareRepository
                        .findByDevice_DeviceId(
                                device.getDeviceId()
                        );


        /*
         * Create a map using software name as the
         * software identity for this device.
         *
         * Example:
         *
         * Chrome     -> InstalledSoftware
         * Git        -> InstalledSoftware
         * DBeaver    -> InstalledSoftware
         */

        Map<String, InstalledSoftware> existingSoftwareMap =
                new HashMap<>();


        for (
                InstalledSoftware existing :
                existingSoftware) {

            if (existing.getSoftwareName() == null) {
                continue;
            }

            existingSoftwareMap.put(
                    existing.getSoftwareName()
                            .trim()
                            .toLowerCase(),
                    existing
            );
        }


        // =====================================================
        // TRACK SOFTWARE PRESENT IN CURRENT AGENT SCAN
        // =====================================================

        Set<String> currentSoftwareNames =
                new HashSet<>();


        int insertedCount = 0;

        int updatedCount = 0;

        int unchangedCount = 0;

        int uninstalledCount = 0;


        // =====================================================
        // PROCESS CURRENT SOFTWARE
        // =====================================================

        for (
                SoftwareData softwareData :
                softwareList) {


            if (softwareData == null) {
                continue;
            }


            String softwareName =
                    softwareData.getSoftwareName();


            if (softwareName == null ||
                    softwareName.isBlank()) {

                continue;
            }


            softwareName =
                    softwareName.trim();


            String softwareKey =
                    softwareName.toLowerCase();


            currentSoftwareNames.add(
                    softwareKey
            );


            // =================================================
            // FIND EXISTING SOFTWARE
            // =================================================

            InstalledSoftware software =
                    existingSoftwareMap.get(
                            softwareKey
                    );


            // =================================================
            // NEW SOFTWARE
            // =================================================

            if (software == null) {

                software =
                        new InstalledSoftware();


                software.setDevice(
                        device
                );

                software.setSoftwareName(
                        softwareName
                );

                software.setVersion(
                        softwareData.getVersion()
                );

                software.setPublisher(
                        softwareData.getPublisher()
                );

                software.setInstallLocation(
                        softwareData.getInstallLocation()
                );

                software.setInstalledAt(
                        toLocalDateTime(
                                softwareData.getInstalledAt()
                        )
                );

                software.setLastSeenAt(
                        toLocalDateTime(
                                softwareData.getLastSeenAt()
                        )
                );

                software.setStatus(
                        "INSTALLED"
                );


                InstalledSoftware savedSoftware =
                        installedSoftwareRepository.save(
                                software
                        );


                insertedCount++;


                System.out.println();

                System.out.println(
                        "NEW SOFTWARE INSERTED: "
                                + savedSoftware
                                .getSoftwareName()
                                + " "
                                + savedSoftware
                                .getVersion()
                );


                continue;
            }


            // =================================================
            // EXISTING SOFTWARE
            // =================================================

            boolean versionChanged =
                    !areEqual(
                            software.getVersion(),
                            softwareData.getVersion()
                    );


            boolean publisherChanged =
                    !areEqual(
                            software.getPublisher(),
                            softwareData.getPublisher()
                    );


            boolean installLocationChanged =
                    !areEqual(
                            software.getInstallLocation(),
                            softwareData.getInstallLocation()
                    );


            boolean wasUninstalled =
                    !"INSTALLED".equalsIgnoreCase(
                            software.getStatus()
                    );


            // =================================================
            // SOFTWARE CHANGED / REINSTALLED
            // =================================================

            if (versionChanged ||
                    publisherChanged ||
                    installLocationChanged ||
                    wasUninstalled) {


                software.setVersion(
                        softwareData.getVersion()
                );

                software.setPublisher(
                        softwareData.getPublisher()
                );

                software.setInstallLocation(
                        softwareData.getInstallLocation()
                );


                /*
                 * Do not overwrite the original
                 * installation date when an existing
                 * software record is updated.
                 */

                if (software.getInstalledAt() == null) {

                    software.setInstalledAt(
                            toLocalDateTime(
                                    softwareData.getInstalledAt()
                            )
                    );
                }


                /*
                 * lastSeenAt is updated ONLY when
                 * the software actually changed or
                 * was previously uninstalled.
                 */

                LocalDateTime lastSeen =
                        toLocalDateTime(
                                softwareData.getLastSeenAt()
                        );


                if (lastSeen != null) {

                    software.setLastSeenAt(
                            lastSeen
                    );

                } else {

                    software.setLastSeenAt(
                            LocalDateTime.now()
                    );
                }


                software.setStatus(
                        "INSTALLED"
                );


                InstalledSoftware savedSoftware =
                        installedSoftwareRepository.save(
                                software
                        );


                updatedCount++;


                System.out.println();

                System.out.println(
                        "SOFTWARE UPDATED: "
                                + savedSoftware
                                .getSoftwareName()
                                + " "
                                + savedSoftware
                                .getVersion()
                );


            } else {

                // =================================================
                // EXISTING SOFTWARE - NO CHANGE
                // =================================================

                unchangedCount++;


                System.out.println();

                System.out.println(
                        "SOFTWARE UNCHANGED: "
                                + software
                                .getSoftwareName()
                );
            }
        }


        // =====================================================
        // FIND SOFTWARE THAT DISAPPEARED
        // =====================================================

        for (
                InstalledSoftware existing :
                existingSoftware) {


            if (existing.getSoftwareName() == null) {
                continue;
            }


            String existingKey =
                    existing.getSoftwareName()
                            .trim()
                            .toLowerCase();


            /*
             * If software exists in DB but was not
             * returned by the current complete scan,
             * it has been removed/uninstalled.
             */

            if (!currentSoftwareNames.contains(
                    existingKey
            )) {


                /*
                 * Only change the record if it is
                 * currently marked INSTALLED.
                 */

                if ("INSTALLED".equalsIgnoreCase(
                        existing.getStatus()
                )) {

                    existing.setStatus(
                            "UNINSTALLED"
                    );


                    InstalledSoftware savedSoftware =
                            installedSoftwareRepository.save(
                                    existing
                            );


                    uninstalledCount++;


                    System.out.println();

                    System.out.println(
                            "SOFTWARE UNINSTALLED: "
                                    + savedSoftware
                                    .getSoftwareName()
                    );
                }
            }
        }


        // =====================================================
        // SOFTWARE SUMMARY
        // =====================================================

        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "       SOFTWARE SYNCHRONIZATION COMPLETED"
        );

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "New software inserted : "
                        + insertedCount
        );

        System.out.println(
                "Software updated      : "
                        + updatedCount
        );

        System.out.println(
                "Software unchanged    : "
                        + unchangedCount
        );

        System.out.println(
                "Software uninstalled  : "
                        + uninstalledCount
        );
    }


    // =========================================================
    // SAVE VULNERABILITIES
    // =========================================================

    private void saveVulnerabilities(
            AgentReportRequest request,
            Device device) {

        List<VulnerabilityData> vulnerabilityList =
                request.getVulnerabilities();


        if (vulnerabilityList == null ||
                vulnerabilityList.isEmpty()) {

            System.out.println();

            System.out.println(
                    "No vulnerabilities received."
            );

            return;
        }


        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "       SAVING VULNERABILITIES"
        );

        System.out.println(
                "=============================================="
        );


        List<Vulnerability> existingVulnerabilities =
                vulnerabilityRepository
                        .findByDevice_DeviceId(
                                device.getDeviceId()
                        );


        int savedCount = 0;


        for (
                VulnerabilityData vulnerabilityData :
                vulnerabilityList) {


            if (vulnerabilityData == null) {
                continue;
            }


            Vulnerability vulnerability =
                    null;


            String cveId =
                    vulnerabilityData.getCveId();

            String title =
                    vulnerabilityData.getTitle();


            // =================================================
            // FIND EXISTING VULNERABILITY
            // =================================================

            for (
                    Vulnerability existing :
                    existingVulnerabilities) {


                boolean matchesCve =
                        cveId != null
                                &&
                                !cveId.isBlank()
                                &&
                                existing.getCveId() != null
                                &&
                                existing.getCveId()
                                        .equalsIgnoreCase(
                                                cveId
                                        );


                boolean matchesTitle =
                        (cveId == null ||
                                cveId.isBlank())
                                &&
                                title != null
                                &&
                                !title.isBlank()
                                &&
                                existing.getTitle() != null
                                &&
                                existing.getTitle()
                                        .equalsIgnoreCase(
                                                title
                                        );


                if (matchesCve ||
                        matchesTitle) {

                    vulnerability =
                            existing;

                    break;
                }
            }


            // =================================================
            // CREATE NEW VULNERABILITY
            // =================================================

            if (vulnerability == null) {

                vulnerability =
                        new Vulnerability();
            }


            vulnerability.setDevice(
                    device
            );

            vulnerability.setCveId(
                    vulnerabilityData.getCveId()
            );

            vulnerability.setTitle(
                    vulnerabilityData.getTitle()
            );

            vulnerability.setDescription(
                    vulnerabilityData.getDescription()
            );

            vulnerability.setSeverity(
                    vulnerabilityData.getSeverity()
            );


            // =================================================
            // STRING → LOCALDATETIME
            // =================================================

            vulnerability.setDetectedAt(
                    toLocalDateTime(
                            vulnerabilityData
                                    .getDetectedAt()
                    )
            );


            // =================================================
            // STATUS
            // =================================================

            if (vulnerabilityData.getStatus() != null &&
                    !vulnerabilityData.getStatus().isBlank()) {

                vulnerability.setStatus(
                        vulnerabilityData.getStatus()
                );
            }


            vulnerability.setResolvedAt(
                    toLocalDateTime(
                            vulnerabilityData
                                    .getResolvedAt()
                    )
            );


            Vulnerability savedVulnerability =
                    vulnerabilityRepository.save(
                            vulnerability
                    );


            savedCount++;


            System.out.println();

            System.out.println(
                    "Vulnerability saved: "
                            +
                            (
                                    savedVulnerability
                                            .getCveId()
                                            != null
                                            &&
                                            !savedVulnerability
                                                    .getCveId()
                                                    .isBlank()
                                            ?
                                            savedVulnerability
                                                    .getCveId()
                                            :
                                            savedVulnerability
                                                    .getTitle()
                            )
            );
        }


        System.out.println();

        System.out.println(
                "Vulnerabilities processed: "
                        + savedCount
        );
    }


    // =========================================================
    // STRING COMPARISON
    // =========================================================

    private boolean areEqual(
            String first,
            String second) {

        if (first == null &&
                second == null) {

            return true;
        }


        if (first == null ||
                second == null) {

            return false;
        }


        return first.trim()
                .equalsIgnoreCase(
                        second.trim()
                );
    }


    // =========================================================
    // STRING → LOCALDATETIME
    // =========================================================

    private LocalDateTime toLocalDateTime(
            String value) {

        if (value == null ||
                value.isBlank()) {

            return null;
        }


        try {

            return LocalDateTime.parse(
                    value
            );

        } catch (Exception e) {

            System.out.println();

            System.out.println(
                    "Invalid LocalDateTime value: "
                            + value
            );

            return null;
        }
    }


    // =========================================================
    // DOUBLE → BIGDECIMAL
    // =========================================================

    private BigDecimal toBigDecimal(
            Double value) {

        if (value == null) {

            return null;
        }


        return BigDecimal.valueOf(
                value
        );
    }
}

