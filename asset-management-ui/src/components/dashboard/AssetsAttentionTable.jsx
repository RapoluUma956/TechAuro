import { useState, useEffect, useRef, useCallback } from "react";
import { Link } from "react-router-dom";
import { ResourceMeter, SortIndicator } from "../common/Icons";
import { dashboardService } from "../../services/dashboardService";

function formatRelativeTime(dateString) {
  if (!dateString) return "Never";
  try {
    const diffMs = Date.now() - new Date(dateString).getTime();
    if (diffMs < 0 || diffMs < 60000) return "Just now";
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    return `${days}d ago`;
  } catch {
    return dateString;
  }
}

export default function AssetsAttentionTable({
  filterCounts = {},
  refreshKey = 0,
}) {
  const [activeFilterTab, setActiveFilterTab] = useState("ALL");
  const [assetSearchQuery, setAssetSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed for backend
  const [sortBy, setSortBy] = useState("lastSeenAt");
  const [sortDir, setSortDir] = useState("asc");

  const [assets, setAssets] = useState([]);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 4,
    totalElements: 0,
    totalPages: 1,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const debounceTimerRef = useRef(null);

  // Fetch attention assets from backend
  const fetchAttentionAssets = useCallback(
    async (filter, search, page, sortField, sortDirection) => {
      setLoading(true);
      setError(null);
      try {
        const response = await dashboardService.getAssetsRequiringAttention({
          filter,
          search: search.trim(),
          page,
          size: 4,
          sortBy: sortField,
          sortDir: sortDirection,
        });

        if (response) {
          setAssets(response.items || []);
          if (response.pagination) {
            setPagination(response.pagination);
          }
        }
      } catch (err) {
        setError(err.message || "Failed to load assets requiring attention");
      } finally {
        setLoading(false);
      }
    },
    []
  );

  // Trigger fetch when filter, page, sort, or refreshKey changes
  useEffect(() => {
    fetchAttentionAssets(activeFilterTab, assetSearchQuery, currentPage, sortBy, sortDir);
  }, [activeFilterTab, currentPage, sortBy, sortDir, refreshKey, fetchAttentionAssets]);

  // Handle Search Input with 300ms Debounce
  const handleSearchChange = (e) => {
    const val = e.target.value;
    setAssetSearchQuery(val);
    setCurrentPage(0);

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    debounceTimerRef.current = setTimeout(() => {
      fetchAttentionAssets(activeFilterTab, val, 0, sortBy, sortDir);
    }, 300);
  };

  const handleTabChange = (key) => {
    setActiveFilterTab(key);
    setCurrentPage(0);
  };

  const handleSort = (columnKey) => {
    if (sortBy === columnKey) {
      setSortDir((prev) => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortBy(columnKey);
      setSortDir("asc");
    }
    setCurrentPage(0);
  };

  const filterTabs = [
    { key: "ALL",      label: "All",      count: filterCounts.all      ?? pagination.totalElements },
    { key: "CRITICAL", label: "Critical", count: filterCounts.critical ?? 0 },
    { key: "WARNING",  label: "Warning",  count: filterCounts.warning  ?? 0 },
    { key: "OFFLINE",  label: "Offline",  count: filterCounts.offline  ?? 0 },
  ];

  // Each column has a fixed minimum width so columns never collapse
  const columns = [
    { key: "assetTag",         label: "Device / Asset Tag", sortable: true,  minWidth: "min-w-[160px]" },
    { key: "employeeName",     label: "Employee",           sortable: true,  minWidth: "min-w-[140px]" },
    { key: "designation",      label: "Organization",       sortable: true,  minWidth: "min-w-[140px]" },
    { key: "healthStatus",     label: "Health",             sortable: true,  minWidth: "min-w-[100px]" },
    { key: "connectionStatus", label: "Connection",         sortable: true,  minWidth: "min-w-[110px]" },
    { key: "cpuUsage",         label: "CPU",                sortable: true,  minWidth: "min-w-[110px]" },
    { key: "ramUsage",         label: "RAM",                sortable: true,  minWidth: "min-w-[110px]" },
    { key: "diskUsage",        label: "Disk",               sortable: true,  minWidth: "min-w-[110px]" },
    { key: "lastSeenAt",       label: "Last Seen",          sortable: true,  minWidth: "min-w-[110px]" },
    { key: "action",           label: "Action",             sortable: false, minWidth: "min-w-[80px]"  },
  ];

  const totalPages = Math.max(1, pagination.totalPages || 1);
  const safeCurrentPage = pagination.page; // 0-indexed
  const startAssetNum = pagination.totalElements === 0 ? 0 : safeCurrentPage * pagination.size + 1;
  const endAssetNum = Math.min((safeCurrentPage + 1) * pagination.size, pagination.totalElements);

  // Generate array of page numbers (1-indexed for display)
  const pageNumbers = Array.from({ length: totalPages }, (_, idx) => idx + 1);

  return (
    <article className="rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
      <div className="flex flex-col gap-3">
        <div className="flex items-center gap-2">
          <h2 className="text-base font-bold text-[#03312E]">
            Assets Requiring Attention
          </h2>
          {loading && (
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent" />
          )}
        </div>

        {/* Full-width Search Bar inside table card */}
        <div className="relative w-full">
          <svg
            className="absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#94A3B8]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            placeholder="Search assets, employees, tags..."
            value={assetSearchQuery}
            onChange={handleSearchChange}
            className="w-full rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] py-2 pl-9 pr-3 text-xs text-[#03312E] placeholder-[#8C9E9B] focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 transition-all"
          />
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="mt-4 flex flex-wrap gap-2">
        {filterTabs.map((tab) => {
          const isActive = activeFilterTab === tab.key;
          return (
            <button
              key={tab.key}
              type="button"
              onClick={() => handleTabChange(tab.key)}
              className={`rounded-lg px-3 py-1 text-xs font-semibold transition-all ${
                isActive
                  ? "bg-[#1976D2] text-white shadow-sm"
                  : "bg-[#F1F5F9] text-[#64748B] hover:bg-[#E2E8F0]"
              }`}
            >
              {tab.label} ({tab.count})
            </button>
          );
        })}
      </div>

      {error && (
        <div className="mt-4 rounded-xl border border-[#FEE2E2] bg-[#FEF2F2] p-3 text-xs font-medium text-[#DC2626]">
          {error}
        </div>
      )}

      {/* Data Table — min-w-[1140px] forces horizontal scroll instead of squishing columns */}
      <div className="mt-4 overflow-hidden rounded-xl border border-[#DCE5E1]">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[1140px] border-collapse text-left text-xs text-[#03312E]">
            <thead>
              <tr className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-xs font-bold uppercase tracking-wider text-[#64748B]">
                {columns.map((col) => (
                  <th
                    key={col.key}
                    className={`whitespace-nowrap py-3.5 px-4 ${col.minWidth} ${
                      col.sortable ? "cursor-pointer select-none hover:text-[#03312E]" : ""
                    }`}
                    onClick={() => col.sortable && handleSort(col.key)}
                  >
                    <span className="inline-flex items-center gap-1">
                      {col.label}
                      {col.sortable && (
                        <SortIndicator active={sortBy === col.key} direction={sortDir} />
                      )}
                    </span>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className={`divide-y divide-[#EBF0EE] bg-white ${loading ? "opacity-60 transition-opacity" : ""}`}>
              {assets.length > 0 ? (
                assets.map((asset) => {
                  const healthUpper = (asset.healthStatus || "").toUpperCase();
                  const connUpper = (asset.connectionStatus || "").toUpperCase();
                  const isOnline = connUpper === "ONLINE";

                  return (
                    <tr
                      key={asset.deviceId || asset.assetTag}
                      className="hover:bg-[#F8FAF9] transition-colors"
                    >
                      {/* Device / Asset Tag */}
                      <td className="whitespace-nowrap py-3.5 px-4 font-bold text-sm text-[#03312E]">
                        {asset.assetTag}
                      </td>

                      {/* Employee */}
                      <td className="whitespace-nowrap py-3.5 px-4 text-sm font-medium text-[#334155]">
                        {asset.employeeName || "Unassigned"}
                      </td>

                      {/* Organization / Designation */}
                      <td className="whitespace-nowrap py-3.5 px-4 text-xs font-medium text-[#64748B]">
                        {asset.designation || "-"}
                      </td>

                      {/* Health Badge */}
                      <td className="whitespace-nowrap py-3.5 px-4">
                        <span
                          className={`inline-flex items-center rounded-md px-2.5 py-0.5 text-[11px] font-bold ${
                            healthUpper === "CRITICAL"
                              ? "bg-[#FEE2E2] text-[#DC2626]"
                              : healthUpper === "WARNING"
                              ? "bg-[#FEF3C7] text-[#D97706]"
                              : healthUpper === "HEALTHY"
                              ? "bg-[#D1FAE5] text-[#059669]"
                              : "bg-[#F1F5F9] text-[#64748B]"
                          }`}
                        >
                          {healthUpper === "CRITICAL"
                            ? "Critical"
                            : healthUpper === "WARNING"
                            ? "Warning"
                            : healthUpper === "HEALTHY"
                            ? "Healthy"
                            : "Unknown"}
                        </span>
                      </td>

                      {/* Connection Status */}
                      <td className="whitespace-nowrap py-3.5 px-4">
                        <div className="flex items-center gap-1.5">
                          <span
                            className={`h-2 w-2 rounded-full ${
                              isOnline ? "bg-[#00C896]" : "bg-[#EF4444]"
                            }`}
                          />
                          <span className="font-medium text-[#334155]">
                            {isOnline ? "Online" : "Offline"}
                          </span>
                        </div>
                      </td>

                      {/* CPU Usage Meter */}
                      <td className="whitespace-nowrap py-3.5 px-4">
                        <ResourceMeter value={asset.cpuUsage ?? 0} />
                      </td>

                      {/* RAM Usage Meter */}
                      <td className="whitespace-nowrap py-3.5 px-4">
                        <ResourceMeter value={asset.ramUsage ?? 0} />
                      </td>

                      {/* Disk Usage Meter */}
                      <td className="whitespace-nowrap py-3.5 px-4">
                        <ResourceMeter value={asset.diskUsage ?? 0} />
                      </td>

                      {/* Last Seen */}
                      <td className="whitespace-nowrap py-3.5 px-4 text-xs font-medium text-[#64748B]">
                        {formatRelativeTime(asset.lastSeenAt)}
                      </td>

                      {/* View Action */}
                      <td className="whitespace-nowrap py-3.5 px-4">
                        <Link
                          to="/employees"
                          className="inline-flex items-center gap-1 rounded-lg px-2.5 py-1 text-xs font-bold text-[#1976D2] transition-colors hover:bg-[#E3F2FD] hover:text-[#1565C0]"
                        >
                          View
                        </Link>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={10} className="py-8 text-center text-xs text-[#94A3B8]">
                    {loading
                      ? "Loading assets requiring attention..."
                      : "No assets found matching the criteria."}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Dynamic Pagination Footer */}
      <div className="mt-4 flex items-center justify-between border-t border-[#F1F5F9] pt-4 text-xs text-[#64748B]">
        <span>
          Showing {startAssetNum}–{endAssetNum} of {pagination.totalElements} assets
        </span>

        {totalPages > 1 && (
          <div className="flex items-center gap-1.5">
            {/* Previous Page Button */}
            <button
              type="button"
              disabled={safeCurrentPage === 0 || loading}
              onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
              className={`flex h-7 w-7 items-center justify-center rounded-md border border-[#DCE5E1] bg-white transition-all ${
                safeCurrentPage === 0
                  ? "cursor-not-allowed opacity-40 text-[#94A3B8]"
                  : "text-[#64748B] hover:bg-[#F8FAFC]"
              }`}
              aria-label="Previous Page"
            >
              ‹
            </button>

            {/* Dynamic Page Buttons */}
            {pageNumbers.map((p) => {
              const pageIdx = p - 1;
              const isActive = pageIdx === safeCurrentPage;
              return (
                <button
                  key={p}
                  type="button"
                  disabled={loading}
                  onClick={() => setCurrentPage(pageIdx)}
                  className={`flex h-7 w-7 items-center justify-center rounded-md transition-all ${
                    isActive
                      ? "bg-[#1976D2] font-bold text-white shadow-sm"
                      : "border border-[#DCE5E1] bg-white text-[#64748B] hover:bg-[#F8FAFC]"
                  }`}
                >
                  {p}
                </button>
              );
            })}

            {/* Next Page Button */}
            <button
              type="button"
              disabled={safeCurrentPage >= totalPages - 1 || loading}
              onClick={() => setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))}
              className={`flex h-7 w-7 items-center justify-center rounded-md border border-[#DCE5E1] bg-white transition-all ${
                safeCurrentPage >= totalPages - 1
                  ? "cursor-not-allowed opacity-40 text-[#94A3B8]"
                  : "text-[#64748B] hover:bg-[#F8FAFC]"
              }`}
              aria-label="Next Page"
            >
              ›
            </button>
          </div>
        )}
      </div>
    </article>
  );
}
