package com.assetmanagement.backend.dto;

public record AssignDeviceRequest(
        String serialNumber,
        Long deviceId
) {}

