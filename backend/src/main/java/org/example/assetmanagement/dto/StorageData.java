package org.example.assetmanagement.dto;

import lombok.Data;

@Data
public class StorageData {

    private Long total;
    private Long used;
    private Long available;
    private Double usagePercent;
    private String storageType;
    private String model;
    private String health;
}