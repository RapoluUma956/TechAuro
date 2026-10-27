package org.example.agent.model;

import java.time.LocalDateTime;
import java.util.List;

public class AssetReport {

    private String assetTag;
    private String serialNumber;
    private String brand;
    private String model;
    private String deviceName;

    private CpuInfo cpu;
    private MemoryInfo memory;
    private StorageInfo storage;
    private BatteryInfo battery;
    private OsInfo os;
    private BiosInfo bios;
    private SecurityInfo security;
    private PerformanceInfo performance;

    private List<NetworkInfo> networks;
    private List<SoftwareInfo> installedSoftware;
    private List<VulnerabilityInfo> vulnerabilities;

    private String agentVersion;
    private LocalDateTime collectedAt;


    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }


    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }


    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


    public CpuInfo getCpu() {
        return cpu;
    }

    public void setCpu(CpuInfo cpu) {
        this.cpu = cpu;
    }


    public MemoryInfo getMemory() {
        return memory;
    }

    public void setMemory(MemoryInfo memory) {
        this.memory = memory;
    }


    public StorageInfo getStorage() {
        return storage;
    }

    public void setStorage(StorageInfo storage) {
        this.storage = storage;
    }


    public BatteryInfo getBattery() {
        return battery;
    }

    public void setBattery(BatteryInfo battery) {
        this.battery = battery;
    }


    public OsInfo getOs() {
        return os;
    }

    public void setOs(OsInfo os) {
        this.os = os;
    }


    public BiosInfo getBios() {
        return bios;
    }

    public void setBios(BiosInfo bios) {
        this.bios = bios;
    }


    public SecurityInfo getSecurity() {
        return security;
    }

    public void setSecurity(SecurityInfo security) {
        this.security = security;
    }


    public PerformanceInfo getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceInfo performance) {
        this.performance = performance;
    }


    public List<NetworkInfo> getNetworks() {
        return networks;
    }

    public void setNetworks(List<NetworkInfo> networks) {
        this.networks = networks;
    }


    public List<SoftwareInfo> getInstalledSoftware() {
        return installedSoftware;
    }

    public void setInstalledSoftware(
            List<SoftwareInfo> installedSoftware) {

        this.installedSoftware = installedSoftware;
    }


    public List<VulnerabilityInfo> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(
            List<VulnerabilityInfo> vulnerabilities) {

        this.vulnerabilities = vulnerabilities;
    }


    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }


    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }


    @Override
    public String toString() {

        return "AssetReport{" +
                "assetTag='" + assetTag + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", cpu=" + cpu +
                ", memory=" + memory +
                ", storage=" + storage +
                ", battery=" + battery +
                ", os=" + os +
                ", bios=" + bios +
                ", security=" + security +
                ", performance=" + performance +
                ", networks=" + networks +
                ", installedSoftware=" + installedSoftware +
                ", vulnerabilities=" + vulnerabilities +
                ", agentVersion='" + agentVersion + '\'' +
                ", collectedAt=" + collectedAt +
                '}';
    }
}