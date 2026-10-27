package org.example.agent.collector;

import org.example.agent.model.MemoryInfo;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;

public class MemoryCollector {

    private final SystemInfo systemInfo;

    public MemoryCollector(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public MemoryInfo collect() {

        GlobalMemory memory =
                systemInfo.getHardware().getMemory();

        VirtualMemory virtualMemory =
                memory.getVirtualMemory();

        MemoryInfo memoryInfo = new MemoryInfo();

        long total = memory.getTotal();

        long available = memory.getAvailable();

        long used = total - available;

        double usagePercent = 0.0;

        if (total > 0) {
            usagePercent =
                    ((double) used / total) * 100.0;
        }

        memoryInfo.setTotal(total);
        memoryInfo.setUsed(used);
        memoryInfo.setAvailable(available);
        memoryInfo.setUsagePercent(usagePercent);

        memoryInfo.setTotalGb(bytesToGb(total));
        memoryInfo.setUsedGb(bytesToGb(used));
        memoryInfo.setAvailableGb(bytesToGb(available));

        long swapTotal = virtualMemory.getSwapTotal();
        long swapUsed = virtualMemory.getSwapUsed();
        memoryInfo.setSwapTotal(swapTotal);
        memoryInfo.setSwapUsed(swapUsed);
        memoryInfo.setSwapTotalGb(bytesToGb(swapTotal));
        memoryInfo.setSwapUsedGb(bytesToGb(swapUsed));

        try {
            var physicalList = memory.getPhysicalMemory();
            if (physicalList != null && !physicalList.isEmpty()) {
                var first = physicalList.get(0);
                if (first.getMemoryType() != null && !first.getMemoryType().isBlank()) {
                    memoryInfo.setRamType(first.getMemoryType());
                }
                long speed = first.getClockSpeed();
                if (speed > 100_000) {
                    memoryInfo.setRamSpeedMhz(speed / 1_000_000L);
                } else if (speed > 0) {
                    memoryInfo.setRamSpeedMhz(speed);
                }
            }
        } catch (Exception ignored) {
        }

        return memoryInfo;
    }

    private double bytesToGb(long bytes) {
        if (bytes <= 0) {
            return 0.0;
        }
        return Math.round((bytes / (1024.0 * 1024.0 * 1024.0)) * 100.0) / 100.0;
    }
}