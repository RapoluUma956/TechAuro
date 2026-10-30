package com.assetmanagement.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String phone,
        String designation,
        LocalDate dateOfJoining,
        String accountStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long assignedDeviceId,
        String assignedSerialNumber,
        String assignedDeviceName,
        String assignedDeviceStatus,
        String assignedConnectionStatus,
        LocalDate assignedWarrantyDate
) {
        public UserResponse(
                Long userId,
                String employeeCode,
                String firstName,
                String lastName,
                String email,
                String phone,
                String designation,
                LocalDate dateOfJoining,
                String accountStatus,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {
                this(userId, employeeCode, firstName, lastName, email, phone, designation, dateOfJoining, accountStatus,
                                createdAt, updatedAt, null, null, null, null, null, null);
        }
}
