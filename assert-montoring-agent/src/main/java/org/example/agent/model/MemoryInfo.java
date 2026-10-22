package org.example.agent.model;

public class MemoryInfo {

    private Long total;
    private Long used;
    private Long available;
    private Double usagePercent;

    private Long swapTotal;
    private Long swapUsed;

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

    @Override
    public String toString() {
        return "MemoryInfo{" +
                "total=" + total +
                ", used=" + used +
                ", available=" + available +
                ", usagePercent=" + usagePercent +
                ", swapTotal=" + swapTotal +
                ", swapUsed=" + swapUsed +
                '}';
    }
}