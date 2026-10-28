/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.SystemInfo
 *  oshi.hardware.GlobalMemory
 *  oshi.hardware.VirtualMemory
 */
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
        GlobalMemory memory = this.systemInfo.getHardware().getMemory();
        VirtualMemory virtualMemory = memory.getVirtualMemory();
        MemoryInfo memoryInfo = new MemoryInfo();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        double usagePercent = 0.0;
        if (total > 0L) {
            usagePercent = (double)used / (double)total * 100.0;
        }
        memoryInfo.setTotal(total);
        memoryInfo.setUsed(used);
        memoryInfo.setAvailable(available);
        memoryInfo.setUsagePercent(usagePercent);
        memoryInfo.setSwapTotal(virtualMemory.getSwapTotal());
        memoryInfo.setSwapUsed(virtualMemory.getSwapUsed());
        return memoryInfo;
    }
}

