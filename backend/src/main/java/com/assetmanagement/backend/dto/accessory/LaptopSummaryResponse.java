package com.assetmanagement.backend.dto.accessory;

public class LaptopSummaryResponse {

    private long totalLaptops;
    private long assignedLaptops;
    private long availableLaptops;
    private long repairLaptops;

    public LaptopSummaryResponse() {}

    public LaptopSummaryResponse(long totalLaptops, long assignedLaptops, long availableLaptops, long repairLaptops) {
        this.totalLaptops = totalLaptops;
        this.assignedLaptops = assignedLaptops;
        this.availableLaptops = availableLaptops;
        this.repairLaptops = repairLaptops;
    }

    public long getTotalLaptops() {
        return totalLaptops;
    }

    public void setTotalLaptops(long totalLaptops) {
        this.totalLaptops = totalLaptops;
    }

    public long getAssignedLaptops() {
        return assignedLaptops;
    }

    public void setAssignedLaptops(long assignedLaptops) {
        this.assignedLaptops = assignedLaptops;
    }

    public long getAvailableLaptops() {
        return availableLaptops;
    }

    public void setAvailableLaptops(long availableLaptops) {
        this.availableLaptops = availableLaptops;
    }

    public long getRepairLaptops() {
        return repairLaptops;
    }

    public void setRepairLaptops(long repairLaptops) {
        this.repairLaptops = repairLaptops;
    }
}
