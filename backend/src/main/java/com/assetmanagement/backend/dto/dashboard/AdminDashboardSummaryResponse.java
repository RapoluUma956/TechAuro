package com.assetmanagement.backend.dto.dashboard;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminDashboardSummaryResponse(
        long totalEmployees,
        long totalDevices,
        long assignedDevices,
        long unassignedDevices,
        long inRepairDevices,
        long totalAccessories,
        long assignedAccessories,
        long availableAccessories,
        long totalLicenses,
        long assignedLicenses,
        long availableLicenses,
        long licensesExpiringSoon,
        DashboardSummaryResponse.AssetHealthDto assetHealth,
        DashboardSummaryResponse.VulnerabilityOverviewDto vulnerabilityOverview,
        OffsetDateTime lastUpdatedAt,
        List<DashboardAlertItem> globalAlerts
) {
    public record DashboardAlertItem(
            Long notificationId,
            String title,
            String message,
            String severity,
            String type,
            java.time.LocalDateTime createdAt
    ) {}
}

