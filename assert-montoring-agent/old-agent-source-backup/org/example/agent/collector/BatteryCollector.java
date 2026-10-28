/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.SystemInfo
 *  oshi.hardware.PowerSource
 */
package org.example.agent.collector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.example.agent.model.BatteryInfo;
import oshi.SystemInfo;
import oshi.hardware.PowerSource;

public class BatteryCollector {
    private final SystemInfo systemInfo;

    public BatteryCollector(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public BatteryInfo collect() {
        Boolean charging;
        BatteryInfo batteryInfo = new BatteryInfo();
        WindowsBatteryData windowsBattery = this.collectWindowsBatteryData();
        PowerSource powerSource = this.getOshiPowerSource();
        if (windowsBattery.percentage != null) {
            batteryInfo.setPercentage(windowsBattery.percentage.doubleValue());
        } else if (powerSource != null) {
            double percentage = powerSource.getRemainingCapacityPercent() * 100.0;
            batteryInfo.setPercentage(percentage);
        }
        if (windowsBattery.charging != null) {
            batteryInfo.setCharging(windowsBattery.charging);
        } else if (powerSource != null) {
            batteryInfo.setCharging(powerSource.isCharging());
        }
        if (powerSource != null) {
            long designCapacity = powerSource.getDesignCapacity();
            long maximumCapacity = powerSource.getMaxCapacity();
            if (designCapacity > 0L && maximumCapacity > 0L) {
                double health = (double)maximumCapacity / (double)designCapacity * 100.0;
                health = Math.min(100.0, Math.max(0.0, health));
                batteryInfo.setHealthPercent(health);
            } else {
                batteryInfo.setHealthPercent(null);
            }
        }
        if (powerSource != null) {
            int cycleCount = powerSource.getCycleCount();
            if (cycleCount >= 0) {
                batteryInfo.setCycleCount(cycleCount);
            } else {
                batteryInfo.setCycleCount(null);
            }
        }
        if ((charging = batteryInfo.getCharging()) != null) {
            if (charging.booleanValue()) {
                batteryInfo.setPowerSource("AC");
            } else {
                batteryInfo.setPowerSource("BATTERY");
            }
        } else {
            batteryInfo.setPowerSource("UNKNOWN");
        }
        return batteryInfo;
    }

    private PowerSource getOshiPowerSource() {
        try {
            List powerSources = this.systemInfo.getHardware().getPowerSources();
            if (powerSources != null && !powerSources.isEmpty()) {
                return (PowerSource)powerSources.get(0);
            }
        }
        catch (Exception e) {
            System.out.println("Unable to read battery through OSHI: " + e.getMessage());
        }
        return null;
    }

    private WindowsBatteryData collectWindowsBatteryData() {
        WindowsBatteryData data = new WindowsBatteryData();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", "Get-CimInstance Win32_Battery | Select-Object EstimatedChargeRemaining, BatteryStatus | ConvertTo-Json -Compress");
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }
            process.waitFor();
            String result = output.toString().trim();
            if (result.isEmpty()) {
                return data;
            }
            data.percentage = this.extractIntegerValue(result, "EstimatedChargeRemaining");
            Integer batteryStatus = this.extractIntegerValue(result, "BatteryStatus");
            if (batteryStatus != null) {
                data.charging = batteryStatus == 2 || batteryStatus == 6 || batteryStatus == 7 || batteryStatus == 8 || batteryStatus == 9 || batteryStatus == 11;
            }
        }
        catch (Exception e) {
            System.out.println("Unable to read Windows battery information: " + e.getMessage());
        }
        return data;
    }

    private Integer extractIntegerValue(String json, String propertyName) {
        try {
            int end;
            int start;
            String search = "\"" + propertyName + "\"";
            int propertyIndex = json.indexOf(search);
            if (propertyIndex < 0) {
                return null;
            }
            int colonIndex = json.indexOf(":", propertyIndex);
            if (colonIndex < 0) {
                return null;
            }
            for (start = colonIndex + 1; start < json.length() && Character.isWhitespace(json.charAt(start)); ++start) {
            }
            for (end = start; end < json.length() && Character.isDigit(json.charAt(end)); ++end) {
            }
            if (start == end) {
                return null;
            }
            return Integer.parseInt(json.substring(start, end));
        }
        catch (Exception e) {
            return null;
        }
    }

    private static class WindowsBatteryData {
        private Integer percentage;
        private Boolean charging;

        private WindowsBatteryData() {
        }
    }
}

