package org.example.agent.model;

public class MemoryInfo {

    private Long total;
    private Long used;
    private Long available;
    private Double usagePercent;

    private Long swapTotal;
    private Long swapUsed;

    // Direct capacities in GB and hardware specs
    private Double totalGb;
    private Double usedGb;
    private Double availableGb;
    private String ramType;
    private Long ramSpeedMhz;
    private Double swapTotalGb;
    private Double swapUsedGb;

    public MemoryInfo() {
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getUsed() {
        return used;
    }

    public void setUsed(Long used) {
        this.used = used;
    }

    public Long getAvailable() {
        return available;
    }

    public void setAvailable(Long available) {
        this.available = available;
    }

    public Double getUsagePercent() {
        return usagePercent;
    }

    public void setUsagePercent(Double usagePercent) {
        this.usagePercent = usagePercent;
    }

    public Long getSwapTotal() {
        return swapTotal;
    }

    public void setSwapTotal(Long swapTotal) {
        this.swapTotal = swapTotal;
    }

    public Long getSwapUsed() {
        return swapUsed;
    }

    public void setSwapUsed(Long swapUsed) {
        this.swapUsed = swapUsed;
    }

    public Double getTotalGb() {
        return totalGb;
    }

    public void setTotalGb(Double totalGb) {
        this.totalGb = totalGb;
    }

    public Double getUsedGb() {
        return usedGb;
    }

    public void setUsedGb(Double usedGb) {
        this.usedGb = usedGb;
    }

    public Double getAvailableGb() {
        return availableGb;
    }

    public void setAvailableGb(Double availableGb) {
        this.availableGb = availableGb;
    }

    public String getRamType() {
        return ramType;
    }

    public void setRamType(String ramType) {
        this.ramType = ramType;
    }

    public Long getRamSpeedMhz() {
        return ramSpeedMhz;
    }

    public void setRamSpeedMhz(Long ramSpeedMhz) {
        this.ramSpeedMhz = ramSpeedMhz;
    }

    public Double getSwapTotalGb() {
        return swapTotalGb;
    }

    public void setSwapTotalGb(Double swapTotalGb) {
        this.swapTotalGb = swapTotalGb;
    }

    public Double getSwapUsedGb() {
        return swapUsedGb;
    }

    public void setSwapUsedGb(Double swapUsedGb) {
        this.swapUsedGb = swapUsedGb;
    }

    @Override
    public String toString() {
        return "MemoryInfo{" +
                "total=" + total +
                ", used=" + used +
                ", available=" + available +
                ", usagePercent=" + usagePercent +
                ", totalGb=" + totalGb +
                ", usedGb=" + usedGb +
                ", availableGb=" + availableGb +
                ", ramType='" + ramType + '\'' +
                ", ramSpeedMhz=" + ramSpeedMhz +
                ", swapTotalGb=" + swapTotalGb +
                ", swapUsedGb=" + swapUsedGb +
                '}';
    }
}