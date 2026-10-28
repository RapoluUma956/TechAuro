package com.assetmanagement.backend.dto.accessory;

public class CreateAccessoryResponse {

    private String message;
    private Long accessoryId;
    private String accessoryName;
    private Integer quantityAdded;
    private Integer totalQuantity;
    private Integer availableQuantity;

    public CreateAccessoryResponse() {}

    public CreateAccessoryResponse(String message, Long accessoryId, String accessoryName, Integer quantityAdded, Integer totalQuantity, Integer availableQuantity) {
        this.message = message;
        this.accessoryId = accessoryId;
        this.accessoryName = accessoryName;
        this.quantityAdded = quantityAdded;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAccessoryId() {
        return accessoryId;
    }

    public void setAccessoryId(Long accessoryId) {
        this.accessoryId = accessoryId;
    }

    public String getAccessoryName() {
        return accessoryName;
    }

    public void setAccessoryName(String accessoryName) {
        this.accessoryName = accessoryName;
    }

    public Integer getQuantityAdded() {
        return quantityAdded;
    }

    public void setQuantityAdded(Integer quantityAdded) {
        this.quantityAdded = quantityAdded;
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
}
