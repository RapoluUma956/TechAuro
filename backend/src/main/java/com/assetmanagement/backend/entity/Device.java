package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_devices_asset_tag", columnNames = "asset_tag"),
                @UniqueConstraint(name = "uk_devices_serial_number", columnNames = "serial_number")
        },
        indexes = {
                @Index(name = "idx_devices_connection_status", columnList = "connection_status"),
                @Index(name = "idx_devices_last_seen_at", columnList = "last_seen_at"),
                @Index(name = "idx_devices_device_status", columnList = "device_status"),
                @Index(name = "idx_devices_warranty_end_date", columnList = "warranty_end_date")
        }
)
public class Device {

    // ==================== PRIMARY IDENTITY ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "asset_tag", unique = true, length = 50)
    private String assetTag;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "hostname", length = 150)
    private String hostname;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 150)
    private String model;

    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;


    // ==================== OPERATING SYSTEM ====================

    @Column(name = "os_name", length = 100)
    private String osName;

    @Column(name = "os_version", length = 100)
    private String osVersion;

    @Column(name = "os_build", length = 100)
    private String osBuild;

    @Column(name = "os_architecture", length = 30)
    private String osArchitecture;


    // ==================== CPU ====================

    @Column(name = "cpu_name", length = 200)
    private String cpuName;

    @Column(name = "cpu", length = 200)
    private String cpu;

    @Column(name = "cpu_vendor", length = 100)
    private String cpuVendor;

    @Column(name = "cpu_cores")
    private Integer cpuCores;

    @Column(name = "cpu_threads")
    private Integer cpuThreads;

    @Column(name = "cpu_frequency_mhz")
    private Long cpuFrequencyMhz;

    @Column(name = "cpu_usage_percent", precision = 5, scale = 2)
    private BigDecimal cpuUsagePercent;

    @Column(name = "cpu_temperature", precision = 6, scale = 2)
    private BigDecimal cpuTemperature;


    // ==================== RAM (IN GB) ====================

    @Column(name = "ram_total_gb", precision = 10, scale = 2)
    private BigDecimal ramTotalGb;

    @Column(name = "ram_gb", precision = 10, scale = 2)
    private BigDecimal ramGb;

    @Column(name = "memory_total", precision = 10, scale = 2)
    private BigDecimal memoryTotal;

    @Column(name = "memory_used", precision = 10, scale = 2)
    private BigDecimal memoryUsed;

    @Column(name = "memory_available", precision = 10, scale = 2)
    private BigDecimal memoryAvailable;

    @Column(name = "memory_usage_percent", precision = 5, scale = 2)
    private BigDecimal memoryUsagePercent;

    @Column(name = "ram_used_gb", precision = 10, scale = 2)
    private BigDecimal ramUsedGb;

    @Column(name = "ram_available_gb", precision = 10, scale = 2)
    private BigDecimal ramAvailableGb;

    @Column(name = "ram_usage_percent", precision = 5, scale = 2)
    private BigDecimal ramUsagePercent;

    @Column(name = "ram_type", length = 50)
    private String ramType;

    @Column(name = "ram_speed_mhz")
    private Long ramSpeedMhz;


    // ==================== STORAGE (IN GB) ====================

    @Column(name = "storage_total_gb", precision = 10, scale = 2)
    private BigDecimal storageTotalGb;

    @Column(name = "storage_total", precision = 10, scale = 2)
    private BigDecimal storageTotal;

    @Column(name = "storage_used_gb", precision = 10, scale = 2)
    private BigDecimal storageUsedGb;

    @Column(name = "storage_used", precision = 10, scale = 2)
    private BigDecimal storageUsed;

    @Column(name = "storage_available_gb", precision = 10, scale = 2)
    private BigDecimal storageAvailableGb;

    @Column(name = "storage_available", precision = 10, scale = 2)
    private BigDecimal storageAvailable;

    @Column(name = "storage_usage_percent", precision = 5, scale = 2)
    private BigDecimal storageUsagePercent;

    @Column(name = "storage_type", length = 30)
    private String storageType;

    @Column(name = "storage_model", length = 150)
    private String storageModel;

    @Column(name = "storage_health", length = 30)
    private String storageHealth;


    // ==================== SSD DETAILS ====================

    @Column(name = "ssd_type", length = 255)
    private String ssdType;

    @Column(name = "ssd_model", length = 255)
    private String ssdModel;

    @Column(name = "ssd_health", length = 255)
    private String ssdHealth;

    @Column(name = "ssd_capacity_gb", precision = 10, scale = 2)
    private BigDecimal ssdCapacityGb;

    @Column(name = "ssd_available_gb", precision = 10, scale = 2)
    private BigDecimal ssdAvailableGb;

    @Column(name = "ssd_used_gb", precision = 10, scale = 2)
    private BigDecimal ssdUsedGb;

    @Column(name = "ssd_usage_percent", precision = 5, scale = 2)
    private BigDecimal ssdUsagePercent;


    // ==================== SWAP MEMORY (IN GB) ====================

    @Column(name = "swap_total_gb", precision = 10, scale = 2)
    private BigDecimal swapTotalGb;

    @Column(name = "swap_total", precision = 10, scale = 2)
    private BigDecimal swapTotal;

    @Column(name = "swap_used_gb", precision = 10, scale = 2)
    private BigDecimal swapUsedGb;

    @Column(name = "swap_used", precision = 10, scale = 2)
    private BigDecimal swapUsed;


    // ==================== BATTERY ====================

    @Column(name = "battery_percentage", precision = 5, scale = 2)
    private BigDecimal batteryPercentage;

    @Column(name = "battery_health_percent", precision = 5, scale = 2)
    private BigDecimal batteryHealthPercent;

    @Column(name = "battery_cycle_count")
    private Integer batteryCycleCount;

    @Column(name = "battery_charging")
    private Boolean batteryCharging;

    @Column(name = "power_source", length = 30)
    private String powerSource;


    // ==================== NETWORK (PRIMARY) ====================

    @Column(name = "ipv4_address", length = 45)
    private String ipv4Address;

    @Column(name = "ipv6_address", length = 45)
    private String ipv6Address;


    // ==================== BIOS AND SECURITY ====================

    @Column(name = "bios_manufacturer", length = 100)
    private String biosManufacturer;

    @Column(name = "bios_version", length = 100)
    private String biosVersion;

    @Column(name = "bios_release_date")
    private LocalDate biosReleaseDate;

    @Column(name = "secure_boot_enabled")
    private Boolean secureBootEnabled;

    @Column(name = "tpm_present")
    private Boolean tpmPresent;

    @Column(name = "tpm_enabled")
    private Boolean tpmEnabled;

    @Column(name = "tpm_version", length = 30)
    private String tpmVersion;

    @Column(name = "bitlocker_enabled")
    private Boolean bitlockerEnabled;

    @Column(name = "bitlocker_protection_status", length = 30)
    private String bitlockerProtectionStatus;

    @Column(name = "bitlocker_volume_status", length = 30)
    private String bitlockerVolumeStatus;

    @Column(name = "bitlocker_encryption_method", length = 50)
    private String bitlockerEncryptionMethod;

    @Column(name = "bitlocker_encryption_percent", precision = 5, scale = 2)
    private BigDecimal bitlockerEncryptionPercent;

    @Column(name = "bitlocker_encryption_pct", precision = 5, scale = 2)
    private BigDecimal bitlockerEncryptionPct;

    @Column(name = "bitlocker_key_protector", length = 100)
    private String bitlockerKeyProtector;

    @Column(name = "bitlocker_volume", length = 20)
    private String bitlockerVolume;


    // ==================== SYSTEM ACTIVITY ====================

    @Column(name = "boot_time")
    private LocalDateTime bootTime;

    @Column(name = "uptime_seconds")
    private Long uptimeSeconds;

    @Column(name = "process_count")
    private Integer processCount;

    @Column(name = "thread_count")
    private Integer threadCount;


    // ==================== ASSET LIFECYCLE ====================

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "warranty_start_date")
    private LocalDate warrantyStartDate;

    @Column(name = "warranty_end_date")
    private LocalDate warrantyEndDate;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "asset_id", length = 100)
    private String assetId;

    @Column(name = "asset_status", length = 50)
    private String assetStatus;

    @Column(name = "current_user_id")
    private Long currentUserId;


    // ==================== AGENT ====================

    @Column(name = "agent_installed", nullable = false)
    private Boolean agentInstalled = false;

    @Column(name = "agent_version", length = 50)
    private String agentVersion;


    // ==================== DEVICE STATUS & MONITORING ====================

    @Column(name = "device_status", length = 30)
    private String deviceStatus;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "connection_status", length = 30)
    private String connectionStatus;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;


    // ==================== AUDIT TIMESTAMPS ====================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // ==================== JPA LIFECYCLE ====================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;

        if (deviceStatus == null && status != null) {
            deviceStatus = status;
        } else if (status == null && deviceStatus != null) {
            status = deviceStatus;
        }
        if (deviceStatus == null || deviceStatus.isBlank()) {
            deviceStatus = "ACTIVE";
            status = "ACTIVE";
        }

        if (agentInstalled == null) {
            agentInstalled = false;
        }

        syncAliases();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        syncAliases();
    }

    private void syncAliases() {
        if (cpu == null && cpuName != null) {
            cpu = cpuName;
        } else if (cpuName == null && cpu != null) {
            cpuName = cpu;
        }

        if (ramGb == null && ramTotalGb != null) {
            ramGb = ramTotalGb;
        } else if (ramTotalGb == null && ramGb != null) {
            ramTotalGb = ramGb;
        }

        if (memoryTotal == null) {
            memoryTotal = ramTotalGb;
        }
        if (memoryUsed == null) {
            memoryUsed = ramUsedGb;
        }
        if (memoryAvailable == null) {
            memoryAvailable = ramAvailableGb;
        }
        if (memoryUsagePercent == null) {
            memoryUsagePercent = ramUsagePercent;
        }

        if (storageTotal == null) {
            storageTotal = storageTotalGb;
        }
        if (storageUsed == null) {
            storageUsed = storageUsedGb;
        }
        if (storageAvailable == null) {
            storageAvailable = storageAvailableGb;
        }

        if (swapTotal == null) {
            swapTotal = swapTotalGb;
        }
        if (swapUsed == null) {
            swapUsed = swapUsedGb;
        }

        if (bitlockerEncryptionPct == null && bitlockerEncryptionPercent != null) {
            bitlockerEncryptionPct = bitlockerEncryptionPercent;
        } else if (bitlockerEncryptionPercent == null && bitlockerEncryptionPct != null) {
            bitlockerEncryptionPercent = bitlockerEncryptionPct;
        }

        if (warrantyExpiryDate == null && warrantyEndDate != null) {
            warrantyExpiryDate = warrantyEndDate;
        } else if (warrantyEndDate == null && warrantyExpiryDate != null) {
            warrantyEndDate = warrantyExpiryDate;
        }

        if (assetStatus == null && deviceStatus != null) {
            assetStatus = deviceStatus;
        }

        if (assetId == null && assetTag != null) {
            assetId = assetTag;
        }

        if (status == null && deviceStatus != null) {
            status = deviceStatus;
        } else if (deviceStatus == null && status != null) {
            deviceStatus = status;
        }
    }


    // ==================== GETTERS & SETTERS ====================

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }

    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }

    public String getOsBuild() { return osBuild; }
    public void setOsBuild(String osBuild) { this.osBuild = osBuild; }

    public String getOsArchitecture() { return osArchitecture; }
    public void setOsArchitecture(String osArchitecture) { this.osArchitecture = osArchitecture; }

    public String getCpuName() { return cpuName; }
    public void setCpuName(String cpuName) { this.cpuName = cpuName; }

    public String getCpuVendor() { return cpuVendor; }
    public void setCpuVendor(String cpuVendor) { this.cpuVendor = cpuVendor; }

    public Integer getCpuCores() { return cpuCores; }
    public void setCpuCores(Integer cpuCores) { this.cpuCores = cpuCores; }

    public Integer getCpuThreads() { return cpuThreads; }
    public void setCpuThreads(Integer cpuThreads) { this.cpuThreads = cpuThreads; }

    public Long getCpuFrequencyMhz() { return cpuFrequencyMhz; }
    public void setCpuFrequencyMhz(Long cpuFrequencyMhz) { this.cpuFrequencyMhz = cpuFrequencyMhz; }

    public BigDecimal getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(BigDecimal cpuUsagePercent) { this.cpuUsagePercent = cpuUsagePercent; }

    public BigDecimal getCpuTemperature() { return cpuTemperature; }
    public void setCpuTemperature(BigDecimal cpuTemperature) { this.cpuTemperature = cpuTemperature; }

    public BigDecimal getRamTotalGb() { return ramTotalGb; }
    public void setRamTotalGb(BigDecimal ramTotalGb) { this.ramTotalGb = ramTotalGb; }

    public BigDecimal getRamUsedGb() { return ramUsedGb; }
    public void setRamUsedGb(BigDecimal ramUsedGb) { this.ramUsedGb = ramUsedGb; }

    public BigDecimal getRamAvailableGb() { return ramAvailableGb; }
    public void setRamAvailableGb(BigDecimal ramAvailableGb) { this.ramAvailableGb = ramAvailableGb; }

    public BigDecimal getRamUsagePercent() { return ramUsagePercent; }
    public void setRamUsagePercent(BigDecimal ramUsagePercent) { this.ramUsagePercent = ramUsagePercent; }

    public String getRamType() { return ramType; }
    public void setRamType(String ramType) { this.ramType = ramType; }

    public Long getRamSpeedMhz() { return ramSpeedMhz; }
    public void setRamSpeedMhz(Long ramSpeedMhz) { this.ramSpeedMhz = ramSpeedMhz; }

    public BigDecimal getStorageTotalGb() { return storageTotalGb; }
    public void setStorageTotalGb(BigDecimal storageTotalGb) { this.storageTotalGb = storageTotalGb; }

    public BigDecimal getStorageUsedGb() { return storageUsedGb; }
    public void setStorageUsedGb(BigDecimal storageUsedGb) { this.storageUsedGb = storageUsedGb; }

    public BigDecimal getStorageAvailableGb() { return storageAvailableGb; }
    public void setStorageAvailableGb(BigDecimal storageAvailableGb) { this.storageAvailableGb = storageAvailableGb; }

    public BigDecimal getStorageUsagePercent() { return storageUsagePercent; }
    public void setStorageUsagePercent(BigDecimal storageUsagePercent) { this.storageUsagePercent = storageUsagePercent; }

    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }

    public String getStorageModel() { return storageModel; }
    public void setStorageModel(String storageModel) { this.storageModel = storageModel; }

    public String getStorageHealth() { return storageHealth; }
    public void setStorageHealth(String storageHealth) { this.storageHealth = storageHealth; }

    public String getSsdType() { return ssdType; }
    public void setSsdType(String ssdType) { this.ssdType = ssdType; }

    public String getSsdModel() { return ssdModel; }
    public void setSsdModel(String ssdModel) { this.ssdModel = ssdModel; }

    public String getSsdHealth() { return ssdHealth; }
    public void setSsdHealth(String ssdHealth) { this.ssdHealth = ssdHealth; }

    public BigDecimal getSwapTotalGb() { return swapTotalGb; }
    public void setSwapTotalGb(BigDecimal swapTotalGb) { this.swapTotalGb = swapTotalGb; }

    public BigDecimal getSwapUsedGb() { return swapUsedGb; }
    public void setSwapUsedGb(BigDecimal swapUsedGb) { this.swapUsedGb = swapUsedGb; }

    public BigDecimal getBatteryPercentage() { return batteryPercentage; }
    public void setBatteryPercentage(BigDecimal batteryPercentage) { this.batteryPercentage = batteryPercentage; }

    public BigDecimal getBatteryHealthPercent() { return batteryHealthPercent; }
    public void setBatteryHealthPercent(BigDecimal batteryHealthPercent) { this.batteryHealthPercent = batteryHealthPercent; }

    public Integer getBatteryCycleCount() { return batteryCycleCount; }
    public void setBatteryCycleCount(Integer batteryCycleCount) { this.batteryCycleCount = batteryCycleCount; }

    public Boolean getBatteryCharging() { return batteryCharging; }
    public void setBatteryCharging(Boolean batteryCharging) { this.batteryCharging = batteryCharging; }

    public String getPowerSource() { return powerSource; }
    public void setPowerSource(String powerSource) { this.powerSource = powerSource; }

    public String getIpv4Address() { return ipv4Address; }
    public void setIpv4Address(String ipv4Address) { this.ipv4Address = ipv4Address; }

    public String getIpv6Address() { return ipv6Address; }
    public void setIpv6Address(String ipv6Address) { this.ipv6Address = ipv6Address; }

    public String getBiosManufacturer() { return biosManufacturer; }
    public void setBiosManufacturer(String biosManufacturer) { this.biosManufacturer = biosManufacturer; }

    public String getBiosVersion() { return biosVersion; }
    public void setBiosVersion(String biosVersion) { this.biosVersion = biosVersion; }

    public LocalDate getBiosReleaseDate() { return biosReleaseDate; }
    public void setBiosReleaseDate(LocalDate biosReleaseDate) { this.biosReleaseDate = biosReleaseDate; }

    public Boolean getSecureBootEnabled() { return secureBootEnabled; }
    public void setSecureBootEnabled(Boolean secureBootEnabled) { this.secureBootEnabled = secureBootEnabled; }

    public Boolean getTpmPresent() { return tpmPresent; }
    public void setTpmPresent(Boolean tpmPresent) { this.tpmPresent = tpmPresent; }

    public Boolean getTpmEnabled() { return tpmEnabled; }
    public void setTpmEnabled(Boolean tpmEnabled) { this.tpmEnabled = tpmEnabled; }

    public String getTpmVersion() { return tpmVersion; }
    public void setTpmVersion(String tpmVersion) { this.tpmVersion = tpmVersion; }

    public Boolean getBitlockerEnabled() { return bitlockerEnabled; }
    public void setBitlockerEnabled(Boolean bitlockerEnabled) { this.bitlockerEnabled = bitlockerEnabled; }

    public String getBitlockerProtectionStatus() { return bitlockerProtectionStatus; }
    public void setBitlockerProtectionStatus(String bitlockerProtectionStatus) {
        this.bitlockerProtectionStatus = bitlockerProtectionStatus;
    }

    public String getBitlockerVolumeStatus() { return bitlockerVolumeStatus; }
    public void setBitlockerVolumeStatus(String bitlockerVolumeStatus) {
        this.bitlockerVolumeStatus = bitlockerVolumeStatus;
    }

    public String getBitlockerEncryptionMethod() { return bitlockerEncryptionMethod; }
    public void setBitlockerEncryptionMethod(String bitlockerEncryptionMethod) {
        this.bitlockerEncryptionMethod = bitlockerEncryptionMethod;
    }

    public BigDecimal getBitlockerEncryptionPercent() { return bitlockerEncryptionPercent; }
    public void setBitlockerEncryptionPercent(BigDecimal bitlockerEncryptionPercent) {
        this.bitlockerEncryptionPercent = bitlockerEncryptionPercent;
    }

    public String getBitlockerKeyProtector() { return bitlockerKeyProtector; }
    public void setBitlockerKeyProtector(String bitlockerKeyProtector) {
        this.bitlockerKeyProtector = bitlockerKeyProtector;
    }

    public String getBitlockerVolume() { return bitlockerVolume; }
    public void setBitlockerVolume(String bitlockerVolume) { this.bitlockerVolume = bitlockerVolume; }

    public LocalDateTime getBootTime() { return bootTime; }
    public void setBootTime(LocalDateTime bootTime) { this.bootTime = bootTime; }

    public Long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(Long uptimeSeconds) { this.uptimeSeconds = uptimeSeconds; }

    public Integer getProcessCount() { return processCount; }
    public void setProcessCount(Integer processCount) { this.processCount = processCount; }

    public Integer getThreadCount() { return threadCount; }
    public void setThreadCount(Integer threadCount) { this.threadCount = threadCount; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDate getWarrantyStartDate() { return warrantyStartDate; }
    public void setWarrantyStartDate(LocalDate warrantyStartDate) {
        this.warrantyStartDate = warrantyStartDate;
    }

    public LocalDate getWarrantyEndDate() { return warrantyEndDate; }
    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public Boolean getAgentInstalled() { return agentInstalled; }
    public void setAgentInstalled(Boolean agentInstalled) { this.agentInstalled = agentInstalled; }

    public String getAgentVersion() { return agentVersion; }
    public void setAgentVersion(String agentVersion) { this.agentVersion = agentVersion; }

    public String getDeviceStatus() { return deviceStatus; }
    public void setDeviceStatus(String deviceStatus) { this.deviceStatus = deviceStatus; }

    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }

    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }


    // ==================== ERD ALIASES & ADDITIONAL GETTERS / SETTERS ====================

    public String getStatus() {
        return (status != null && !status.isBlank()) ? status : deviceStatus;
    }

    public void setStatus(String status) {
        this.status = status;
        this.deviceStatus = status;
    }

    public String getCpu() {
        return (cpu != null && !cpu.isBlank()) ? cpu : cpuName;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
        if (this.cpuName == null || this.cpuName.isBlank()) {
            this.cpuName = cpu;
        }
    }

    public BigDecimal getRamGb() {
        return (ramGb != null) ? ramGb : ramTotalGb;
    }

    public void setRamGb(BigDecimal ramGb) {
        this.ramGb = ramGb;
        if (this.ramTotalGb == null) {
            this.ramTotalGb = ramGb;
        }
    }

    public BigDecimal getMemoryTotal() {
        return (memoryTotal != null) ? memoryTotal : ramTotalGb;
    }

    public void setMemoryTotal(BigDecimal memoryTotal) {
        this.memoryTotal = memoryTotal;
        if (this.ramTotalGb == null) {
            this.ramTotalGb = memoryTotal;
        }
    }

    public BigDecimal getMemoryUsed() {
        return (memoryUsed != null) ? memoryUsed : ramUsedGb;
    }

    public void setMemoryUsed(BigDecimal memoryUsed) {
        this.memoryUsed = memoryUsed;
        if (this.ramUsedGb == null) {
            this.ramUsedGb = memoryUsed;
        }
    }

    public BigDecimal getMemoryAvailable() {
        return (memoryAvailable != null) ? memoryAvailable : ramAvailableGb;
    }

    public void setMemoryAvailable(BigDecimal memoryAvailable) {
        this.memoryAvailable = memoryAvailable;
        if (this.ramAvailableGb == null) {
            this.ramAvailableGb = memoryAvailable;
        }
    }

    public BigDecimal getMemoryUsagePercent() {
        return (memoryUsagePercent != null) ? memoryUsagePercent : ramUsagePercent;
    }

    public void setMemoryUsagePercent(BigDecimal memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
        if (this.ramUsagePercent == null) {
            this.ramUsagePercent = memoryUsagePercent;
        }
    }

    public BigDecimal getStorageTotal() {
        return (storageTotal != null) ? storageTotal : storageTotalGb;
    }

    public void setStorageTotal(BigDecimal storageTotal) {
        this.storageTotal = storageTotal;
        if (this.storageTotalGb == null) {
            this.storageTotalGb = storageTotal;
        }
    }

    public BigDecimal getStorageUsed() {
        return (storageUsed != null) ? storageUsed : storageUsedGb;
    }

    public void setStorageUsed(BigDecimal storageUsed) {
        this.storageUsed = storageUsed;
        if (this.storageUsedGb == null) {
            this.storageUsedGb = storageUsed;
        }
    }

    public BigDecimal getStorageAvailable() {
        return (storageAvailable != null) ? storageAvailable : storageAvailableGb;
    }

    public void setStorageAvailable(BigDecimal storageAvailable) {
        this.storageAvailable = storageAvailable;
        if (this.storageAvailableGb == null) {
            this.storageAvailableGb = storageAvailable;
        }
    }

    public BigDecimal getSwapTotal() {
        return (swapTotal != null) ? swapTotal : swapTotalGb;
    }

    public void setSwapTotal(BigDecimal swapTotal) {
        this.swapTotal = swapTotal;
        if (this.swapTotalGb == null) {
            this.swapTotalGb = swapTotal;
        }
    }

    public BigDecimal getSwapUsed() {
        return (swapUsed != null) ? swapUsed : swapUsedGb;
    }

    public void setSwapUsed(BigDecimal swapUsed) {
        this.swapUsed = swapUsed;
        if (this.swapUsedGb == null) {
            this.swapUsedGb = swapUsed;
        }
    }

    public BigDecimal getSsdCapacityGb() {
        return ssdCapacityGb;
    }

    public void setSsdCapacityGb(BigDecimal ssdCapacityGb) {
        this.ssdCapacityGb = ssdCapacityGb;
    }

    public BigDecimal getSsdAvailableGb() {
        return ssdAvailableGb;
    }

    public void setSsdAvailableGb(BigDecimal ssdAvailableGb) {
        this.ssdAvailableGb = ssdAvailableGb;
    }

    public BigDecimal getSsdUsedGb() {
        return ssdUsedGb;
    }

    public void setSsdUsedGb(BigDecimal ssdUsedGb) {
        this.ssdUsedGb = ssdUsedGb;
    }

    public BigDecimal getSsdUsagePercent() {
        return ssdUsagePercent;
    }

    public void setSsdUsagePercent(BigDecimal ssdUsagePercent) {
        this.ssdUsagePercent = ssdUsagePercent;
    }

    public BigDecimal getBitlockerEncryptionPct() {
        return (bitlockerEncryptionPct != null) ? bitlockerEncryptionPct : bitlockerEncryptionPercent;
    }

    public void setBitlockerEncryptionPct(BigDecimal bitlockerEncryptionPct) {
        this.bitlockerEncryptionPct = bitlockerEncryptionPct;
        if (this.bitlockerEncryptionPercent == null) {
            this.bitlockerEncryptionPercent = bitlockerEncryptionPct;
        }
    }

    public LocalDate getWarrantyExpiryDate() {
        return (warrantyExpiryDate != null) ? warrantyExpiryDate : warrantyEndDate;
    }

    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) {
        this.warrantyExpiryDate = warrantyExpiryDate;
        if (this.warrantyEndDate == null) {
            this.warrantyEndDate = warrantyExpiryDate;
        }
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getAssetId() {
        return (assetId != null && !assetId.isBlank()) ? assetId : assetTag;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
        if (this.assetTag == null || this.assetTag.isBlank()) {
            this.assetTag = assetId;
        }
    }

    public String getAssetStatus() {
        return (assetStatus != null && !assetStatus.isBlank()) ? assetStatus : deviceStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
        if (this.deviceStatus == null || this.deviceStatus.isBlank()) {
            this.deviceStatus = assetStatus;
        }
    }
}
