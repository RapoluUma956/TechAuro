/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.SystemInfo
 *  oshi.hardware.ComputerSystem
 */
package org.example.agent.collector;

import java.time.LocalDateTime;
import java.util.List;
import org.example.agent.collector.BatteryCollector;
import org.example.agent.collector.BiosCollector;
import org.example.agent.collector.CpuCollector;
import org.example.agent.collector.MemoryCollector;
import org.example.agent.collector.NetworkCollector;
import org.example.agent.collector.OsCollector;
import org.example.agent.collector.PerformanceCollector;
import org.example.agent.collector.SecurityCollector;
import org.example.agent.collector.SoftwareCollector;
import org.example.agent.collector.StorageCollector;
import org.example.agent.collector.VulnerabilityCollector;
import org.example.agent.model.AssetReport;
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
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;

public class DeviceCollector {
    private final SystemInfo systemInfo = new SystemInfo();
    private final CpuCollector cpuCollector = new CpuCollector(this.systemInfo);
    private final MemoryCollector memoryCollector = new MemoryCollector(this.systemInfo);
    private final StorageCollector storageCollector = new StorageCollector(this.systemInfo);
    private final BatteryCollector batteryCollector = new BatteryCollector(this.systemInfo);
    private final OsCollector osCollector = new OsCollector(this.systemInfo);
    private final BiosCollector biosCollector = new BiosCollector();
    private final SecurityCollector securityCollector = new SecurityCollector();
    private final PerformanceCollector performanceCollector = new PerformanceCollector();
    private final VulnerabilityCollector vulnerabilityCollector = new VulnerabilityCollector();
    private final NetworkCollector networkCollector = new NetworkCollector();
    private final SoftwareCollector softwareCollector = new SoftwareCollector();

    public AssetReport collect() {
        System.out.println();
        System.out.println("======================================");
        System.out.println("       DEVICE COLLECTION STARTED");
        System.out.println("======================================");
        AssetReport report = new AssetReport();
        this.collectDeviceIdentity(report);
        System.out.println();
        System.out.println("Collecting CPU information...");
        try {
            CpuInfo cpu = this.cpuCollector.collect();
            report.setCpu(cpu);
        }
        catch (Exception e) {
            System.out.println("Error collecting CPU information");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Collecting memory information...");
        try {
            MemoryInfo memory = this.memoryCollector.collect();
            report.setMemory(memory);
        }
        catch (Exception e) {
            System.out.println("Error collecting memory information");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Collecting storage information...");
        try {
            StorageInfo storage = this.storageCollector.collect();
            report.setStorage(storage);
        }
        catch (Exception e) {
            System.out.println("Error collecting storage information");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Collecting battery information...");
        try {
            BatteryInfo battery = this.batteryCollector.collect();
            report.setBattery(battery);
        }
        catch (Exception e) {
            System.out.println("Error collecting battery information");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Collecting OS information...");
        try {
            OsInfo os = this.osCollector.collect();
            report.setOs(os);
        }
        catch (Exception e) {
            System.out.println("Error collecting OS information");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Collecting BIOS information...");
        try {
            BiosInfo bios = this.biosCollector.collect();
            report.setBios(bios);
        }
        catch (Exception e) {
            System.out.println("Error collecting BIOS information");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Collecting security information...");
        try {
            SecurityInfo security = this.securityCollector.collect();
            report.setSecurity(security);
        }
        catch (Exception e) {
            System.out.println("Error collecting security information");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Collecting performance information...");
        try {
            PerformanceInfo performance = this.performanceCollector.collect();
            report.setPerformance(performance);
        }
        catch (Exception e) {
            System.out.println("Error collecting performance information");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("Collecting vulnerability information...");
        try {
            List<VulnerabilityInfo> vulnerabilities = this.vulnerabilityCollector.collect();
            report.setVulnerabilities(vulnerabilities);
        }
        catch (Exception e) {
            System.out.println("Error collecting vulnerability information");
            e.printStackTrace();
            report.setVulnerabilities(List.of());
        }
        System.out.println();
        System.out.println("Collecting network information...");
        try {
            List<NetworkInfo> networks = this.networkCollector.collect();
            report.setNetworks(networks);
            System.out.println("Networks returned by NetworkCollector: " + networks.size());
            System.out.println("Networks inside AssetReport: " + report.getNetworks().size());
        }
        catch (Exception e) {
            System.out.println("Error collecting network information");
            e.printStackTrace();
            report.setNetworks(List.of());
        }
        System.out.println();
        System.out.println("Collecting installed software...");
        try {
            List<SoftwareInfo> software = this.softwareCollector.collect();
            report.setInstalledSoftware(software);
            System.out.println("Software returned by SoftwareCollector: " + software.size());
            System.out.println("Software inside AssetReport: " + report.getInstalledSoftware().size());
        }
        catch (Exception e) {
            System.out.println("Error collecting installed software");
            e.printStackTrace();
            report.setInstalledSoftware(List.of());
        }
        report.setAgentVersion("1.0.0");
        report.setCollectedAt(LocalDateTime.now());
        System.out.println();
        System.out.println("======================================");
        System.out.println("       DEVICE COLLECTION COMPLETED");
        System.out.println("======================================");
        return report;
    }

    private void collectDeviceIdentity(AssetReport report) {
        System.out.println();
        System.out.println("======================================");
        System.out.println("       DEVICE IDENTITY COLLECTION");
        System.out.println("======================================");
        try {
            ComputerSystem computerSystem = this.systemInfo.getHardware().getComputerSystem();
            String manufacturer = computerSystem.getManufacturer();
            String model = computerSystem.getModel();
            String serialNumber = computerSystem.getSerialNumber();
            report.setBrand(this.cleanValue(manufacturer));
            report.setModel(this.cleanValue(model));
            report.setSerialNumber(this.cleanValue(serialNumber));
            String assetTag = this.createAssetTag(report.getSerialNumber());
            report.setAssetTag(assetTag);
            System.out.println();
            System.out.println("Brand         : " + report.getBrand());
            System.out.println("Model         : " + report.getModel());
            System.out.println("Serial Number : " + report.getSerialNumber());
            System.out.println("Asset Tag     : " + report.getAssetTag());
        }
        catch (Exception e) {
            System.out.println();
            System.out.println("ERROR WHILE COLLECTING DEVICE IDENTITY");
            e.printStackTrace();
        }
        System.out.println("======================================");
    }

    private String cleanValue(String value) {
        if (value == null) {
            return null;
        }
        if ((value = value.trim()).isBlank()) {
            return null;
        }
        return value;
    }

    private String createAssetTag(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) {
            return null;
        }
        return "AUTO-" + serialNumber.trim();
    }
}

