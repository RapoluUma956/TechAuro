package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Device getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Device not found with id: " + deviceId
                        )
                );
    }

    public Device getDeviceByAssetTag(String assetTag) {
        return deviceRepository.findByAssetTag(assetTag)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Device not found with asset tag: " + assetTag
                        )
                );
    }

    @Transactional
    public Device createDevice(Device device) {

        if (deviceRepository.existsByAssetTag(device.getAssetTag())) {
            throw new RuntimeException(
                    "Asset tag already exists: "
                            + device.getAssetTag()
            );
        }

        if (deviceRepository.existsBySerialNumber(
                device.getSerialNumber())) {

            throw new RuntimeException(
                    "Serial number already exists: "
                            + device.getSerialNumber()
            );
        }

        return deviceRepository.save(device);
    }

    @Transactional
    public Device updateDevice(Long deviceId, Device updatedDevice) {

        Device existingDevice = getDeviceById(deviceId);

        existingDevice.setAssetTag(updatedDevice.getAssetTag());
        existingDevice.setSerialNumber(updatedDevice.getSerialNumber());
        existingDevice.setBrand(updatedDevice.getBrand());
        existingDevice.setModel(updatedDevice.getModel());
        existingDevice.setWarrantyStartDate(
                updatedDevice.getWarrantyStartDate()
        );
        existingDevice.setWarrantyEndDate(
                updatedDevice.getWarrantyEndDate()
        );
        existingDevice.setStatus(updatedDevice.getStatus());

        return deviceRepository.save(existingDevice);
    }

    @Transactional
    public void deleteDevice(Long deviceId) {

        Device device = getDeviceById(deviceId);

        deviceRepository.delete(device);
    }
}