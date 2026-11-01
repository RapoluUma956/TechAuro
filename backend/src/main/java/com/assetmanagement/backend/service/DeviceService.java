package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.repository.DeviceRepository;

import com.assetmanagement.backend.security.AuthorizationService;
import com.assetmanagement.backend.security.AuthorizedScope;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceHeartbeatMonitorService heartbeatMonitorService;
    private final AuthorizationService authorizationService;

    public DeviceService(DeviceRepository deviceRepository, DeviceHeartbeatMonitorService heartbeatMonitorService,
            AuthorizationService authorizationService) {
        this.deviceRepository = deviceRepository;
        this.heartbeatMonitorService = heartbeatMonitorService;
        this.authorizationService = authorizationService;
    }

    public List<Device> getAllDevices() {
        AuthorizedScope scope = authorizationService.getAuthorizedScope("EMPLOYEES_DEVICES");
        List<Device> list;
        if (scope.isAll()) {
            list = deviceRepository.findAll();
        } else if (scope.isEmpty()) {
            return Collections.emptyList();
        } else {
            Set<Long> deviceIds = authorizationService.resolveAuthorizedDeviceIds(scope);
            list = (deviceIds == null || deviceIds.isEmpty()) ? Collections.emptyList() : deviceRepository.findByDeviceIdIn(deviceIds);
        }
        for (Device d : list) {
            d.setConnectionStatus(heartbeatMonitorService.resolveEffectiveStatus(d));
        }
        return list;
    }

    public List<Device> getUnassignedDevices() {
        authorizationService.validateAccess("EMPLOYEES_DEVICES", "WRITE");
        return getAllDevices().stream()
                .filter(d -> d.getCurrentUserId() == null)
                .toList();
    }

    public Device getDeviceById(Long deviceId) {
        authorizationService.validateDeviceAccess(deviceId, "EMPLOYEES_DEVICES", "READ");
        Device d = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with id: " + deviceId));
        d.setConnectionStatus(heartbeatMonitorService.resolveEffectiveStatus(d));
        return d;
    }

    public Device getDeviceBySerialNumber(String serialNumber) {
        Device d = deviceRepository.findBySerialNumberIgnoreCase(serialNumber)
                .orElseGet(() -> deviceRepository.findBySerialNumber(serialNumber)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Device not found with serial number: " + serialNumber)));
        authorizationService.validateDeviceAccess(d.getDeviceId(), "EMPLOYEES_DEVICES", "READ");
        d.setConnectionStatus(heartbeatMonitorService.resolveEffectiveStatus(d));
        return d;
    }

    public Device getDeviceByAssetTag(String assetTag) {
        Device d = deviceRepository.findByAssetTag(assetTag)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with asset tag: " + assetTag));
        authorizationService.validateDeviceAccess(d.getDeviceId(), "EMPLOYEES_DEVICES", "READ");
        d.setConnectionStatus(heartbeatMonitorService.resolveEffectiveStatus(d));
        return d;
    }

    @Transactional
    public Device createDevice(Device device) {
        authorizationService.validateAccess("EMPLOYEES_DEVICES", "WRITE");
        if (device.getSerialNumber() == null || device.getSerialNumber().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Serial number is required");
        }

        String cleanSerial = device.getSerialNumber().trim().toUpperCase();
        device.setSerialNumber(cleanSerial);

        if (device.getAssetTag() == null || device.getAssetTag().isBlank()) {
            device.setAssetTag("AST-" + cleanSerial);
        }

        if (device.getDeviceName() == null || device.getDeviceName().isBlank()) {
            String b = device.getBrand() != null ? device.getBrand() : "";
            String m = device.getModel() != null ? device.getModel() : "Workstation";
            device.setDeviceName((b + " " + m).trim());
        }

        if (device.getConnectionStatus() == null || device.getConnectionStatus().isBlank()) {
            device.setConnectionStatus("OFFLINE");
        }

        if (device.getDeviceStatus() == null || device.getDeviceStatus().isBlank()) {
            device.setDeviceStatus("ACTIVE");
        }

        if (device.getRamTotalGb() != null && device.getRamGb() == null) {
            device.setRamGb(device.getRamTotalGb());
        }
        if (device.getStorageTotalGb() != null && device.getSsdCapacityGb() == null) {
            device.setSsdCapacityGb(device.getStorageTotalGb());
        }

        // If device already exists with this serial, update its basic descriptive info
        // while preserving any live agent metrics already reported
        return deviceRepository.findBySerialNumberIgnoreCase(cleanSerial)
                .map(existing -> {
                    if (device.getDeviceName() != null && !device.getDeviceName().isBlank()) {
                        existing.setDeviceName(device.getDeviceName());
                    }
                    if (device.getBrand() != null && !device.getBrand().isBlank()) {
                        existing.setBrand(device.getBrand());
                    }
                    if (device.getModel() != null && !device.getModel().isBlank()) {
                        existing.setModel(device.getModel());
                    }
                    if (device.getAssetTag() != null && !device.getAssetTag().isBlank() && existing.getAssetTag() == null) {
                        existing.setAssetTag(device.getAssetTag());
                    }
                    if (device.getRamTotalGb() != null) {
                        existing.setRamTotalGb(device.getRamTotalGb());
                        existing.setRamGb(device.getRamTotalGb());
                    }
                    if (device.getStorageTotalGb() != null) {
                        existing.setStorageTotalGb(device.getStorageTotalGb());
                        existing.setSsdCapacityGb(device.getStorageTotalGb());
                    }
                    if (device.getOsName() != null && !device.getOsName().isBlank()) {
                        existing.setOsName(device.getOsName());
                    }
                    if (device.getWarrantyEndDate() != null) {
                        existing.setWarrantyEndDate(device.getWarrantyEndDate());
                    }
                    if (device.getDeviceStatus() != null) {
                        existing.setDeviceStatus(device.getDeviceStatus());
                    }
                    return deviceRepository.save(existing);
                })
                .orElseGet(() -> deviceRepository.save(device));
    }

    @Transactional
    public Device updateDevice(Long deviceId, Device updatedDevice) {
        authorizationService.validateDeviceAccess(deviceId, "EMPLOYEES_DEVICES", "WRITE");
        Device existingDevice = getDeviceById(deviceId);

        existingDevice.setAssetTag(updatedDevice.getAssetTag());
        existingDevice.setSerialNumber(updatedDevice.getSerialNumber());
        existingDevice.setBrand(updatedDevice.getBrand());
        existingDevice.setModel(updatedDevice.getModel());
        existingDevice.setWarrantyStartDate(updatedDevice.getWarrantyStartDate());
        existingDevice.setWarrantyEndDate(updatedDevice.getWarrantyEndDate());
        existingDevice.setStatus(updatedDevice.getStatus());

        return deviceRepository.save(existingDevice);
    }

    @Transactional
    public void deleteDevice(Long deviceId) {
        authorizationService.validateDeviceAccess(deviceId, "EMPLOYEES_DEVICES", "WRITE");
        Device device = getDeviceById(deviceId);
        deviceRepository.delete(device);
    }
}
