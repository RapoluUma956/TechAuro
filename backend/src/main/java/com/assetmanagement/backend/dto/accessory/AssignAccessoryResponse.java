package com.assetmanagement.backend.dto.accessory;

public class AssignAccessoryResponse {

    private String message;
    private Long assignmentId;
    private Long assetId;
    private String assetName;
    private Long userId;
    private String userName;
    private int quantity;
    private int remainingAvailable;

    public AssignAccessoryResponse() {}

    public AssignAccessoryResponse(
            String message,
            Long assignmentId,
            Long assetId,
            String assetName,
            Long userId,
            String userName,
            int quantity,
            int remainingAvailable) {
        this.message = message;
        this.assignmentId = assignmentId;
        this.assetId = assetId;
        this.assetName = assetName;
        this.userId = userId;
        this.userName = userName;
        this.quantity = quantity;
        this.remainingAvailable = remainingAvailable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRemainingAvailable() {
        return remainingAvailable;
    }

    public void setRemainingAvailable(int remainingAvailable) {
        this.remainingAvailable = remainingAvailable;
    }
}
