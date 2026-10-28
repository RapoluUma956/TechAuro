/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

public class StorageInfo {
    private Long total;
    private Long used;
    private Long available;
    private Double usagePercent;
    private String storageType;
    private String model;
    private String health;

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

    public String getStorageType() {
        return this.storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getHealth() {
        return this.health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String toString() {
        return "StorageInfo{total=" + this.total + ", used=" + this.used + ", available=" + this.available + ", usagePercent=" + this.usagePercent + ", storageType='" + this.storageType + "', model='" + this.model + "', health='" + this.health + "'}";
    }
}

