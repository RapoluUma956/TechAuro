package com.assetmanagement.backend.dto.accessory;

import java.time.LocalDate;

public class AssignAccessoryRequest {

    private Long userId;
    private LocalDate assignedDate;
    private Integer quantity = 1;
    private String notes;

    public AssignAccessoryRequest() {}

    public AssignAccessoryRequest(Long userId, LocalDate assignedDate, Integer quantity, String notes) {
        this.userId = userId;
        this.assignedDate = assignedDate;
        this.quantity = quantity;
        this.notes = notes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
