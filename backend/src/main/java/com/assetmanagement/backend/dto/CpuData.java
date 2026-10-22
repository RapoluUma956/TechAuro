package com.assetmanagement.backend.dto;

import lombok.Data;

@Data
public class CpuData {

    private String name;
    private String vendor;
    private Integer cores;
    private Integer threads;
    private Long frequencyMhz;
    private Double usagePercent;
    private Double temperature;
}