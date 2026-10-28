/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.SystemInfo
 *  oshi.hardware.CentralProcessor
 *  oshi.hardware.CentralProcessor$ProcessorIdentifier
 */
package org.example.agent.collector;

import org.example.agent.model.CpuInfo;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class CpuCollector {
    private final SystemInfo systemInfo;

    public CpuCollector(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public CpuInfo collect() {
        CpuInfo cpuInfo = new CpuInfo();
        CentralProcessor processor = this.systemInfo.getHardware().getProcessor();
        CentralProcessor.ProcessorIdentifier identifier = processor.getProcessorIdentifier();
        cpuInfo.setName(identifier.getName());
        cpuInfo.setVendor(identifier.getVendor());
        cpuInfo.setCores(processor.getPhysicalProcessorCount());
        cpuInfo.setThreads(processor.getLogicalProcessorCount());
        long[] previousTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000L);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double usage = processor.getSystemCpuLoadBetweenTicks(previousTicks) * 100.0;
        usage = Math.min(100.0, Math.max(0.0, usage));
        cpuInfo.setUsagePercent(usage);
        long[] currentFrequencies = processor.getCurrentFreq();
        long averageFrequency = 0L;
        if (currentFrequencies != null && currentFrequencies.length > 0) {
            long totalFrequency = 0L;
            int validValues = 0;
            for (long frequency : currentFrequencies) {
                if (frequency <= 0L) continue;
                totalFrequency += frequency;
                ++validValues;
            }
            if (validValues > 0) {
                averageFrequency = totalFrequency / (long)validValues;
            }
        }
        if (averageFrequency <= 0L) {
            averageFrequency = processor.getMaxFreq();
        }
        if (averageFrequency > 0L) {
            cpuInfo.setFrequencyMhz(averageFrequency / 1000000L);
        } else {
            cpuInfo.setFrequencyMhz(null);
        }
        return cpuInfo;
    }
}

