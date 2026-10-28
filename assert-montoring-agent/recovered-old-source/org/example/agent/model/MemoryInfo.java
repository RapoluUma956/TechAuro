/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

public class MemoryInfo {
    private Long total;
    private Long used;
    private Long available;
    private Double usagePercent;
    private Long swapTotal;
    private Long swapUsed;

    public Long getTotal() {
        return this.total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getUsed() {
        return this.used;
    }

    public void setUsed(Long used) {
        this.used = used;
    }

    public Long getAvailable() {
        return this.available;
    }

    public void setAvailable(Long available) {
        this.available = available;
    }

    public Double getUsagePercent() {
        return this.usagePercent;
    }

    public void setUsagePercent(Double usagePercent) {
        this.usagePercent = usagePercent;
    }

    public Long getSwapTotal() {
        return this.swapTotal;
    }

    public void setSwapTotal(Long swapTotal) {
        this.swapTotal = swapTotal;
    }

    public Long getSwapUsed() {
        return this.swapUsed;
    }

    public void setSwapUsed(Long swapUsed) {
        this.swapUsed = swapUsed;
    }

    public String toString() {
        return "MemoryInfo{total=" + this.total + ", used=" + this.used + ", available=" + this.available + ", usagePercent=" + this.usagePercent + ", swapTotal=" + this.swapTotal + ", swapUsed=" + this.swapUsed + "}";
    }
}

