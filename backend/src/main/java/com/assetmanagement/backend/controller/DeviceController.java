package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.employee.EmployeeDtos.DeviceHistoryResponse;
import com.assetmanagement.backend.dto.employee.EmployeeDtos.DeviceLookupResponse;
import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.service.DeviceService;
import com.assetmanagement.backend.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/devices", "/api/devices"})
@CrossOrigin(originPatterns = "*")
public class DeviceController {

    private final DeviceService deviceService;
    private final EmployeeService employeeService;

    public DeviceController(DeviceService deviceService, EmployeeService employeeService) {
        this.deviceService = deviceService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<Device>> getUnassignedDevices() {
        return ResponseEntity.ok(deviceService.getUnassignedDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<DeviceLookupResponse> getDeviceBySerialNumber(@PathVariable String serialNumber) {
        return ResponseEntity.ok(employeeService.getDeviceBySerial(serialNumber));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<DeviceHistoryResponse> getDeviceHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(employeeService.getDeviceHistory(id, page, size));
    }

    @GetMapping("/asset/{assetTag}")
    public ResponseEntity<Device> getByAssetTag(@PathVariable String assetTag) {
        return ResponseEntity.ok(deviceService.getDeviceByAssetTag(assetTag));
    }

    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(device));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(
            @PathVariable Long id,
            @RequestBody Device device) {
        return ResponseEntity.ok(deviceService.updateDevice(id, device));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
