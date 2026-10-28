package com.assetmanagement.backend.dto.accessory;

import java.util.ArrayList;
import java.util.List;

public class LaptopConfigurationDto {

    private String brand;
    private String model;
    private String ram;
    private String ssd;
    private long total;
    private long assigned;
    private long remaining;
    private long repair;
    private List<LaptopDeviceItemDto> devices = new ArrayList<>();

    public LaptopConfigurationDto() {}

    public LaptopConfigurationDto(String brand, String model, String ram, String ssd, long total, long assigned, long remaining, long repair) {
        this.brand = brand;
        this.model = model;
        this.ram = ram;
        this.ssd = ssd;
        this.total = total;
        this.assigned = assigned;
        this.remaining = remaining;
        this.repair = repair;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getSsd() {
        return ssd;
    }

    public void setSsd(String ssd) {
        this.ssd = ssd;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAssigned() {
        return assigned;
    }

    public void setAssigned(long assigned) {
        this.assigned = assigned;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public long getRepair() {
        return repair;
    }

    public void setRepair(long repair) {
        this.repair = repair;
    }

    public List<LaptopDeviceItemDto> getDevices() {
        return devices;
    }

    public void setDevices(List<LaptopDeviceItemDto> devices) {
        this.devices = devices;
    }
}
