import { useState, useEffect, useCallback } from "react";
import { dashboardService } from "../../services/dashboardService";
import useAutoRefresh from "../../hooks/useAutoRefresh";
import {
  DashboardHeader,
  DashboardMetricCards,
  DeviceConnectivityCard,
  AssetHealthCard,
  DonutChartCard,
  AssetsAttentionTable,
  QuickActionsCard,
  RecentAlertsCard,
  ThresholdSettingsModal,
} from "../../components/dashboard";

export default function Dashboard() {
  const [summary, setSummary] = useState(null);
  const [alerts, setAlerts] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const [isThresholdModalOpen, setIsThresholdModalOpen] = useState(false);

  // Fetch summary and recent alerts from Spring Boot backend
  const fetchDashboardData = useCallback(async (isManualRefresh = false, isBackground = false) => {
    if (isManualRefresh) {
      setRefreshing(true);
    } else if (!isBackground) {
      setLoading(true);
    }
    setError(null);

    try {
      const [summaryRes, alertsRes] = await Promise.all([
        dashboardService.getSummary(),
        dashboardService.getRecentAlerts(5),
      ]);

      setSummary(summaryRes);
      setAlerts(alertsRes);
      if (isManualRefresh) {
        setRefreshKey((k) => k + 1);
      }
    } catch (err) {
      setError(err.message || "Failed to load dashboard data. Please check your connection.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboardData(false);
  }, [fetchDashboardData]);

  // 30-Second Active-Tab Auto-Refresh
  useAutoRefresh(
    useCallback(() => {
      fetchDashboardData(false, true);
    }, [fetchDashboardData]),
    30000
  );

  const handleManualRefresh = () => {
    fetchDashboardData(true);
  };

  const handleThresholdsUpdated = () => {
    fetchDashboardData(true);
  };

  // 1) Summary Cards Data
  const dashboardStatistics = [
    {
      id: "total-assets",
      title: "Total Assets",
      value: summary?.totalAssets ?? 0,
      caption: "Managed devices",
      icon: "laptop",
      iconBg: "#EBF5FF",
      iconColor: "#1976D2",
      link: "/employees",
    },
    {
      id: "online-assets",
      title: "Online Assets",
      value: summary?.onlineAssets ?? 0,
      caption: "Devices connected",
      icon: "wifi",
      iconBg: "#E6F4EA",
      iconColor: "#00C896",
      link: "/dashboard",
    },
    {
      id: "open-vulnerabilities",
      title: "Open Vulnerabilities",
      value: summary?.openVulnerabilities ?? 0,
      caption: "Require review",
      icon: "shield-alert",
      iconBg: "#FEE2E2",
      iconColor: "#DC2626",
      link: "/notifications",
    },
    {
      id: "license-expiry",
      title: "License Expiry",
      value: summary?.licensesExpiringSoon ?? 0,
      caption: "Expiring soon",
      icon: "key",
      iconBg: "#FEF3C7",
      iconColor: "#D97706",
      link: "/licenses",
    },
  ];

  // 2) Device Connectivity Data
  const deviceConnectivityData = {
    title: "Device Connectivity",
    total: summary?.totalAssets ?? 0,
    online: {
      count: summary?.deviceConnectivity?.online ?? 0,
      percentage: summary?.deviceConnectivity?.onlinePercentage ?? 0,
      color: "var(--color-green)",
    },
    offline: {
      count: summary?.deviceConnectivity?.offline ?? 0,
      percentage: summary?.deviceConnectivity?.offlinePercentage ?? 0,
      color: "var(--color-red)",
    },
  };

  // 3) Asset Health Data
  const assetHealthData = {
    title: "Asset Health",
    items: [
      {
        id: "healthy",
        label: "Healthy",
        count: summary?.assetHealth?.healthy?.count ?? 0,
        percentage: summary?.assetHealth?.healthy?.percentage ?? 0,
        color: "var(--color-green)",
        dotColor: "var(--color-green)",
      },
      {
        id: "warning",
        label: "Warning",
        count: summary?.assetHealth?.warning?.count ?? 0,
        percentage: summary?.assetHealth?.warning?.percentage ?? 0,
        color: "var(--color-yellow)",
        dotColor: "var(--color-yellow)",
      },
      {
        id: "critical",
        label: "Critical",
        count: summary?.assetHealth?.critical?.count ?? 0,
        percentage: summary?.assetHealth?.critical?.percentage ?? 0,
        color: "var(--color-red)",
        dotColor: "var(--color-red)",
      },
      ...((summary?.assetHealth?.unknown?.count > 0 || summary?.assetHealth?.unknown?.percentage > 0)
        ? [
            {
              id: "unknown",
              label: "Unknown",
              count: summary.assetHealth.unknown.count,
              percentage: summary.assetHealth.unknown.percentage,
              color: "#94A3B8",
              dotColor: "#94A3B8",
            },
          ]
        : []),
    ],
  };

  const vulnOverview = summary?.vulnerabilityOverview;
  const vCrit = vulnOverview?.critical ?? 0;
  const vHigh = vulnOverview?.high ?? 0;
  const vLow = vulnOverview?.low ?? 0;
  const vMedRaw = vulnOverview?.medium ?? 0;
  const vTotal = vulnOverview?.total ?? (vCrit + vHigh + vMedRaw + vLow);
  const vMed = vMedRaw + Math.max(0, vTotal - (vCrit + vHigh + vMedRaw + vLow));

  // 4) Vulnerabilities Donut Card Data
  const vulnerabilitiesBySeverityData = {
    title: "Open Vulnerabilities by Severity",
    total: vTotal,
    centerLabel: "Total",
    segments: [
      {
        id: "critical",
        label: "Critical",
        count: vCrit,
        color: "var(--color-red)",
      },
      {
        id: "high",
        label: "High",
        count: vHigh,
        color: "var(--color-orange)",
      },
      {
        id: "medium",
        label: "Medium",
        count: vMed,
        color: "var(--color-yellow)",
      },
      {
        id: "low",
        label: "Low",
        count: vLow,
        color: "var(--color-blue)",
      },
    ],
  };
  // 5) License Expiry Donut Card Data
  const licenseExpiryOverviewData = {
    title: "License Expiry Overview",
    total: summary?.licenseOverview?.total ?? 0,
    centerLabel: "Expiring Soon",
    segments: [
      {
        id: "expired",
        label: "Expired",
        count: summary?.licenseOverview?.expired ?? 0,
        color: "var(--color-red)",
      },
      {
        id: "expiring-7-days",
        label: "Expiring (≤ 7 days)",
        count: summary?.licenseOverview?.expiringWithin7Days ?? 0,
        color: "var(--color-orange)",
      },
      {
        id: "expiring-30-days",
        label: "Expiring (≤ 30 days)",
        count: summary?.licenseOverview?.expiringWithin30Days ?? 0,
        color: "var(--color-yellow)",
      },
      {
        id: "active",
        label: "Active",
        count: summary?.licenseOverview?.active ?? 0,
        color: "var(--color-green)",
      },
    ],
  };

  // 6) Recent Alerts Data
  const recentAlertsData = {
    title: "Recent Alerts",
    viewAllLink: "/notifications",
    items: alerts?.items || [],
  };

  // 7) Filter Counts for Attention Table
  const filterCounts = {
    all: summary?.totalAssets ?? 0,
    critical: summary?.assetHealth?.critical?.count ?? 0,
    warning: summary?.assetHealth?.warning?.count ?? 0,
    offline: summary?.deviceConnectivity?.offline ?? 0,
  };

  return (
    <div className="space-y-6 text-[#24211D]">
      {/* 8) Header & Last Updated */}
      <DashboardHeader
        data={{
          title: "Dashboard",
          subtitle: "Overview of your IT infrastructure and security posture",
        }}
        lastUpdatedAt={summary?.lastUpdatedAt}
        onRefresh={handleManualRefresh}
        onOpenThresholds={() => setIsThresholdModalOpen(true)}
        refreshing={refreshing}
      />

      {/* Error Banner */}
      {error && (
        <div className="flex items-center justify-between rounded-2xl border border-[#FEE2E2] bg-[#FEF2F2] p-4 text-xs font-medium text-[#DC2626]">
          <div className="flex items-center gap-2">
            <svg className="h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{error}</span>
          </div>
          <button
            type="button"
            onClick={handleManualRefresh}
            className="rounded-lg bg-[#DC2626] px-3 py-1 font-semibold text-white shadow-sm hover:bg-[#B91C1C]"
          >
            Retry
          </button>
        </div>
      )}

      {/* 1) Top 4 Metric Summary Cards */}
      {loading ? (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="flex items-center gap-4 rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-sm animate-pulse"
            >
              <div className="h-14 w-14 rounded-2xl bg-[#E2E8F0]" />
              <div className="flex-1 space-y-2">
                <div className="h-3 w-1/2 rounded bg-[#E2E8F0]" />
                <div className="h-6 w-1/3 rounded bg-[#CBD5E1]" />
                <div className="h-2.5 w-2/3 rounded bg-[#F1F5F9]" />
              </div>
            </div>
          ))}
        </div>
      ) : (
        <DashboardMetricCards statistics={dashboardStatistics} />
      )}

      {/* Main Dashboard Layout */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
        <div className="space-y-6 lg:col-span-8">
          {/* 2 & 3) Connectivity and Health */}
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <DeviceConnectivityCard data={deviceConnectivityData} />
            <AssetHealthCard data={assetHealthData} />
          </div>

          {/* 4 & 5) Donut Charts: Vulnerabilities & License Expiry */}
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <DonutChartCard
              title={vulnerabilitiesBySeverityData.title}
              total={vulnerabilitiesBySeverityData.total}
              centerLabel={vulnerabilitiesBySeverityData.centerLabel}
              segments={vulnerabilitiesBySeverityData.segments}
            />
            <DonutChartCard
              title={licenseExpiryOverviewData.title}
              total={licenseExpiryOverviewData.total}
              centerLabel={licenseExpiryOverviewData.centerLabel}
              segments={licenseExpiryOverviewData.segments}
            />
          </div>

          {/* 7) Assets Requiring Attention Table */}
          <AssetsAttentionTable
            filterCounts={filterCounts}
            refreshKey={refreshKey}
          />
        </div>

        {/* Right Sidebar: 6) Recent Alerts & Quick Actions */}
        <div className="space-y-6 lg:col-span-4">
          <RecentAlertsCard
            data={recentAlertsData}
            loading={loading}
          />
          <QuickActionsCard
            onOpenThresholds={() => setIsThresholdModalOpen(true)}
          />
        </div>
      </div>

      {/* 9) Threshold Configuration Modal */}
      <ThresholdSettingsModal
        isOpen={isThresholdModalOpen}
        onClose={() => setIsThresholdModalOpen(false)}
        onThresholdsUpdated={handleThresholdsUpdated}
      />
    </div>
  );
}
