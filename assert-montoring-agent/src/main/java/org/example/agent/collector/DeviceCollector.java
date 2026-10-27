package org.example.agent.collector;

import oshi.SystemInfo;
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

import java.time.LocalDateTime;
import java.util.List;

public class DeviceCollector {

    private final SystemInfo systemInfo;

    private final CpuCollector cpuCollector;
    private final MemoryCollector memoryCollector;
    private final StorageCollector storageCollector;
    private final BatteryCollector batteryCollector;
    private final OsCollector osCollector;
    private final BiosCollector biosCollector;
    private final SecurityCollector securityCollector;
    private final PerformanceCollector performanceCollector;
    private final VulnerabilityCollector vulnerabilityCollector;
    private final NetworkCollector networkCollector;
    private final SoftwareCollector softwareCollector;


    public DeviceCollector() {

        systemInfo = new SystemInfo();

        cpuCollector = new CpuCollector(systemInfo);
        memoryCollector = new MemoryCollector(systemInfo);
        storageCollector = new StorageCollector(systemInfo);
        batteryCollector = new BatteryCollector(systemInfo);
        osCollector = new OsCollector(systemInfo);

        // These collectors do not require SystemInfo
        biosCollector = new BiosCollector();
        securityCollector = new SecurityCollector();
        performanceCollector = new PerformanceCollector();
        vulnerabilityCollector = new VulnerabilityCollector();
        networkCollector = new NetworkCollector();
        softwareCollector = new SoftwareCollector();
    }


    public AssetReport collect() {

        System.out.println();
        System.out.println("======================================");
        System.out.println("       DEVICE COLLECTION STARTED");
        System.out.println("======================================");


        AssetReport report = new AssetReport();


        /*
         * =========================================================
         * DEVICE IDENTITY
         * =========================================================
         */

        collectDeviceIdentity(report);


        /*
         * =========================================================
         * CPU
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting CPU information..."
        );

        try {

            CpuInfo cpu = cpuCollector.collect();

            report.setCpu(cpu);

        } catch (Exception e) {

            System.out.println(
                    "Error collecting CPU information"
            );

            e.printStackTrace();
        }


        /*
         * =========================================================
         * MEMORY
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting memory information..."
        );

        try {

            MemoryInfo memory =
                    memoryCollector.collect();

            report.setMemory(memory);

        } catch (Exception e) {

            System.out.println(
                    "Error collecting memory information"
            );

            e.printStackTrace();
        }


        /*
         * =========================================================
         * STORAGE
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting storage information..."
        );

        try {

            StorageInfo storage =
                    storageCollector.collect();

            report.setStorage(storage);

        } catch (Exception e) {

            System.out.println(
                    "Error collecting storage information"
            );

            e.printStackTrace();
        }


        /*
         * =========================================================
         * BATTERY
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting battery information..."
        );

        try {

            BatteryInfo battery =
                    batteryCollector.collect();

            report.setBattery(battery);

        } catch (Exception e) {

            System.out.println(
                    "Error collecting battery information"
            );

            e.printStackTrace();
        }


        /*
         * =========================================================
         * OPERATING SYSTEM
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting OS information..."
        );

        try {

            OsInfo os =
                    osCollector.collect();

            report.setOs(os);

        } catch (Exception e) {

            System.out.println(
                    "Error collecting OS information"
            );

            e.printStackTrace();
        }


        /*
         * =========================================================
         * BIOS
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting BIOS information..."
        );

        try {

            BiosInfo bios =
                    biosCollector.collect();

            report.setBios(bios);

        } catch (Exception e) {

            System.out.println(
                    "Error collecting BIOS information"
            );

            e.printStackTrace();
        }


        /*
         * =========================================================
         * SECURITY
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting security information..."
        );

        try {

            SecurityInfo security =
                    securityCollector.collect();

            report.setSecurity(security);

        } catch (Exception e) {

            System.out.println(
                    "Error collecting security information"
            );

            e.printStackTrace();
        }


        /*
         * =========================================================
         * PERFORMANCE
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting performance information..."
        );

        try {

            PerformanceInfo performance =
                    performanceCollector.collect();

            report.setPerformance(performance);

        } catch (Exception e) {

            System.out.println(
                    "Error collecting performance information"
            );

            e.printStackTrace();
        }


        /*
         * =========================================================
         * VULNERABILITIES
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting vulnerability information..."
        );

        try {

            List<VulnerabilityInfo> vulnerabilities =
                    vulnerabilityCollector.collect();

            report.setVulnerabilities(
                    vulnerabilities
            );

        } catch (Exception e) {

            System.out.println(
                    "Error collecting vulnerability information"
            );

            e.printStackTrace();

            report.setVulnerabilities(
                    List.of()
            );
        }


        /*
         * =========================================================
         * NETWORK
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting network information..."
        );

        try {

            List<NetworkInfo> networks =
                    networkCollector.collect();

            report.setNetworks(networks);

            System.out.println(
                    "Networks returned by NetworkCollector: "
                            + networks.size()
            );

            System.out.println(
                    "Networks inside AssetReport: "
                            + report.getNetworks().size()
            );

        } catch (Exception e) {

            System.out.println(
                    "Error collecting network information"
            );

            e.printStackTrace();

            report.setNetworks(
                    List.of()
            );
        }


        /*
         * =========================================================
         * INSTALLED SOFTWARE
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "Collecting installed software..."
        );

        try {

            List<SoftwareInfo> software =
                    softwareCollector.collect();

            report.setInstalledSoftware(
                    software
            );

            System.out.println(
                    "Software returned by SoftwareCollector: "
                            + software.size()
            );

            System.out.println(
                    "Software inside AssetReport: "
                            + report
                            .getInstalledSoftware()
                            .size()
            );

        } catch (Exception e) {

            System.out.println(
                    "Error collecting installed software"
            );

            e.printStackTrace();

            report.setInstalledSoftware(
                    List.of()
            );
        }


        /*
         * =========================================================
         * AGENT INFORMATION
         * =========================================================
         */

        report.setAgentVersion(
                "1.0.0"
        );

        report.setCollectedAt(
                LocalDateTime.now()
        );


        /*
         * =========================================================
         * FINAL OUTPUT
         * =========================================================
         */

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "       DEVICE COLLECTION COMPLETED"
        );

        System.out.println(
                "======================================"
        );


        return report;
    }


    /*
     * =============================================================
     * DEVICE IDENTITY COLLECTION
     * =============================================================
     */

    private void collectDeviceIdentity(
            AssetReport report) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "       DEVICE IDENTITY COLLECTION"
        );

        System.out.println(
                "======================================"
        );


        try {

            var computerSystem =
                    systemInfo
                            .getHardware()
                            .getComputerSystem();


            String manufacturer =
                    computerSystem.getManufacturer();


            String model =
                    computerSystem.getModel();


            String serialNumber =
                    computerSystem.getSerialNumber();


            report.setBrand(
                    cleanValue(manufacturer)
            );


            report.setModel(
                    cleanValue(model)
            );

            report.setDeviceName(
                    cleanValue(model != null && !model.isBlank() ? model : manufacturer)
            );

            report.setSerialNumber(
                    cleanValue(serialNumber)
            );


            /*
             * Asset tag is not currently stored on the laptop
             * by the agent, so we generate a temporary deterministic
             * asset tag using the serial number.
             *
             * This will be stored in devices.asset_tag.
             */

            String assetTag =
                    createAssetTag(
                            report.getSerialNumber()
                    );


            report.setAssetTag(
                    assetTag
            );


            System.out.println();
            System.out.println(
                    "Brand         : "
                            + report.getBrand()
            );


            System.out.println(
                    "Model         : "
                            + report.getModel()
            );


            System.out.println(
                    "Serial Number : "
                            + report.getSerialNumber()
            );


            System.out.println(
                    "Asset Tag     : "
                            + report.getAssetTag()
            );


        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "ERROR WHILE COLLECTING DEVICE IDENTITY"
            );

            e.printStackTrace();
        }


        System.out.println(
                "======================================"
        );
    }


    private String cleanValue(String value) {

        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.isBlank()) {
            return null;
        }

        return value;
    }


    private String createAssetTag(
            String serialNumber) {

        if (serialNumber == null ||
                serialNumber.isBlank()) {

            return null;
        }

        return "AUTO-" +
                serialNumber.trim();
    }

}