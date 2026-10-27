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

    @Column(name = "cpu_warning_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal cpuWarningPercent = new BigDecimal("90.00");

    @Column(name = "ram_warning_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal ramWarningPercent = new BigDecimal("90.00");

    @Column(name = "disk_critical_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal diskCriticalPercent = new BigDecimal("90.00");

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

    public BigDecimal getCpuWarningPercent() {
        return cpuWarningPercent;
    }

    public void setCpuWarningPercent(BigDecimal cpuWarningPercent) {
        this.cpuWarningPercent = cpuWarningPercent;
    }

    public BigDecimal getRamWarningPercent() {
        return ramWarningPercent;
    }

    public void setRamWarningPercent(BigDecimal ramWarningPercent) {
        this.ramWarningPercent = ramWarningPercent;
    }

    public BigDecimal getDiskCriticalPercent() {
        return diskCriticalPercent;
    }

    public void setDiskCriticalPercent(BigDecimal diskCriticalPercent) {
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
