package org.example.agent.collector;

import org.example.agent.model.StorageInfo;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OSFileStore;

import java.util.List;

public class StorageCollector {

    private final SystemInfo systemInfo;

    public StorageCollector(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public StorageInfo collect() {

        StorageInfo storageInfo = new StorageInfo();

        // ==================================================
        // FILE SYSTEM STORAGE
        // ==================================================

        List<OSFileStore> fileStores =
                systemInfo.getOperatingSystem()
                        .getFileSystem()
                        .getFileStores();

        long total = 0;
        long available = 0;

        for (OSFileStore fileStore : fileStores) {

            long storeTotal = fileStore.getTotalSpace();
            long storeAvailable = fileStore.getUsableSpace();

            if (storeTotal > 0) {
                total += storeTotal;
                available += storeAvailable;
            }
        }

        long used = total - available;

        double usagePercent = 0.0;

        if (total > 0) {
            usagePercent =
                    ((double) used / total) * 100.0;
        }

        storageInfo.setTotal(total);
        storageInfo.setUsed(used);
        storageInfo.setAvailable(available);
        storageInfo.setUsagePercent(usagePercent);

        storageInfo.setTotalGb(bytesToGb(total));
        storageInfo.setUsedGb(bytesToGb(used));
        storageInfo.setAvailableGb(bytesToGb(available));

        // ==================================================
        // PHYSICAL DISK INFORMATION
        // ==================================================

        List<HWDiskStore> disks =
                systemInfo.getHardware()
                        .getDiskStores();

        if (!disks.isEmpty()) {

            HWDiskStore disk = disks.get(0);
            String model = disk.getModel() != null ? disk.getModel().trim() : "UNKNOWN";

            storageInfo.setModel(model);
            storageInfo.setHealth("GOOD");

            String upperModel = model.toUpperCase();
            if (upperModel.contains("NVME")) {
                storageInfo.setStorageType("SSD");
                storageInfo.setSsdType("NVMe");
                storageInfo.setSsdModel(model);
                storageInfo.setSsdHealth("GOOD");
            } else if (upperModel.contains("SSD")) {
                storageInfo.setStorageType("SSD");
                storageInfo.setSsdType("SSD");
                storageInfo.setSsdModel(model);
                storageInfo.setSsdHealth("GOOD");
            } else {
                storageInfo.setStorageType("HDD");
            }

        } else {

            storageInfo.setModel(
                    "UNKNOWN"
            );

            storageInfo.setHealth(
                    "UNKNOWN"
            );

            storageInfo.setStorageType(
                    "UNKNOWN"
            );
        }

        return storageInfo;
    }

    private double bytesToGb(long bytes) {
        if (bytes <= 0) {
            return 0.0;
        }
        return Math.round((bytes / (1024.0 * 1024.0 * 1024.0)) * 100.0) / 100.0;
    }
}