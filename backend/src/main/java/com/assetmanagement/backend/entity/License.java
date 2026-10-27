package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "licenses")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Long licenseId;

    @Column(name = "license_name", length = 150)
    private String licenseName;

    @Column(name = "software_name", length = 150)
    private String softwareName;

    @Column(name = "publisher", length = 150)
    private String publisher;

    @Column(name = "license_key", length = 255)
    private String licenseKey;

    @Column(name = "license_type", length = 50)
    private String licenseType;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Column(name = "total_licenses")
    private Integer totalLicenses = 0;

    @Column(name = "available_quantity")
    private Integer availableQuantity = 0;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "warranty_end_date")
    private LocalDate warrantyEndDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // ==================== GETTERS AND SETTERS ====================

    public Long getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Long licenseId) {
        this.licenseId = licenseId;
    }

    public String getLicenseName() {
        return (licenseName != null && !licenseName.isBlank()) ? licenseName : softwareName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public Integer getTotalQuantity() {
        return (totalQuantity != null && totalQuantity > 0) ? totalQuantity : totalLicenses;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
        if (this.totalLicenses == null || this.totalLicenses == 0) {
            this.totalLicenses = totalQuantity;
        }
    }

    public Integer getTotalLicenses() {
        return (totalLicenses != null && totalLicenses > 0) ? totalLicenses : totalQuantity;
    }

    public void setTotalLicenses(Integer totalLicenses) {
        this.totalLicenses = totalLicenses;
        if (this.totalQuantity == null || this.totalQuantity == 0) {
            this.totalQuantity = totalLicenses;
        }
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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


    // ==================== LIFECYCLE ====================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (totalQuantity == null && totalLicenses != null) {
            totalQuantity = totalLicenses;
        } else if (totalLicenses == null && totalQuantity != null) {
            totalLicenses = totalQuantity;
        }

        if (totalQuantity == null) {
            totalQuantity = 0;
        }

        if (totalLicenses == null) {
            totalLicenses = 0;
        }

        if (availableQuantity == null) {
            availableQuantity = 0;
        }

        if (softwareName == null && licenseName != null) {
            softwareName = licenseName;
        } else if (licenseName == null && softwareName != null) {
            licenseName = softwareName;
        }

        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}