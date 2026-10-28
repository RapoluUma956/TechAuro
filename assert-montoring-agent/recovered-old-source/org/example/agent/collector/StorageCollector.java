/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.SystemInfo
 *  oshi.hardware.HWDiskStore
 *  oshi.software.os.OSFileStore
 */
package org.example.agent.collector;

import java.util.List;
import org.example.agent.model.StorageInfo;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OSFileStore;

public class StorageCollector {
    private final SystemInfo systemInfo;

    public StorageCollector(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public StorageInfo collect() {
        StorageInfo storageInfo = new StorageInfo();
        List fileStores = this.systemInfo.getOperatingSystem().getFileSystem().getFileStores();
        long total = 0L;
        long available = 0L;
        for (OSFileStore fileStore : fileStores) {
            long storeTotal = fileStore.getTotalSpace();
            long storeAvailable = fileStore.getUsableSpace();
            if (storeTotal <= 0L) continue;
            total += storeTotal;
            available += storeAvailable;
        }
        long used = total - available;
        double usagePercent = 0.0;
        if (total > 0L) {
            usagePercent = (double)used / (double)total * 100.0;
        }
        storageInfo.setTotal(total);
        storageInfo.setUsed(used);
        storageInfo.setAvailable(available);
        storageInfo.setUsagePercent(usagePercent);
        List disks = this.systemInfo.getHardware().getDiskStores();
        if (!disks.isEmpty()) {
            HWDiskStore disk = (HWDiskStore)disks.get(0);
            storageInfo.setModel(disk.getModel());
            storageInfo.setHealth("AVAILABLE");
            storageInfo.setStorageType("UNKNOWN");
        } else {
            storageInfo.setModel("UNKNOWN");
            storageInfo.setHealth("UNKNOWN");
            storageInfo.setStorageType("UNKNOWN");
        }
        return storageInfo;
    }
}

