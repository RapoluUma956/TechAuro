
package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "devices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_devices_asset_tag",
                        columnNames = "asset_tag"
                ),
                @UniqueConstraint(
                        name = "uk_devices_serial_number",
                        columnNames = "serial_number"
                )
        }
)
public class Device {

    // ==================== DEVICE INFORMATION ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "asset_tag", nullable = false, unique = true, length = 50)
    private String assetTag;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model", nullable = false, length = 150)
    private String model;

    @Column(name = "warranty_start_date")
    private LocalDate warrantyStartDate;

    @Column(name = "warranty_end_date")
    private LocalDate warrantyEndDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // ==================== CPU INFORMATION ====================

    @Column(name = "cpu_name", length = 200)
    private String cpuName;

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


    // ==================== MEMORY INFORMATION ====================

    @Column(name = "memory_total")
    private Long memoryTotal;

    @Column(name = "memory_used")
    private Long memoryUsed;

    @Column(name = "memory_available")
    private Long memoryAvailable;

    @Column(name = "memory_usage_percent", precision = 5, scale = 2)
    private BigDecimal memoryUsagePercent;

    @Column(name = "swap_total")
    private Long swapTotal;

    @Column(name = "swap_used")
    private Long swapUsed;


    // ==================== STORAGE INFORMATION ====================

    @Column(name = "storage_total")
    private Long storageTotal;

    @Column(name = "storage_used")
    private Long storageUsed;

    @Column(name = "storage_available")
    private Long storageAvailable;

    @Column(name = "storage_usage_percent", precision = 5, scale = 2)
    private BigDecimal storageUsagePercent;

    @Column(name = "storage_type", length = 30)
    private String storageType;

    @Column(name = "storage_model", length = 150)
    private String storageModel;

    @Column(name = "storage_health", length = 30)
    private String storageHealth;


    // ==================== BATTERY INFORMATION ====================

    @Column(name = "battery_percentage", precision = 5, scale = 2)
    private BigDecimal batteryPercentage;

    @Column(name = "battery_charging")
    private Boolean batteryCharging;

    @Column(name = "battery_health_percent", precision = 5, scale = 2)
    private BigDecimal batteryHealthPercent;

    @Column(name = "battery_cycle_count")
    private Integer batteryCycleCount;

    @Column(name = "power_source", length = 30)
    private String powerSource;


    // ==================== OPERATING SYSTEM ====================

    @Column(name = "os_name", length = 100)
    private String osName;

    @Column(name = "os_version", length = 100)
    private String osVersion;

    @Column(name = "os_build", length = 100)
    private String osBuild;

    @Column(name = "os_architecture", length = 30)
    private String osArchitecture;

    @Column(name = "hostname", length = 150)
    private String hostname;


    // ==================== BITLOCKER INFORMATION ====================

    @Column(name = "bitlocker_enabled")
    private Boolean bitlockerEnabled;

    @Column(name = "bitlocker_protection_status", length = 30)
    private String bitlockerProtectionStatus;

    @Column(name = "bitlocker_volume_status", length = 30)
    private String bitlockerVolumeStatus;

    @Column(name = "bitlocker_encryption_pct", precision = 5, scale = 2)
    private BigDecimal bitlockerEncryptionPct;

    @Column(name = "bitlocker_encryption_method", length = 50)
    private String bitlockerEncryptionMethod;

    @Column(name = "bitlocker_key_protector", length = 100)
    private String bitlockerKeyProtector;

    @Column(name = "bitlocker_volume", length = 20)
    private String bitlockerVolume;


    // ==================== BIOS / FIRMWARE ====================

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


    // ==================== SYSTEM RUNTIME ====================

    @Column(name = "uptime_seconds")
    private Long uptimeSeconds;

    @Column(name = "boot_time")
    private LocalDateTime bootTime;


    // ==================== PERFORMANCE ====================

    @Column(name = "process_count")
    private Integer processCount;

    @Column(name = "thread_count")
    private Integer threadCount;


    // ==================== AGENT INFORMATION ====================

    @Column(name = "connection_status", length = 30)
    private String connectionStatus;

    @Column(name = "agent_version", length = 50)
    private String agentVersion;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;


    // ==================== GETTERS AND SETTERS ====================

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public LocalDate getWarrantyStartDate() { return warrantyStartDate; }
    public void setWarrantyStartDate(LocalDate warrantyStartDate) {
        this.warrantyStartDate = warrantyStartDate;
    }

    public LocalDate getWarrantyEndDate() { return warrantyEndDate; }
    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCpuName() { return cpuName; }
    public void setCpuName(String cpuName) { this.cpuName = cpuName; }

    public String getCpuVendor() { return cpuVendor; }
    public void setCpuVendor(String cpuVendor) { this.cpuVendor = cpuVendor; }

    public Integer getCpuCores() { return cpuCores; }
    public void setCpuCores(Integer cpuCores) { this.cpuCores = cpuCores; }

    public Integer getCpuThreads() { return cpuThreads; }
    public void setCpuThreads(Integer cpuThreads) { this.cpuThreads = cpuThreads; }

    public Long getCpuFrequencyMhz() { return cpuFrequencyMhz; }
    public void setCpuFrequencyMhz(Long cpuFrequencyMhz) {
        this.cpuFrequencyMhz = cpuFrequencyMhz;
    }

    public BigDecimal getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(BigDecimal cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public BigDecimal getCpuTemperature() { return cpuTemperature; }
    public void setCpuTemperature(BigDecimal cpuTemperature) {
        this.cpuTemperature = cpuTemperature;
    }

    public Long getMemoryTotal() { return memoryTotal; }
    public void setMemoryTotal(Long memoryTotal) { this.memoryTotal = memoryTotal; }

    public Long getMemoryUsed() { return memoryUsed; }
    public void setMemoryUsed(Long memoryUsed) { this.memoryUsed = memoryUsed; }

    public Long getMemoryAvailable() { return memoryAvailable; }
    public void setMemoryAvailable(Long memoryAvailable) {
        this.memoryAvailable = memoryAvailable;
    }

    public BigDecimal getMemoryUsagePercent() { return memoryUsagePercent; }
    public void setMemoryUsagePercent(BigDecimal memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
    }

    public Long getSwapTotal() { return swapTotal; }
    public void setSwapTotal(Long swapTotal) { this.swapTotal = swapTotal; }

    public Long getSwapUsed() { return swapUsed; }
    public void setSwapUsed(Long swapUsed) { this.swapUsed = swapUsed; }

    public Long getStorageTotal() { return storageTotal; }
    public void setStorageTotal(Long storageTotal) { this.storageTotal = storageTotal; }

    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }

    public Long getStorageAvailable() { return storageAvailable; }
    public void setStorageAvailable(Long storageAvailable) {
        this.storageAvailable = storageAvailable;
    }

    public BigDecimal getStorageUsagePercent() { return storageUsagePercent; }
    public void setStorageUsagePercent(BigDecimal storageUsagePercent) {
        this.storageUsagePercent = storageUsagePercent;
    }

    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }

    public String getStorageModel() { return storageModel; }
    public void setStorageModel(String storageModel) { this.storageModel = storageModel; }

    public String getStorageHealth() { return storageHealth; }
    public void setStorageHealth(String storageHealth) { this.storageHealth = storageHealth; }

    public BigDecimal getBatteryPercentage() { return batteryPercentage; }
    public void setBatteryPercentage(BigDecimal batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public Boolean getBatteryCharging() { return batteryCharging; }
    public void setBatteryCharging(Boolean batteryCharging) {
        this.batteryCharging = batteryCharging;
    }

    public BigDecimal getBatteryHealthPercent() { return batteryHealthPercent; }
    public void setBatteryHealthPercent(BigDecimal batteryHealthPercent) {
        this.batteryHealthPercent = batteryHealthPercent;
    }

    public Integer getBatteryCycleCount() { return batteryCycleCount; }
    public void setBatteryCycleCount(Integer batteryCycleCount) {
        this.batteryCycleCount = batteryCycleCount;
    }

    public String getPowerSource() { return powerSource; }
    public void setPowerSource(String powerSource) { this.powerSource = powerSource; }

    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }

    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }

    public String getOsBuild() { return osBuild; }
    public void setOsBuild(String osBuild) { this.osBuild = osBuild; }

    public String getOsArchitecture() { return osArchitecture; }
    public void setOsArchitecture(String osArchitecture) {
        this.osArchitecture = osArchitecture;
    }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public Boolean getBitlockerEnabled() { return bitlockerEnabled; }
    public void setBitlockerEnabled(Boolean bitlockerEnabled) {
        this.bitlockerEnabled = bitlockerEnabled;
    }

    public String getBitlockerProtectionStatus() { return bitlockerProtectionStatus; }
    public void setBitlockerProtectionStatus(String bitlockerProtectionStatus) {
        this.bitlockerProtectionStatus = bitlockerProtectionStatus;
    }

    public String getBitlockerVolumeStatus() { return bitlockerVolumeStatus; }
    public void setBitlockerVolumeStatus(String bitlockerVolumeStatus) {
        this.bitlockerVolumeStatus = bitlockerVolumeStatus;
    }

    public BigDecimal getBitlockerEncryptionPct() { return bitlockerEncryptionPct; }
    public void setBitlockerEncryptionPct(BigDecimal bitlockerEncryptionPct) {
        this.bitlockerEncryptionPct = bitlockerEncryptionPct;
    }

    public String getBitlockerEncryptionMethod() { return bitlockerEncryptionMethod; }
    public void setBitlockerEncryptionMethod(String bitlockerEncryptionMethod) {
        this.bitlockerEncryptionMethod = bitlockerEncryptionMethod;
    }

    public String getBitlockerKeyProtector() { return bitlockerKeyProtector; }
    public void setBitlockerKeyProtector(String bitlockerKeyProtector) {
        this.bitlockerKeyProtector = bitlockerKeyProtector;
    }

    public String getBitlockerVolume() { return bitlockerVolume; }
    public void setBitlockerVolume(String bitlockerVolume) {
        this.bitlockerVolume = bitlockerVolume;
    }

    public String getBiosManufacturer() { return biosManufacturer; }
    public void setBiosManufacturer(String biosManufacturer) {
        this.biosManufacturer = biosManufacturer;
    }

    public String getBiosVersion() { return biosVersion; }
    public void setBiosVersion(String biosVersion) { this.biosVersion = biosVersion; }

    public LocalDate getBiosReleaseDate() { return biosReleaseDate; }
    public void setBiosReleaseDate(LocalDate biosReleaseDate) {
        this.biosReleaseDate = biosReleaseDate;
    }

    public Boolean getSecureBootEnabled() { return secureBootEnabled; }
    public void setSecureBootEnabled(Boolean secureBootEnabled) {
        this.secureBootEnabled = secureBootEnabled;
    }

    public Boolean getTpmPresent() { return tpmPresent; }
    public void setTpmPresent(Boolean tpmPresent) { this.tpmPresent = tpmPresent; }

    public Boolean getTpmEnabled() { return tpmEnabled; }
    public void setTpmEnabled(Boolean tpmEnabled) { this.tpmEnabled = tpmEnabled; }

    public String getTpmVersion() { return tpmVersion; }
    public void setTpmVersion(String tpmVersion) { this.tpmVersion = tpmVersion; }

    public Long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(Long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public LocalDateTime getBootTime() { return bootTime; }
    public void setBootTime(LocalDateTime bootTime) { this.bootTime = bootTime; }

    public Integer getProcessCount() { return processCount; }
    public void setProcessCount(Integer processCount) { this.processCount = processCount; }

    public Integer getThreadCount() { return threadCount; }
    public void setThreadCount(Integer threadCount) { this.threadCount = threadCount; }

    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getAgentVersion() { return agentVersion; }
    public void setAgentVersion(String agentVersion) { this.agentVersion = agentVersion; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }


    // ==================== JPA LIFECYCLE METHODS ====================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
