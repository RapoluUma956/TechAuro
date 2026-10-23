package com.assetmanagement.backend.dto.auth;

public record RefreshResponseData(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
