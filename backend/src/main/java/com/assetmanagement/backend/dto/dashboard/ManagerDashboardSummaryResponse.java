package com.assetmanagement.backend.dto.dashboard;

import java.util.List;

public record ManagerDashboardSummaryResponse(
        long assignedEmployeeCount,
        long assignedDeviceCount,
        DashboardSummaryResponse.AssetHealthDto deviceHealth,
        Double averageCpuUsage,
        Double averageRamUsage,
        long assignedAccessoriesCount,
        long licenseAllocationsCount,
        DashboardSummaryResponse.VulnerabilityOverviewDto vulnerabilityOverview,
        List<AdminDashboardSummaryResponse.DashboardAlertItem> recentAlerts
) {}

