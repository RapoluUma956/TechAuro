package com.assetmanagement.backend.dto.accessory;

import java.util.List;

public class BulkAddLaptopResponse {

    private String message;
    private int quantityAdded;
    private List<LaptopDeviceItemDto> devices;

    public BulkAddLaptopResponse() {}

    public BulkAddLaptopResponse(String message, int quantityAdded, List<LaptopDeviceItemDto> devices) {
        this.message = message;
        this.quantityAdded = quantityAdded;
        this.devices = devices;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getQuantityAdded() {
        return quantityAdded;
    }

    public void setQuantityAdded(int quantityAdded) {
        this.quantityAdded = quantityAdded;
    }

    public List<LaptopDeviceItemDto> getDevices() {
        return devices;
    }

    public void setDevices(List<LaptopDeviceItemDto> devices) {
        this.devices = devices;
    }
}
