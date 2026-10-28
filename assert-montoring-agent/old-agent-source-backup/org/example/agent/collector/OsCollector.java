/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.SystemInfo
 *  oshi.software.os.OperatingSystem
 *  oshi.software.os.OperatingSystem$OSVersionInfo
 */
package org.example.agent.collector;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.example.agent.model.OsInfo;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

public class OsCollector {
    private final SystemInfo systemInfo;

    public OsCollector(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public OsInfo collect() {
        OsInfo osInfo = new OsInfo();
        OperatingSystem operatingSystem = this.systemInfo.getOperatingSystem();
        osInfo.setName(operatingSystem.getFamily());
        OperatingSystem.OSVersionInfo versionInfo = operatingSystem.getVersionInfo();
        if (versionInfo != null) {
            osInfo.setVersion(versionInfo.getVersion());
            osInfo.setBuild(versionInfo.getBuildNumber());
        }
        String architecture = System.getProperty("os.arch");
        osInfo.setArchitecture(architecture);
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            osInfo.setHostname(hostname);
        }
        catch (Exception e) {
            String hostname = System.getenv("COMPUTERNAME");
            if (hostname != null && !hostname.isBlank()) {
                osInfo.setHostname(hostname);
            }
            osInfo.setHostname("UNKNOWN");
        }
        long bootTimeSeconds = operatingSystem.getSystemBootTime();
        if (bootTimeSeconds > 0L) {
            LocalDateTime bootTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(bootTimeSeconds), ZoneId.systemDefault());
            osInfo.setBootTime(bootTime);
            long currentTimeSeconds = System.currentTimeMillis() / 1000L;
            long uptime = currentTimeSeconds - bootTimeSeconds;
            if (uptime < 0L) {
                uptime = 0L;
            }
            osInfo.setUptimeSeconds(uptime);
        }
        return osInfo;
    }
}

