package com.assetmanagement.backend.dto.auth;

public record LoginResponseData(
        String accessToken,
        String tokenType,
        long expiresIn,
        boolean passwordChangeRequired,
        UserAuthDto user
) {}
