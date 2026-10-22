package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accessories")
public class Accessory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accessory_id")
    private Long accessoryId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /*
     * Category-specific specifications.
     *
     * Example:
     * {
     *   "connectivity": "Wireless",
     *   "dpi": 1600,
     *   "buttons": 6
     * }
     */
    @Column(name = "specifications", columnDefinition = "jsonb")
    private String specifications;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(name = "damaged_quantity", nullable = false)
    private Integer damagedQuantity = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // ==================== GETTERS AND SETTERS ====================

    public Long getAccessoryId() {
        return accessoryId;
    }

    public void setAccessoryId(Long accessoryId) {
        this.accessoryId = accessoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getDamagedQuantity() {
        return damagedQuantity;
    }

    public void setDamagedQuantity(Integer damagedQuantity) {
        this.damagedQuantity = damagedQuantity;
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


    // ==================== LIFECYCLE METHODS ====================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (totalQuantity == null) {
            totalQuantity = 0;
        }

        if (availableQuantity == null) {
            availableQuantity = 0;
        }

        if (damagedQuantity == null) {
            damagedQuantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}