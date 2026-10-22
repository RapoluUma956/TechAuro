package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.service.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {

        return ResponseEntity.ok(
                deviceService.getAllDevices()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                deviceService.getDeviceById(id)
        );
    }

    @GetMapping("/asset/{assetTag}")
    public ResponseEntity<Device> getByAssetTag(
            @PathVariable String assetTag) {

        return ResponseEntity.ok(
                deviceService.getDeviceByAssetTag(assetTag)
        );
    }

    @PostMapping
    public ResponseEntity<Device> createDevice(
            @RequestBody Device device) {

        return ResponseEntity.ok(
                deviceService.createDevice(device)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(
            @PathVariable Long id,
            @RequestBody Device device) {

        return ResponseEntity.ok(
                deviceService.updateDevice(id, device)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable Long id) {

        deviceService.deleteDevice(id);

        return ResponseEntity.noContent().build();
    }
}