package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.RemoteAccessAudit;
import com.assetmanagement.backend.entity.RemoteAccessSession;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.RemoteAccessAuditRepository;
import com.assetmanagement.backend.repository.RemoteAccessSessionRepository;
import com.assetmanagement.backend.websocket.RemoteDesktopRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/remote-access", "/api/v1/remote-access"})
@CrossOrigin(originPatterns = "*")
public class RemoteAccessController {

    private final RemoteDesktopRegistry registry;
    private final DeviceRepository deviceRepository;
    private final RemoteAccessSessionRepository sessionRepository;
    private final RemoteAccessAuditRepository auditRepository;

    public RemoteAccessController(
            RemoteDesktopRegistry registry,
            DeviceRepository deviceRepository,
            RemoteAccessSessionRepository sessionRepository,
            RemoteAccessAuditRepository auditRepository) {
        this.registry = registry;
        this.deviceRepository = deviceRepository;
        this.sessionRepository = sessionRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Check if the employee's monitoring agent is currently connected and ready for remote desktop.
     */
    @GetMapping("/status/{deviceId}")
    public ResponseEntity<?> getRemoteAccessStatus(@PathVariable Long deviceId) {
        Optional<Device> devOpt = deviceRepository.findById(deviceId);
        if (devOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Device not found"
            ));
        }

        Device dev = devOpt.get();
        boolean isConnected = registry.isAgentOnline(dev.getDeviceId(), dev.getSerialNumber());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "deviceId", dev.getDeviceId(),
                "serialNumber", dev.getSerialNumber() != null ? dev.getSerialNumber() : "",
                "deviceName", dev.getDeviceName() != null ? dev.getDeviceName() : "",
                "hostname", dev.getHostname() != null ? dev.getHostname() : "",
                "os", (dev.getOsName() != null ? dev.getOsName() : "") + (dev.getOsVersion() != null ? " " + dev.getOsVersion() : ""),
                "isAgentConnected", isConnected,
                "connectionStatus", dev.getConnectionStatus() != null ? dev.getConnectionStatus() : "UNKNOWN"
        ));
    }

    /**
     * Get historical remote desktop sessions for a device.
     */
    @GetMapping("/sessions/{deviceId}")
    public ResponseEntity<?> getSessionsForDevice(@PathVariable Long deviceId) {
        List<RemoteAccessSession> sessions = sessionRepository.findByDeviceIdOrderByRequestedAtDesc(deviceId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "sessions", sessions
        ));
    }

    /**
     * Get audit logs for a specific remote access session.
     */
    @GetMapping("/audit/{sessionId}")
    public ResponseEntity<?> getSessionAudit(@PathVariable String sessionId) {
        List<RemoteAccessAudit> auditLogs = auditRepository.findBySessionIdOrderByRecordedAtAsc(sessionId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "sessionId", sessionId,
                "auditLogs", auditLogs
        ));
    }
}
