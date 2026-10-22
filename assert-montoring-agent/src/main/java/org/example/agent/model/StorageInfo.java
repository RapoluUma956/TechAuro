package org.example.agent.model;

public class StorageInfo {

    private Long total;
    private Long used;
    private Long available;
    private Double usagePercent;

    private String storageType;
    private String model;
    private String health;

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

    @Override
    public String toString() {
        return "StorageInfo{" +
                "total=" + total +
                ", used=" + used +
                ", available=" + available +
                ", usagePercent=" + usagePercent +
                ", storageType='" + storageType + '\'' +
                ", model='" + model + '\'' +
                ", health='" + health + '\'' +
                '}';
    }
}