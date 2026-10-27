package com.assetmanagement.backend.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record DashboardSummaryResponse(
        long totalAssets,
        long onlineAssets,
        long offlineAssets,
        long openVulnerabilities,
        long licensesExpiringSoon,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        java.time.OffsetDateTime lastUpdatedAt,
        DeviceConnectivityDto deviceConnectivity,
        AssetHealthDto assetHealth,
        VulnerabilityOverviewDto vulnerabilityOverview,
        LicenseOverviewDto licenseOverview
) {
    public record DeviceConnectivityDto(
            long online,
            long offline,
            int onlinePercentage,
            int offlinePercentage
    ) {}

    public record AssetHealthDto(
            HealthItemDto healthy,
            HealthItemDto warning,
            HealthItemDto critical,
            HealthItemDto unknown
    ) {
        public record HealthItemDto(
                long count,
                int percentage
        ) {}
    }

    public record VulnerabilityOverviewDto(
            long total,
            long critical,
            long high,
            long medium,
            long low
    ) {}

    public record LicenseOverviewDto(
            long total,
            long expired,
            long expiringWithin7Days,
            long expiringWithin30Days,
            long active
    ) {}
}
