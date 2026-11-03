package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.dashboard.*;
import com.assetmanagement.backend.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/dashboard", "/api/dashboard"})
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 1) GET /api/dashboard/summary (Auto-delegates or scopes according to effective data scope)
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    // 1.1) GET /api/dashboard/admin-summary (Organization-wide metrics)
    @GetMapping("/admin-summary")
    public ResponseEntity<AdminDashboardSummaryResponse> getAdminSummary() {
        return ResponseEntity.ok(dashboardService.getAdminSummary());
    }

    // 1.2) GET /api/dashboard/my-summary (Logged-in employee profile, laptop, telemetry, accessories, licenses)
    @GetMapping("/my-summary")
    public ResponseEntity<EmployeeDashboardSummaryResponse> getMySummary() {
        return ResponseEntity.ok(dashboardService.getMySummary());
    }

    // 1.3) GET /api/dashboard/manager-summary (Managed team devices, health, licenses, alerts)
    @GetMapping("/manager-summary")
    public ResponseEntity<ManagerDashboardSummaryResponse> getManagerSummary() {
        return ResponseEntity.ok(dashboardService.getManagerSummary());
    }

    // 2) GET /api/dashboard/recent-alerts?limit=5
    @GetMapping("/recent-alerts")
    public ResponseEntity<DashboardAlertsResponse> getRecentAlerts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentAlerts(limit));
    }

    // 3) GET /api/dashboard/assets/attention?filter=ALL&search=&page=0&size=4&sortBy=lastSeenAt&sortDir=asc
    @GetMapping("/assets/attention")
    public ResponseEntity<AttentionAssetsResponse> getAssetsRequiringAttention(
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "lastSeenAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(dashboardService.getAssetsRequiringAttention(
                filter, search, page, size, sortBy, sortDir));
    }

    // 4) GET /api/dashboard/thresholds
    @GetMapping("/thresholds")
    public ResponseEntity<ThresholdDto> getThresholds() {
        return ResponseEntity.ok(dashboardService.getThresholds());
    }

    // 5) PUT /api/dashboard/thresholds
    @PutMapping("/thresholds")
    public ResponseEntity<ThresholdDto> updateThresholds(
            @Valid @RequestBody UpdateThresholdRequest request) {
        return ResponseEntity.ok(dashboardService.updateThresholds(request));
    }
}
