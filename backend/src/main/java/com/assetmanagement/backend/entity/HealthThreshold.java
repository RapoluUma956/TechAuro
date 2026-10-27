package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_thresholds")
public class HealthThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "threshold_id")
    private Long thresholdId;

    @Column(name = "cpu_warning_percent", nullable = false)
    private Integer cpuWarningPercent = 90;

    @Column(name = "ram_warning_percent", nullable = false)
    private Integer ramWarningPercent = 90;

    @Column(name = "disk_critical_percent", nullable = false)
    private Integer diskCriticalPercent = 90;

    @Column(name = "offline_timeout_minutes", nullable = false)
    private Integer offlineTimeoutMinutes = 5;

    @Column(name = "warranty_warning_days", nullable = false)
    private Integer warrantyWarningDays = 30;

    @Column(name = "license_warning_days", nullable = false)
    private Integer licenseWarningDays = 30;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public HealthThreshold() {
    }

    public HealthThreshold(Integer cpuWarningPercent, Integer ramWarningPercent, Integer diskCriticalPercent,
                           Integer offlineTimeoutMinutes, Integer warrantyWarningDays, Integer licenseWarningDays) {
        this.cpuWarningPercent = cpuWarningPercent;
        this.ramWarningPercent = ramWarningPercent;
        this.diskCriticalPercent = diskCriticalPercent;
        this.offlineTimeoutMinutes = offlineTimeoutMinutes;
        this.warrantyWarningDays = warrantyWarningDays;
        this.licenseWarningDays = licenseWarningDays;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getThresholdId() {
        return thresholdId;
    }

    public void setThresholdId(Long thresholdId) {
        this.thresholdId = thresholdId;
    }

    public Integer getCpuWarningPercent() {
        return cpuWarningPercent;
    }

    public void setCpuWarningPercent(Integer cpuWarningPercent) {
        this.cpuWarningPercent = cpuWarningPercent;
    }

    public Integer getRamWarningPercent() {
        return ramWarningPercent;
    }

    public void setRamWarningPercent(Integer ramWarningPercent) {
        this.ramWarningPercent = ramWarningPercent;
    }

    public Integer getDiskCriticalPercent() {
        return diskCriticalPercent;
    }

    public void setDiskCriticalPercent(Integer diskCriticalPercent) {
        this.diskCriticalPercent = diskCriticalPercent;
    }

    public Integer getOfflineTimeoutMinutes() {
        return offlineTimeoutMinutes;
    }

    public void setOfflineTimeoutMinutes(Integer offlineTimeoutMinutes) {
        this.offlineTimeoutMinutes = offlineTimeoutMinutes;
    }

    public Integer getWarrantyWarningDays() {
        return warrantyWarningDays;
    }

    public void setWarrantyWarningDays(Integer warrantyWarningDays) {
        this.warrantyWarningDays = warrantyWarningDays;
    }

    public Integer getLicenseWarningDays() {
        return licenseWarningDays;
    }

    public void setLicenseWarningDays(Integer licenseWarningDays) {
        this.licenseWarningDays = licenseWarningDays;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
