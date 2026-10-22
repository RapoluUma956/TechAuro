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


    // =========================================================
    // JPA LIFECYCLE METHODS
    // =========================================================

    @PrePersist
    protected void onCreate() {

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        if (lastSeenAt == null) {
            lastSeenAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {

        lastSeenAt = LocalDateTime.now();
    }
}