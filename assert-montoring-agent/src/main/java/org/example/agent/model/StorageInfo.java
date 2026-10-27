package org.example.agent.model;

public class StorageInfo {

    private Long total;
    private Long used;
    private Long available;
    private Double usagePercent;

    private String storageType;
    private String model;
    private String health;

    // Direct capacities in GB & SSD details
    private Double totalGb;
    private Double usedGb;
    private Double availableGb;
    private String ssdType;
    private String ssdModel;
    private String ssdHealth;

    public StorageInfo() {
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

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
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

    public String getSsdType() {
        return ssdType;
    }

    public void setSsdType(String ssdType) {
        this.ssdType = ssdType;
    }

    public String getSsdModel() {
        return ssdModel;
    }

    public void setSsdModel(String ssdModel) {
        this.ssdModel = ssdModel;
    }

    public String getSsdHealth() {
        return ssdHealth;
    }

    public void setSsdHealth(String ssdHealth) {
        this.ssdHealth = ssdHealth;
    }

    @Override
    public String toString() {
        return "StorageInfo{" +
                "total=" + total +
                ", used=" + used +
                ", available=" + available +
                ", usagePercent=" + usagePercent +
                ", totalGb=" + totalGb +
                ", usedGb=" + usedGb +
                ", availableGb=" + availableGb +
                ", storageType='" + storageType + '\'' +
                ", model='" + model + '\'' +
                ", health='" + health + '\'' +
                ", ssdType='" + ssdType + '\'' +
                ", ssdModel='" + ssdModel + '\'' +
                ", ssdHealth='" + ssdHealth + '\'' +
                '}';
    }
}