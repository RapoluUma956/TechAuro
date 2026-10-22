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

        CentralProcessor processor =
                systemInfo
                        .getHardware()
                        .getProcessor();

        CentralProcessor.ProcessorIdentifier identifier =
                processor.getProcessorIdentifier();

        // ==================================================
        // CPU NAME
        // ==================================================

        cpuInfo.setName(
                identifier.getName()
        );

        // ==================================================
        // CPU VENDOR
        // ==================================================

        cpuInfo.setVendor(
                identifier.getVendor()
        );

        // ==================================================
        // PHYSICAL CORES
        // ==================================================

        cpuInfo.setCores(
                processor.getPhysicalProcessorCount()
        );

        // ==================================================
        // LOGICAL PROCESSORS / THREADS
        // ==================================================

        cpuInfo.setThreads(
                processor.getLogicalProcessorCount()
        );

        // ==================================================
        // CPU USAGE
        //
        // Measure CPU activity over one second.
        // ==================================================

        long[] previousTicks =
                processor.getSystemCpuLoadTicks();

        try {

            Thread.sleep(1000);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        double usage =
                processor.getSystemCpuLoadBetweenTicks(
                        previousTicks
                ) * 100.0;

        // Keep value between 0 and 100.
        usage = Math.min(
                100.0,
                Math.max(0.0, usage)
        );

        cpuInfo.setUsagePercent(
                usage
        );

        // ==================================================
        // CURRENT CPU FREQUENCY
        // ==================================================

        long[] currentFrequencies =
                processor.getCurrentFreq();

        long averageFrequency = 0;

        if (currentFrequencies != null
                && currentFrequencies.length > 0) {

            long totalFrequency = 0;
            int validValues = 0;

            for (long frequency :
                    currentFrequencies) {

                if (frequency > 0) {

                    totalFrequency += frequency;
                    validValues++;
                }
            }

            if (validValues > 0) {

                averageFrequency =
                        totalFrequency / validValues;
            }
        }

        // ==================================================
        // FALLBACK
        //
        // Some systems may not expose current frequency.
        // In that case use maximum frequency.
        // ==================================================

        if (averageFrequency <= 0) {

            averageFrequency =
                    processor.getMaxFreq();
        }

        if (averageFrequency > 0) {

            cpuInfo.setFrequencyMhz(
                    averageFrequency / 1_000_000
            );

        } else {

            cpuInfo.setFrequencyMhz(
                    null
            );
        }

        return cpuInfo;
    }
}