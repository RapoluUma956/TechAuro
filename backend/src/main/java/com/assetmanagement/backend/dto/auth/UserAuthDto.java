package com.assetmanagement.backend.dto.auth;

import java.util.List;
import java.util.Map;

public record UserAuthDto(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String designation,
        String status,
        List<String> roles,
        Map<String, Object> permissions
) {}
