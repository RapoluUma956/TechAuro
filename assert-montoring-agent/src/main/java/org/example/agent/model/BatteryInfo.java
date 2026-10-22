package org.example.agent.model;

public class BatteryInfo {

    private Double percentage;
    private Boolean charging;
    private Double healthPercent;
    private Integer cycleCount;
    private String powerSource;

    public BatteryInfo() {
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Boolean getCharging() {
        return charging;
    }

    public void setCharging(Boolean charging) {
        this.charging = charging;
    }

    public Double getHealthPercent() {
        return healthPercent;
    }

    public void setHealthPercent(Double healthPercent) {
        this.healthPercent = healthPercent;
    }

    public Integer getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(Integer cycleCount) {
        this.cycleCount = cycleCount;
    }

    public String getPowerSource() {
        return powerSource;
    }

    public void setPowerSource(String powerSource) {
        this.powerSource = powerSource;
    }

    @Override
    public String toString() {
        return "BatteryInfo{" +
                "percentage=" + percentage +
                ", charging=" + charging +
                ", healthPercent=" + healthPercent +
                ", cycleCount=" + cycleCount +
                ", powerSource='" + powerSource + '\'' +
                '}';
    }
}