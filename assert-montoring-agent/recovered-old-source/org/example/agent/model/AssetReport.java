/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

import java.time.LocalDateTime;
import java.util.List;
import org.example.agent.model.BatteryInfo;
import org.example.agent.model.BiosInfo;
import org.example.agent.model.CpuInfo;
import org.example.agent.model.MemoryInfo;
import org.example.agent.model.NetworkInfo;
import org.example.agent.model.OsInfo;
import org.example.agent.model.PerformanceInfo;
import org.example.agent.model.SecurityInfo;
import org.example.agent.model.SoftwareInfo;
import org.example.agent.model.StorageInfo;
import org.example.agent.model.VulnerabilityInfo;

public class AssetReport {
    private String assetTag;
    private String serialNumber;
    private String brand;
    private String model;
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
        return this.assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBrand() {
        return this.brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public CpuInfo getCpu() {
        return this.cpu;
    }

    public void setCpu(CpuInfo cpu) {
        this.cpu = cpu;
    }

    public MemoryInfo getMemory() {
        return this.memory;
    }

    public void setMemory(MemoryInfo memory) {
        this.memory = memory;
    }

    public StorageInfo getStorage() {
        return this.storage;
    }

    public void setStorage(StorageInfo storage) {
        this.storage = storage;
    }

    public BatteryInfo getBattery() {
        return this.battery;
    }

    public void setBattery(BatteryInfo battery) {
        this.battery = battery;
    }

    public OsInfo getOs() {
        return this.os;
    }

    public void setOs(OsInfo os) {
        this.os = os;
    }

    public BiosInfo getBios() {
        return this.bios;
    }

    public void setBios(BiosInfo bios) {
        this.bios = bios;
    }

    public SecurityInfo getSecurity() {
        return this.security;
    }

    public void setSecurity(SecurityInfo security) {
        this.security = security;
    }

    public PerformanceInfo getPerformance() {
        return this.performance;
    }

    public void setPerformance(PerformanceInfo performance) {
        this.performance = performance;
    }

    public List<NetworkInfo> getNetworks() {
        return this.networks;
    }

    public void setNetworks(List<NetworkInfo> networks) {
        this.networks = networks;
    }

    public List<SoftwareInfo> getInstalledSoftware() {
        return this.installedSoftware;
    }

    public void setInstalledSoftware(List<SoftwareInfo> installedSoftware) {
        this.installedSoftware = installedSoftware;
    }

    public List<VulnerabilityInfo> getVulnerabilities() {
        return this.vulnerabilities;
    }

    public void setVulnerabilities(List<VulnerabilityInfo> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public String getAgentVersion() {
        return this.agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public LocalDateTime getCollectedAt() {
        return this.collectedAt;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }

    public String toString() {
        return "AssetReport{assetTag='" + this.assetTag + "', serialNumber='" + this.serialNumber + "', brand='" + this.brand + "', model='" + this.model + "', cpu=" + String.valueOf(this.cpu) + ", memory=" + String.valueOf(this.memory) + ", storage=" + String.valueOf(this.storage) + ", battery=" + String.valueOf(this.battery) + ", os=" + String.valueOf(this.os) + ", bios=" + String.valueOf(this.bios) + ", security=" + String.valueOf(this.security) + ", performance=" + String.valueOf(this.performance) + ", networks=" + String.valueOf(this.networks) + ", installedSoftware=" + String.valueOf(this.installedSoftware) + ", vulnerabilities=" + String.valueOf(this.vulnerabilities) + ", agentVersion='" + this.agentVersion + "', collectedAt=" + String.valueOf(this.collectedAt) + "}";
    }
}

