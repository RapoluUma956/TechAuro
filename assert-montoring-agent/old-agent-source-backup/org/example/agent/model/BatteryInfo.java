/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

public class BatteryInfo {
    private Double percentage;
    private Boolean charging;
    private Double healthPercent;
    private Integer cycleCount;
    private String powerSource;

    public Double getPercentage() {
        return this.percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Boolean getCharging() {
        return this.charging;
    }

    public void setCharging(Boolean charging) {
        this.charging = charging;
    }

    public Double getHealthPercent() {
        return this.healthPercent;
    }

    public void setHealthPercent(Double healthPercent) {
        this.healthPercent = healthPercent;
    }

    public Integer getCycleCount() {
        return this.cycleCount;
    }

    public void setCycleCount(Integer cycleCount) {
        this.cycleCount = cycleCount;
    }

    public String getPowerSource() {
        return this.powerSource;
    }

    public void setPowerSource(String powerSource) {
        this.powerSource = powerSource;
    }

    public String toString() {
        return "BatteryInfo{percentage=" + this.percentage + ", charging=" + this.charging + ", healthPercent=" + this.healthPercent + ", cycleCount=" + this.cycleCount + ", powerSource='" + this.powerSource + "'}";
    }
}

