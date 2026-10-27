import { api } from "./api";

/**
 * Dashboard API Service
 * 
 * Interacts with Spring Boot backend endpoints for:
 * 1) GET  /api/dashboard/summary
 * 2) GET  /api/dashboard/recent-alerts?limit=5
 * 3) GET  /api/dashboard/assets/attention?filter=ALL&search=&page=0&size=4&sortBy=lastSeenAt&sortDir=asc
 * 4) GET  /api/dashboard/thresholds
 * 5) PUT  /api/dashboard/thresholds
 */
export const dashboardService = {
  /**
   * 1) Retrieve overall summary cards, connectivity, health, vulnerability, and license overviews
   * @returns {Promise<DashboardSummaryResponse>}
   */
  async getSummary() {
    return await api.get("/dashboard/summary");
  },

  /**
   * 2) Retrieve recent dynamically generated alerts
   * @param {number} limit 
   * @returns {Promise<{ items: Array, total: number }>}
   */
  async getRecentAlerts(limit = 5) {
    return await api.get(`/dashboard/recent-alerts?limit=${encodeURIComponent(limit)}`);
  },

  /**
   * 3) Retrieve searchable, filterable, sortable, and paginated assets requiring attention
   * @param {Object} params
   * @param {string} [params.filter='ALL'] - 'ALL' | 'CRITICAL' | 'WARNING' | 'OFFLINE'
   * @param {string} [params.search='']
   * @param {number} [params.page=0]
   * @param {number} [params.size=4]
   * @param {string} [params.sortBy='lastSeenAt']
   * @param {string} [params.sortDir='asc'] - 'asc' | 'desc'
   * @returns {Promise<{ items: Array, pagination: { page: number, size: number, totalElements: number, totalPages: number } }>}
   */
  async getAssetsRequiringAttention({
    filter = "ALL",
    search = "",
    page = 0,
    size = 4,
    sortBy = "lastSeenAt",
    sortDir = "asc",
  } = {}) {
    const params = new URLSearchParams({
      filter: filter || "ALL",
      search: search || "",
      page: String(page ?? 0),
      size: String(size ?? 4),
      sortBy: sortBy || "lastSeenAt",
      sortDir: sortDir || "asc",
    });

    return await api.get(`/dashboard/assets/attention?${params.toString()}`);
  },

  /**
   * 4) Retrieve configurable health thresholds
   * @returns {Promise<ThresholdDto>}
   */
  async getThresholds() {
    return await api.get("/dashboard/thresholds");
  },

  /**
   * 5) Update configurable health thresholds
   * @param {Object} thresholds
   * @param {number} thresholds.cpuWarningPercent
   * @param {number} thresholds.ramWarningPercent
   * @param {number} thresholds.diskCriticalPercent
   * @param {number} thresholds.offlineTimeoutMinutes
   * @param {number} thresholds.warrantyWarningDays
   * @param {number} thresholds.licenseWarningDays
   * @returns {Promise<ThresholdDto>}
   */
  async updateThresholds(thresholds) {
    return await api.put("/dashboard/thresholds", thresholds);
  },
};
