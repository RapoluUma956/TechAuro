package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.HealthThreshold;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.HealthThresholdRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Proactive Device Heartbeat & Connectivity Monitor Service
 *
 * Runs background sweeps and provides real-time resolution of whether a device
 * is genuinely ONLINE or has timed out into OFFLINE state based on configurable
 * HealthThreshold.offlineTimeoutMinutes.
 */
@Service
public class DeviceHeartbeatMonitorService {

    private static final Logger log = LoggerFactory.getLogger(DeviceHeartbeatMonitorService.class);

    private final DeviceRepository deviceRepository;
    private final HealthThresholdRepository healthThresholdRepository;
    private final NotificationService notificationService;

    public DeviceHeartbeatMonitorService(
            DeviceRepository deviceRepository,
            HealthThresholdRepository healthThresholdRepository,
            NotificationService notificationService) {
        this.deviceRepository = deviceRepository;
        this.healthThresholdRepository = healthThresholdRepository;
        this.notificationService = notificationService;
    }

    /**
     * Resolves the current configured offline timeout threshold in minutes (default: 5 min).
     */
    public int getTimeoutMinutes() {
        return healthThresholdRepository.findCurrentThresholds()
                .map(HealthThreshold::getOfflineTimeoutMinutes)
                .filter(m -> m != null && m > 0)
                .orElse(5);
    }

    /**
     * Real-time calculation of effective connection status:
     * Returns "ONLINE" if the device has transmitted telemetry within the threshold window,
     * otherwise "OFFLINE".
     */
    public String resolveEffectiveStatus(Device device, int timeoutMinutes) {
        if (device == null) {
            return "OFFLINE";
        }

        LocalDateTime lastTelemetry = device.getRecordedAt() != null
                ? device.getRecordedAt()
                : (device.getLastSeenAt() != null ? device.getLastSeenAt() : device.getUpdatedAt());

        if (lastTelemetry == null) {
            return "OFFLINE";
        }

        long minutesAgo = Duration.between(lastTelemetry, LocalDateTime.now()).toMinutes();
        if (minutesAgo > timeoutMinutes) {
            return "OFFLINE";
        }

        return "ONLINE";
    }

    public String resolveEffectiveStatus(Device device) {
        return resolveEffectiveStatus(device, getTimeoutMinutes());
    }

    /**
     * Sweep on server boot to immediately synchronize any devices that went offline
     * while the backend was restarted or idle.
     */
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("[HeartbeatMonitor] Application ready. Running initial connectivity sweep...");
        sweepDeviceHeartbeats();
    }

    /**
     * Periodic background sweep executed every 15 seconds.
     * Transitions inactive devices to OFFLINE in the database and triggers alert synchronization.
     */
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void sweepDeviceHeartbeats() {
        int timeoutMinutes = getTimeoutMinutes();
        List<Device> devices = deviceRepository.findAll();
        int transitionedToOffline = 0;
        int transitionedToOnline = 0;

        for (Device device : devices) {
            String currentStored = device.getConnectionStatus() != null ? device.getConnectionStatus() : "OFFLINE";
            String effective = resolveEffectiveStatus(device, timeoutMinutes);

            if (!effective.equalsIgnoreCase(currentStored)) {
                device.setConnectionStatus(effective);
                deviceRepository.save(device);

                if ("OFFLINE".equalsIgnoreCase(effective)) {
                    transitionedToOffline++;
                    log.info("[HeartbeatMonitor] Device '{}' (SN: {}) has timed out (> {} min inactivity). Transitioned to OFFLINE.",
                            device.getDeviceName(), device.getSerialNumber(), timeoutMinutes);
                } else {
                    transitionedToOnline++;
                    log.info("[HeartbeatMonitor] Device '{}' (SN: {}) telemetry received. Transitioned to ONLINE.",
                            device.getDeviceName(), device.getSerialNumber());
                }
            }
        }

        if (transitionedToOffline > 0 || transitionedToOnline > 0) {
            try {
                notificationService.syncSystemNotifications();
            } catch (Exception e) {
                log.warn("[HeartbeatMonitor] Error synchronizing notifications after connectivity sweep: {}", e.getMessage());
            }
        }
    }
}

