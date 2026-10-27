package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "installed_software",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_device_software",
                        columnNames = {
                                "device_id",
                                "software_name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_installed_software_device",
                        columnList = "device_id"
                ),
                @Index(
                        name = "idx_installed_software_name",
                        columnList = "software_name"
                )
        }
)
public class InstalledSoftware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "software_id")
    private Long softwareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "device_id",
            nullable = false
    )
    private Device device;

    @Column(
            name = "software_name",
            nullable = false,
            length = 255
    )
    private String softwareName;

    @Column(
            name = "version",
            length = 255
    )
    private String version;

    @Column(
            name = "publisher",
            length = 255
    )
    private String publisher;

    @Column(
            name = "install_location",
            length = 1000
    )
    private String installLocation;

    @Column(name = "installed_at")
    private LocalDateTime installedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;

    @Column(name = "software_type", length = 100)
    private String softwareType;

    @Column(name = "uninstall_string", length = 1000)
    private String uninstallString;

    @Column(name = "install_date")
    private java.time.LocalDate installDate;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public Long getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(Long softwareId) {
        this.softwareId = softwareId;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getInstallLocation() {
        return installLocation;
    }

    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSoftwareType() {
        return softwareType;
    }

    public void setSoftwareType(String softwareType) {
        this.softwareType = softwareType;
    }

    public String getUninstallString() {
        return uninstallString;
    }

    public void setUninstallString(String uninstallString) {
        this.uninstallString = uninstallString;
    }

    public java.time.LocalDate getInstallDate() {
        return installDate;
    }

    public void setInstallDate(java.time.LocalDate installDate) {
        this.installDate = installDate;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    // =========================================================
    // JPA LIFECYCLE METHODS
    // =========================================================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (detectedAt == null) {
            detectedAt = now;
        }

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        if (lastSeenAt == null) {
            lastSeenAt = now;
        }

        if (installedAt != null && installDate == null) {
            installDate = installedAt.toLocalDate();
        } else if (installDate != null && installedAt == null) {
            installedAt = installDate.atStartOfDay();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastSeenAt = LocalDateTime.now();
    }
}