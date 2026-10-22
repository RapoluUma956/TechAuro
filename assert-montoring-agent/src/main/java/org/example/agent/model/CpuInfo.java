package org.example.agent.model;

public class CpuInfo {

    private String name;
    private String vendor;
    private Integer cores;
    private Integer threads;
    private Long frequencyMhz;
    private Double usagePercent;

    public CpuInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getCores() {
        return cores;
    }

    public void setCores(Integer cores) {
        this.cores = cores;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public Long getFrequencyMhz() {
        return frequencyMhz;
    }

    public void setFrequencyMhz(Long frequencyMhz) {
        this.frequencyMhz = frequencyMhz;
    }

    public Double getUsagePercent() {
        return usagePercent;
    }

    public void setUsagePercent(Double usagePercent) {
        this.usagePercent = usagePercent;
    }

    @Override
    public String toString() {

        return "CpuInfo{" +
                "name='" + name + '\'' +
                ", vendor='" + vendor + '\'' +
                ", cores=" + cores +
                ", threads=" + threads +
                ", frequencyMhz=" + frequencyMhz +
                ", usagePercent=" + usagePercent +
                '}';
    }
}