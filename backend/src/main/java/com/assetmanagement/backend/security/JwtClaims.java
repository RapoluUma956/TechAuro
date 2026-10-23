package com.assetmanagement.backend.security;

import java.util.List;

public record JwtClaims(
        Long userId,
        String email,
        String employeeCode,
        List<String> roles,
        boolean passwordChangeRequired,
        long issuedAt,
        long expiresAt
) {}
