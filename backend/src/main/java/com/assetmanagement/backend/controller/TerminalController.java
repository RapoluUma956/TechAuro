package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.RemoteTerminalSessionRepository;
import com.assetmanagement.backend.websocket.ActiveAgentRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/terminal", "/api/v1/terminal"})
@CrossOrigin(originPatterns = "*")
public class TerminalController {

    private final ActiveAgentRegistry activeAgentRegistry;
    private final DeviceRepository deviceRepository;
    private final RemoteTerminalSessionRepository sessionRepository;

    public TerminalController(
            ActiveAgentRegistry activeAgentRegistry,
            DeviceRepository deviceRepository,
            RemoteTerminalSessionRepository sessionRepository) {
        this.activeAgentRegistry = activeAgentRegistry;
        this.deviceRepository = deviceRepository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/status/{deviceId}")
    public ResponseEntity<?> getTerminalStatus(@PathVariable Long deviceId) {
        Optional<Device> devOpt = deviceRepository.findById(deviceId);
        if (devOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Device not found"
            ));
        }

        Device dev = devOpt.get();
        boolean isConnected = activeAgentRegistry.isAgentOnline(dev.getDeviceId(), dev.getSerialNumber());

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
}

