package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_permissions_permission_name",
                        columnNames = "permission_name"
                )
        }
)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @Column(name = "permission_name", nullable = false, unique = true, length = 100)
    private String permissionName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // Getters and Setters

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}