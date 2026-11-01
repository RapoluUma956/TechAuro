package com.assetmanagement.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "employee_access_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_eaa_manager_employee", columnNames = {"manager_user_id", "employee_user_id"})
        }
)
public class EmployeeAccessAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "manager_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_eaa_manager")
    )
    private User manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_eaa_employee")
    )
    private User employee;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public EmployeeAccessAssignment() {}

    public EmployeeAccessAssignment(User manager, User employee) {
        this.manager = manager;
        this.employee = employee;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
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
}

