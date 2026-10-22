package org.example.assetmanagement.dto;

import lombok.Data;

@Data
public class MemoryData {

    private Long total;
    private Long used;
    private Long available;
    private Double usagePercent;
    private Long swapTotal;
    private Long swapUsed;
}