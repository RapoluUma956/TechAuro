package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_assignments")
public class DeviceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "device_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_device_assignments_device")
    )
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_device_assignments_user")
    )
    private User user;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public DeviceAssignment() {
    }

    public DeviceAssignment(Device device, User user, String serialNumber, String status) {
        this.device = device;
        this.user = user;
        this.serialNumber = serialNumber != null ? serialNumber : (device != null ? device.getSerialNumber() : null);
        this.status = status != null ? status : "ACTIVE";
        this.assignedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
        if ((this.serialNumber == null || this.serialNumber.isBlank()) && device != null && device.getSerialNumber() != null) {
            this.serialNumber = device.getSerialNumber();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }

        if (status == null) {
            status = "ACTIVE";
        }

        if ((serialNumber == null || serialNumber.isBlank()) && device != null && device.getSerialNumber() != null) {
            serialNumber = device.getSerialNumber();
        }
    }
}
