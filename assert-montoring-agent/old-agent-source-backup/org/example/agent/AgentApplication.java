/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent;

import java.util.List;
import org.example.agent.collector.DeviceCollector;
import org.example.agent.model.AssetReport;
import org.example.agent.model.BatteryInfo;
import org.example.agent.model.MemoryInfo;
import org.example.agent.model.NetworkInfo;
import org.example.agent.model.OsInfo;
import org.example.agent.model.SecurityInfo;
import org.example.agent.model.SoftwareInfo;
import org.example.agent.model.StorageInfo;
import org.example.agent.sender.AgentSender;

public class AgentApplication {
    private static final long MONITORING_INTERVAL = 30000L;

    public static void main(String[] args) {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("        ASSET MONITORING AGENT");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("Continuous monitoring started.");
        System.out.println("Collection interval: 30 seconds");
        DeviceCollector collector = new DeviceCollector();
        AgentSender sender = new AgentSender();
        while (true) {
            try {
                while (true) {
                    System.out.println();
                    System.out.println("==============================================");
                    System.out.println("        STARTING NEW MONITORING CYCLE");
                    System.out.println("==============================================");
                    System.out.println();
                    System.out.println("Starting device data collection...");
                    AssetReport report = collector.collect();
                    List<SoftwareInfo> currentSoftware = report.getInstalledSoftware();
                    if (currentSoftware == null) {
                        System.out.println();
                        System.out.println("WARNING: Software inventory is null. Backend must not infer uninstallations.");
                    } else {
                        System.out.println();
                        System.out.println("Installed software inventory collected: " + currentSoftware.size());
                    }
                    System.out.println();
                    System.out.println("==============================================");
                    System.out.println("             DEVICE INFORMATION");
                    System.out.println("==============================================");
                    if (report.getCpu() != null) {
                        System.out.println();
                        System.out.println("========== CPU ==========");
                        System.out.println("CPU Name       : " + report.getCpu().getName());
                        System.out.println("CPU Vendor     : " + report.getCpu().getVendor());
                        System.out.println("CPU Cores      : " + report.getCpu().getCores());
                        System.out.println("CPU Threads    : " + report.getCpu().getThreads());
                        System.out.println("CPU Frequency  : " + report.getCpu().getFrequencyMhz() + " MHz");
                        System.out.println("CPU Usage      : " + report.getCpu().getUsagePercent() + " %");
                    }
                    if (report.getMemory() != null) {
                        MemoryInfo memory = report.getMemory();
                        System.out.println();
                        System.out.println("========== MEMORY ==========");
                        System.out.println("Total Memory   : " + AgentApplication.formatBytes(memory.getTotal()));
                        System.out.println("Used Memory    : " + AgentApplication.formatBytes(memory.getUsed()));
                        System.out.println("Available      : " + AgentApplication.formatBytes(memory.getAvailable()));
                        System.out.println("Usage          : " + memory.getUsagePercent() + " %");
                        System.out.println("Swap Total     : " + AgentApplication.formatBytes(memory.getSwapTotal()));
                        System.out.println("Swap Used      : " + AgentApplication.formatBytes(memory.getSwapUsed()));
                    }
                    if (report.getStorage() != null) {
                        StorageInfo storage = report.getStorage();
                        System.out.println();
                        System.out.println("========== STORAGE ==========");
                        System.out.println("Total Storage  : " + AgentApplication.formatBytes(storage.getTotal()));
                        System.out.println("Used Storage   : " + AgentApplication.formatBytes(storage.getUsed()));
                        System.out.println("Available      : " + AgentApplication.formatBytes(storage.getAvailable()));
                        System.out.println("Usage          : " + storage.getUsagePercent() + " %");
                        System.out.println("Storage Type   : " + storage.getStorageType());
                        System.out.println("Storage Model  : " + storage.getModel());
                        System.out.println("Storage Health : " + storage.getHealth());
                    }
                    if (report.getBattery() != null) {
                        BatteryInfo battery = report.getBattery();
                        System.out.println();
                        System.out.println("========== BATTERY ==========");
                        System.out.println("Percentage     : " + battery.getPercentage() + " %");
                        System.out.println("Charging       : " + battery.getCharging());
                        System.out.println("Health         : " + battery.getHealthPercent() + " %");
                        System.out.println("Cycle Count    : " + battery.getCycleCount());
                        System.out.println("Power Source   : " + battery.getPowerSource());
                    }
                    if (report.getOs() != null) {
                        OsInfo os = report.getOs();
                        System.out.println();
                        System.out.println("========== OPERATING SYSTEM ==========");
                        System.out.println("OS Name        : " + os.getName());
                        System.out.println("OS Version     : " + os.getVersion());
                        System.out.println("OS Build       : " + os.getBuild());
                        System.out.println("Architecture   : " + os.getArchitecture());
                        System.out.println("Hostname       : " + os.getHostname());
                        System.out.println("Boot Time      : " + String.valueOf(os.getBootTime()));
                        System.out.println("Uptime         : " + AgentApplication.formatUptime(os.getUptimeSeconds()));
                    }
                    if (report.getBios() != null) {
                        System.out.println();
                        System.out.println("========== BIOS ==========");
                        System.out.println("Manufacturer   : " + report.getBios().getManufacturer());
                        System.out.println("BIOS Version   : " + report.getBios().getVersion());
                        System.out.println("Release Date   : " + String.valueOf(report.getBios().getReleaseDate()));
                    }
                    if (report.getSecurity() != null) {
                        SecurityInfo security = report.getSecurity();
                        System.out.println();
                        System.out.println("========== SECURITY ==========");
                        System.out.println("BitLocker      : " + security.getBitlockerEnabled());
                        System.out.println("Protection     : " + security.getBitlockerProtectionStatus());
                        System.out.println("Volume Status  : " + security.getBitlockerVolumeStatus());
                        System.out.println("Encryption     : " + security.getBitlockerEncryptionPct() + " %");
                        System.out.println("Encryption Type: " + security.getBitlockerEncryptionMethod());
                        System.out.println("Key Protector  : " + security.getBitlockerKeyProtector());
                        System.out.println("Volume         : " + security.getBitlockerVolume());
                        System.out.println("Secure Boot    : " + security.getSecureBootEnabled());
                        System.out.println("TPM Present    : " + security.getTpmPresent());
                        System.out.println("TPM Enabled    : " + security.getTpmEnabled());
                        System.out.println("TPM Version    : " + security.getTpmVersion());
                    }
                    System.out.println();
                    System.out.println("==============================================");
                    System.out.println("                 NETWORK");
                    System.out.println("==============================================");
                    if (report.getNetworks() != null && !report.getNetworks().isEmpty()) {
                        System.out.println("Number of Networks: " + report.getNetworks().size());
                        for (NetworkInfo network : report.getNetworks()) {
                            System.out.println();
                            System.out.println("----------------------------------------------");
                            System.out.println("Interface Name : " + network.getInterfaceName());
                            System.out.println("Interface Type : " + network.getInterfaceType());
                            System.out.println("Network Name   : " + network.getNetworkName());
                            System.out.println("MAC Address    : " + network.getMacAddress());
                            System.out.println("IPv4 Address   : " + network.getIpv4Address());
                            System.out.println("IPv6 Address   : " + network.getIpv6Address());
                            System.out.println("Subnet Mask    : " + network.getSubnetMask());
                            System.out.println("Gateway        : " + network.getGateway());
                            System.out.println("DNS Servers    : " + String.valueOf(network.getDnsServers()));
                            System.out.println("Status         : " + network.getConnectionStatus());
                            System.out.println("Primary        : " + network.isPrimary());
                            System.out.println("Speed          : " + network.getSpeedMbps());
                            System.out.println("Last Seen      : " + String.valueOf(network.getLastSeenAt()));
                            System.out.println("----------------------------------------------");
                        }
                    } else {
                        System.out.println("No network information available.");
                    }
                    System.out.println();
                    System.out.println("==============================================");
                    System.out.println("            INSTALLED SOFTWARE");
                    System.out.println("==============================================");
                    if (report.getInstalledSoftware() != null && !report.getInstalledSoftware().isEmpty()) {
                        System.out.println("Number of Software: " + report.getInstalledSoftware().size());
                        for (SoftwareInfo software : report.getInstalledSoftware()) {
                            System.out.println();
                            System.out.println("----------------------------------------------");
                            System.out.println("Software Name : " + software.getSoftwareName());
                            System.out.println("Version       : " + software.getVersion());
                            System.out.println("Publisher     : " + software.getPublisher());
                            System.out.println("Install Path  : " + software.getInstallLocation());
                            System.out.println("Installed At  : " + String.valueOf(software.getInstalledAt()));
                            System.out.println("Last Seen     : " + String.valueOf(software.getLastSeenAt()));
                            System.out.println("Status        : " + software.getStatus());
                        }
                    } else {
                        System.out.println("No software inventory returned in this cycle.");
                    }
                    System.out.println();
                    System.out.println("==============================================");
                    System.out.println("              AGENT INFORMATION");
                    System.out.println("==============================================");
                    System.out.println("Agent Version  : " + report.getAgentVersion());
                    System.out.println("Collected At   : " + String.valueOf(report.getCollectedAt()));
                    System.out.println();
                    System.out.println("==============================================");
                    System.out.println("          SENDING DATA TO SERVER");
                    System.out.println("==============================================");
                    sender.sendReport(report);
                    System.out.println();
                    System.out.println("==============================================");
                    System.out.println("        MONITORING CYCLE COMPLETED");
                    System.out.println("==============================================");
                    System.out.println();
                    System.out.println("Next collection will start in 30 seconds...");
                    Thread.sleep(30000L);
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println();
                System.out.println("==============================================");
                System.out.println("       AGENT MONITORING STOPPED");
                System.out.println("==============================================");
                System.out.println("Agent interrupted. Exiting...");
                break;
            }
            catch (Exception e) {
                System.out.println();
                System.out.println("==============================================");
                System.out.println("             AGENT ERROR");
                System.out.println("==============================================");
                e.printStackTrace();
                System.out.println();
                System.out.println("Agent will continue monitoring.");
                System.out.println("Waiting 30 seconds before next attempt...");
                try {
                    Thread.sleep(30000L);
                }
                catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    System.out.println("Agent interrupted while waiting.");
                    break;
                }
            }
        }
    }

    private static String formatBytes(Long bytes) {
        if (bytes == null) {
            return "N/A";
        }
        if (bytes < 1024L) {
            return bytes + " B";
        }
        double kb = (double)bytes.longValue() / 1024.0;
        if (kb < 1024.0) {
            return String.format("%.2f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024.0) {
            return String.format("%.2f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }

    private static String formatUptime(Long seconds) {
        if (seconds == null) {
            return "N/A";
        }
        long days = seconds / 86400L;
        long hours = seconds % 86400L / 3600L;
        long minutes = seconds % 3600L / 60L;
        return days + " days, " + hours + " hours, " + minutes + " minutes";
    }
}

