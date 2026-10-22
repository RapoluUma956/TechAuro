package org.example.agent.collector;

import org.example.agent.model.BatteryInfo;
import oshi.SystemInfo;
import oshi.hardware.PowerSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BatteryCollector {

    private final SystemInfo systemInfo;

    public BatteryCollector(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public BatteryInfo collect() {

        BatteryInfo batteryInfo = new BatteryInfo();

        // ==================================================
        // WINDOWS BATTERY INFORMATION
        // ==================================================

        WindowsBatteryData windowsBattery =
                collectWindowsBatteryData();

        // ==================================================
        // OSHI BATTERY INFORMATION
        // Used as fallback/additional information
        // ==================================================

        PowerSource powerSource =
                getOshiPowerSource();

        // ==================================================
        // BATTERY PERCENTAGE
        // Prefer Windows value
        // ==================================================

        if (windowsBattery.percentage != null) {

            batteryInfo.setPercentage(
                    windowsBattery.percentage.doubleValue()
            );

        } else if (powerSource != null) {

            double percentage =
                    powerSource.getRemainingCapacityPercent()
                            * 100.0;

            batteryInfo.setPercentage(
                    percentage
            );
        }

        // ==================================================
        // CHARGING STATUS
        // Prefer Windows value
        // ==================================================

        if (windowsBattery.charging != null) {

            batteryInfo.setCharging(
                    windowsBattery.charging
            );

        } else if (powerSource != null) {

            batteryInfo.setCharging(
                    powerSource.isCharging()
            );
        }

        // ==================================================
        // BATTERY HEALTH
        // ==================================================

        if (powerSource != null) {

            long designCapacity =
                    powerSource.getDesignCapacity();

            long maximumCapacity =
                    powerSource.getMaxCapacity();

            if (designCapacity > 0
                    && maximumCapacity > 0) {

                double health =
                        ((double) maximumCapacity
                                / designCapacity)
                                * 100.0;

                // Keep health between 0 and 100
                health = Math.min(
                        100.0,
                        Math.max(0.0, health)
                );

                batteryInfo.setHealthPercent(
                        health
                );

            } else {

                batteryInfo.setHealthPercent(
                        null
                );
            }
        }

        // ==================================================
        // BATTERY CYCLE COUNT
        // ==================================================

        if (powerSource != null) {

            int cycleCount =
                    powerSource.getCycleCount();

            if (cycleCount >= 0) {

                batteryInfo.setCycleCount(
                        cycleCount
                );

            } else {

                batteryInfo.setCycleCount(
                        null
                );
            }
        }

        // ==================================================
        // POWER SOURCE
        // ==================================================

        Boolean charging =
                batteryInfo.getCharging();

        if (charging != null) {

            if (charging) {

                batteryInfo.setPowerSource(
                        "AC"
                );

            } else {

                batteryInfo.setPowerSource(
                        "BATTERY"
                );
            }

        } else {

            batteryInfo.setPowerSource(
                    "UNKNOWN"
            );
        }

        return batteryInfo;
    }

    // ==================================================
    // GET OSHI BATTERY
    // ==================================================

    private PowerSource getOshiPowerSource() {

        try {

            List<PowerSource> powerSources =
                    systemInfo
                            .getHardware()
                            .getPowerSources();

            if (powerSources != null
                    && !powerSources.isEmpty()) {

                return powerSources.get(0);
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to read battery through OSHI: "
                            + e.getMessage()
            );
        }

        return null;
    }

    // ==================================================
    // WINDOWS BATTERY
    // ==================================================

    private WindowsBatteryData collectWindowsBatteryData() {

        WindowsBatteryData data =
                new WindowsBatteryData();

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "powershell.exe",
                            "-NoProfile",
                            "-NonInteractive",
                            "-Command",
                            "Get-CimInstance Win32_Battery | " +
                                    "Select-Object EstimatedChargeRemaining, BatteryStatus | " +
                                    "ConvertTo-Json -Compress"
                    );

            Process process =
                    processBuilder.start();

            StringBuilder output =
                    new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream(),
                                         StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    output.append(line);
                }
            }

            process.waitFor();

            String result =
                    output.toString().trim();

            if (result.isEmpty()) {

                return data;
            }

            // ==================================================
            // CURRENT BATTERY PERCENTAGE
            // ==================================================

            data.percentage =
                    extractIntegerValue(
                            result,
                            "EstimatedChargeRemaining"
                    );

            // ==================================================
            // BATTERY STATUS
            // ==================================================

            Integer batteryStatus =
                    extractIntegerValue(
                            result,
                            "BatteryStatus"
                    );

            if (batteryStatus != null) {

                /*
                 * Win32_Battery BatteryStatus:
                 *
                 * 1  = Discharging
                 * 2  = AC
                 * 3  = Fully Charged
                 * 4  = Low
                 * 5  = Critical
                 * 6  = Charging
                 * 7  = Charging and High
                 * 8  = Charging and Low
                 * 9  = Charging and Critical
                 * 10 = Undefined
                 * 11 = Partially Charged
                 */

                data.charging =
                        batteryStatus == 2
                                || batteryStatus == 6
                                || batteryStatus == 7
                                || batteryStatus == 8
                                || batteryStatus == 9
                                || batteryStatus == 11;
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to read Windows battery information: "
                            + e.getMessage()
            );
        }

        return data;
    }

    // ==================================================
    // EXTRACT INTEGER FROM JSON
    // ==================================================

    private Integer extractIntegerValue(
            String json,
            String propertyName) {

        try {

            String search =
                    "\"" + propertyName + "\"";

            int propertyIndex =
                    json.indexOf(search);

            if (propertyIndex < 0) {

                return null;
            }

            int colonIndex =
                    json.indexOf(
                            ":",
                            propertyIndex
                    );

            if (colonIndex < 0) {

                return null;
            }

            int start =
                    colonIndex + 1;

            while (start < json.length()
                    && Character.isWhitespace(
                    json.charAt(start))) {

                start++;
            }

            int end = start;

            while (end < json.length()
                    && Character.isDigit(
                    json.charAt(end))) {

                end++;
            }

            if (start == end) {

                return null;
            }

            return Integer.parseInt(
                    json.substring(start, end)
            );

        } catch (Exception e) {

            return null;
        }
    }

    // ==================================================
    // WINDOWS BATTERY DATA
    // ==================================================

    private static class WindowsBatteryData {

        private Integer percentage;

        private Boolean charging;
    }
}