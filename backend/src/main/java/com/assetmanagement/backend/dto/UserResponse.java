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
        LocalDateTime updatedAt
) {}
