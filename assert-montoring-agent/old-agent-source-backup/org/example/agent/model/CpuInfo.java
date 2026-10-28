/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

public class CpuInfo {
    private String name;
    private String vendor;
    private Integer cores;
    private Integer threads;
    private Long frequencyMhz;
    private Double usagePercent;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return this.vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getCores() {
        return this.cores;
    }

    public void setCores(Integer cores) {
        this.cores = cores;
    }

    public Integer getThreads() {
        return this.threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public Long getFrequencyMhz() {
        return this.frequencyMhz;
    }

    public void setFrequencyMhz(Long frequencyMhz) {
        this.frequencyMhz = frequencyMhz;
    }

    public Double getUsagePercent() {
        return this.usagePercent;
    }

    public void setUsagePercent(Double usagePercent) {
        this.usagePercent = usagePercent;
    }

    public String toString() {
        return "CpuInfo{name='" + this.name + "', vendor='" + this.vendor + "', cores=" + this.cores + ", threads=" + this.threads + ", frequencyMhz=" + this.frequencyMhz + ", usagePercent=" + this.usagePercent + "}";
    }
}

