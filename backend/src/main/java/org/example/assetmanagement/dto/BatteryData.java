package org.example.assetmanagement.dto;

import lombok.Data;

@Data
public class BatteryData {

    private Double percentage;
    private Boolean charging;
    private Double healthPercent;
    private Integer cycleCount;
    private String powerSource;
}