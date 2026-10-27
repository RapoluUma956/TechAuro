package com.assetmanagement.backend.dto;

import java.util.List;

public class AgentReportRequest {

    private String assetTag;
    private String serialNumber;
    private String deviceName;
    private String brand;
    private String model;

    private CpuData cpu;
    private MemoryData memory;
    private StorageData storage;
    private BatteryData battery;
    private OsData os;
    private BiosData bios;
    private SecurityData security;
    private PerformanceData performance;

    private List<NetworkData> networks;

    private List<SoftwareData> installedSoftware;

    private List<VulnerabilityData> vulnerabilities;

    private String agentVersion;

    private String collectedAt;


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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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


    public CpuData getCpu() {
        return cpu;
    }

    public void setCpu(CpuData cpu) {
        this.cpu = cpu;
    }


    public MemoryData getMemory() {
        return memory;
    }

    public void setMemory(MemoryData memory) {
        this.memory = memory;
    }


    public StorageData getStorage() {
        return storage;
    }

    public void setStorage(StorageData storage) {
        this.storage = storage;
    }


    public BatteryData getBattery() {
        return battery;
    }

    public void setBattery(BatteryData battery) {
        this.battery = battery;
    }


    public OsData getOs() {
        return os;
    }

    public void setOs(OsData os) {
        this.os = os;
    }


    public BiosData getBios() {
        return bios;
    }

    public void setBios(BiosData bios) {
        this.bios = bios;
    }


    public SecurityData getSecurity() {
        return security;
    }

    public void setSecurity(SecurityData security) {
        this.security = security;
    }


    public PerformanceData getPerformance() {
        return performance;
    }

    public void setPerformance(
            PerformanceData performance) {

        this.performance = performance;
    }


    public List<NetworkData> getNetworks() {
        return networks;
    }

    public void setNetworks(
            List<NetworkData> networks) {

        this.networks = networks;
    }


    public List<SoftwareData> getInstalledSoftware() {
        return installedSoftware;
    }

    public void setInstalledSoftware(
            List<SoftwareData> installedSoftware) {

        this.installedSoftware =
                installedSoftware;
    }


    public List<VulnerabilityData> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(
            List<VulnerabilityData> vulnerabilities) {

        this.vulnerabilities =
                vulnerabilities;
    }


    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(
            String agentVersion) {

        this.agentVersion = agentVersion;
    }


    public String getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(
            String collectedAt) {

        this.collectedAt = collectedAt;
    }
}