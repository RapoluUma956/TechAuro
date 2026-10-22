package org.example.agent.collector;

import org.example.agent.model.OsInfo;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class OsCollector {

    private final SystemInfo systemInfo;

    public OsCollector(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public OsInfo collect() {

        OsInfo osInfo = new OsInfo();

        OperatingSystem operatingSystem =
                systemInfo.getOperatingSystem();

        // ==================================================
        // OS NAME
        // ==================================================

        osInfo.setName(
                operatingSystem.getFamily()
        );

        // ==================================================
        // OS VERSION AND BUILD
        // ==================================================

        OperatingSystem.OSVersionInfo versionInfo =
                operatingSystem.getVersionInfo();

        if (versionInfo != null) {

            osInfo.setVersion(
                    versionInfo.getVersion()
            );

            osInfo.setBuild(
                    versionInfo.getBuildNumber()
            );
        }

        // ==================================================
        // ARCHITECTURE
        // ==================================================

        String architecture =
                System.getProperty("os.arch");

        osInfo.setArchitecture(
                architecture
        );

        // ==================================================
        // HOSTNAME
        // ==================================================

        try {

            String hostname =
                    InetAddress
                            .getLocalHost()
                            .getHostName();

            osInfo.setHostname(
                    hostname
            );

        } catch (Exception e) {

            // Fallback for Windows
            String hostname =
                    System.getenv("COMPUTERNAME");

            if (hostname != null
                    && !hostname.isBlank()) {

                osInfo.setHostname(
                        hostname
                );

            } else {

                osInfo.setHostname(
                        "UNKNOWN"
                );
            }
        }

        // ==================================================
        // BOOT TIME
        // ==================================================

        long bootTimeSeconds =
                operatingSystem.getSystemBootTime();

        if (bootTimeSeconds > 0) {

            LocalDateTime bootTime =
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(
                                    bootTimeSeconds
                            ),
                            ZoneId.systemDefault()
                    );

            osInfo.setBootTime(
                    bootTime
            );

            // ==================================================
            // UPTIME
            // ==================================================

            long currentTimeSeconds =
                    System.currentTimeMillis()
                            / 1000;

            long uptime =
                    currentTimeSeconds
                            - bootTimeSeconds;

            if (uptime < 0) {
                uptime = 0;
            }

            osInfo.setUptimeSeconds(
                    uptime
            );
        }

        return osInfo;
    }
}