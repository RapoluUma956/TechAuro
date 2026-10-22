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

        // ==================================================
        // PHYSICAL DISK INFORMATION
        // ==================================================

        List<HWDiskStore> disks =
                systemInfo.getHardware()
                        .getDiskStores();

        if (!disks.isEmpty()) {

            HWDiskStore disk = disks.get(0);

            storageInfo.setModel(
                    disk.getModel()
            );

            storageInfo.setHealth(
                    "AVAILABLE"
            );

            // OSHI does not reliably expose
            // SSD/HDD type on every platform.
            storageInfo.setStorageType(
                    "UNKNOWN"
            );

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
}