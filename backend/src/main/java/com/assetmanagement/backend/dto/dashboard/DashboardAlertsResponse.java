package com.assetmanagement.backend.dto.dashboard;

import java.util.List;

public record DashboardAlertsResponse(
        List<DashboardAlertItem> items,
        int total
) {}
