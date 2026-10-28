package com.assetmanagement.backend.dto.license;

public record LicenseSummaryResponse(
        long totalLicenses,
        long totalSeats,
        long assignedSeats,
        long availableSeats,
        long expiringSoonCount
) {}
