package org.example.assetmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "license_assignments",
        indexes = {
                @Index(
                        name = "idx_license_assignments_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_license_assignments_license",
                        columnList = "license_id"
                )
        }
)
public class LicenseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "license_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_license_assignments_license")
    )
    private License license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_license_assignments_user")
    )
    private User user;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;


    // ==================== GETTERS AND SETTERS ====================

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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


    // ==================== LIFECYCLE ====================

    @PrePersist
    protected void onCreate() {

        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }

        if (status == null) {
            status = "ACTIVE";
        }
    }
}