package com.assetmanagement.backend.dto.accessory;

public class AccessorySummaryResponse {

    private long totalAccessories;
    private long assignedAccessories;
    private long unassignedAccessories;
    private long repairAccessories;

    public AccessorySummaryResponse() {}

    public AccessorySummaryResponse(long totalAccessories, long assignedAccessories, long unassignedAccessories, long repairAccessories) {
        this.totalAccessories = totalAccessories;
        this.assignedAccessories = assignedAccessories;
        this.unassignedAccessories = unassignedAccessories;
        this.repairAccessories = repairAccessories;
    }

    public long getTotalAccessories() {
        return totalAccessories;
    }

    public void setTotalAccessories(long totalAccessories) {
        this.totalAccessories = totalAccessories;
    }

    public long getAssignedAccessories() {
        return assignedAccessories;
    }

    public void setAssignedAccessories(long assignedAccessories) {
        this.assignedAccessories = assignedAccessories;
    }

    public long getUnassignedAccessories() {
        return unassignedAccessories;
    }

    public void setUnassignedAccessories(long unassignedAccessories) {
        this.unassignedAccessories = unassignedAccessories;
    }

    public long getRepairAccessories() {
        return repairAccessories;
    }

    public void setRepairAccessories(long repairAccessories) {
        this.repairAccessories = repairAccessories;
    }
}
