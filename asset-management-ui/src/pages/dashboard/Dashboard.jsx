import {
  dashboardHeaderData,
  dashboardStatistics,
  deviceConnectivityData,
  assetHealthData,
  vulnerabilitiesBySeverityData,
  licenseExpiryOverviewData,
  quickActionsData,
  recentAlertsData,
  assetsRequiringAttentionData,
} from "../../data/dashboardData";

import {
  DashboardHeader,
  DashboardMetricCards,
  DeviceConnectivityCard,
  AssetHealthCard,
  DonutChartCard,
  AssetsAttentionTable,
  QuickActionsCard,
  RecentAlertsCard,
} from "../../components/dashboard";

export default function Dashboard() {
  return (
    <div className="space-y-6 text-[#24211D]">
      <DashboardHeader data={dashboardHeaderData} />
      <DashboardMetricCards statistics={dashboardStatistics} />
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
        <div className="space-y-6 lg:col-span-8">
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <DeviceConnectivityCard data={deviceConnectivityData} />
            <AssetHealthCard data={assetHealthData} />
          </div>

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

          <AssetsAttentionTable data={assetsRequiringAttentionData} />
        </div>

        <div className="space-y-6 lg:col-span-4">
          <RecentAlertsCard data={recentAlertsData} />
          <QuickActionsCard data={quickActionsData} />   
        </div>
      </div>
    </div>
  );
}
