package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.repository.DeviceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<Device> getUnassignedDevices() {
        return deviceRepository.findAll().stream()
                .filter(d -> d.getCurrentUserId() == null)
                .toList();
    }

    public Device getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with id: " + deviceId));
    }

    public Device getDeviceBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumberIgnoreCase(serialNumber)
                .orElseGet(() -> deviceRepository.findBySerialNumber(serialNumber)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Device not found with serial number: " + serialNumber)));
    }

    public Device getDeviceByAssetTag(String assetTag) {
        return deviceRepository.findByAssetTag(assetTag)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with asset tag: " + assetTag));
    }

    @Transactional
    public Device createDevice(Device device) {
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
        Device device = getDeviceById(deviceId);
        deviceRepository.delete(device);
    }
}
