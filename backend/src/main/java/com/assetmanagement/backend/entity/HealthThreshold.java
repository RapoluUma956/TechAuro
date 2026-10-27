package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
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
        this.cpuWarningPercent = cpuWarningPercent != null ? cpuWarningPercent : 90;
        this.ramWarningPercent = ramWarningPercent != null ? ramWarningPercent : 90;
        this.diskCriticalPercent = diskCriticalPercent != null ? diskCriticalPercent : 90;
        this.offlineTimeoutMinutes = offlineTimeoutMinutes != null ? offlineTimeoutMinutes : 5;
        this.warrantyWarningDays = warrantyWarningDays != null ? warrantyWarningDays : 30;
        this.licenseWarningDays = licenseWarningDays != null ? licenseWarningDays : 30;
        this.updatedAt = LocalDateTime.now();
    }

    public HealthThreshold(BigDecimal cpuWarningPercent, BigDecimal ramWarningPercent, BigDecimal diskCriticalPercent,
                           Integer offlineTimeoutMinutes, Integer warrantyWarningDays, Integer licenseWarningDays) {
        this(
            cpuWarningPercent != null ? cpuWarningPercent.intValue() : 90,
            ramWarningPercent != null ? ramWarningPercent.intValue() : 90,
            diskCriticalPercent != null ? diskCriticalPercent.intValue() : 90,
            offlineTimeoutMinutes,
            warrantyWarningDays,
            licenseWarningDays
        );
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== GETTERS AND SETTERS ====================

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

    public void setCpuWarningPercent(BigDecimal cpuWarningPercent) {
        this.cpuWarningPercent = cpuWarningPercent != null ? cpuWarningPercent.intValue() : 90;
    }

    public Integer getRamWarningPercent() {
        return ramWarningPercent;
    }

    public void setRamWarningPercent(Integer ramWarningPercent) {
        this.ramWarningPercent = ramWarningPercent;
    }

    public void setRamWarningPercent(BigDecimal ramWarningPercent) {
        this.ramWarningPercent = ramWarningPercent != null ? ramWarningPercent.intValue() : 90;
    }

    public Integer getDiskCriticalPercent() {
        return diskCriticalPercent;
    }

    public void setDiskCriticalPercent(Integer diskCriticalPercent) {
        this.diskCriticalPercent = diskCriticalPercent;
    }

    public void setDiskCriticalPercent(BigDecimal diskCriticalPercent) {
        this.diskCriticalPercent = diskCriticalPercent != null ? diskCriticalPercent.intValue() : 90;
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
