import { useState } from "react";
import { ResourceMeter } from "./DashboardIcons";

export default function AssetsAttentionTable({ data }) {
  const [activeFilterTab, setActiveFilterTab] = useState("all");
  const [assetSearchQuery, setAssetSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = data.pageSize || 4;

  const filteredAssets = data.assets.filter((asset) => {
    // Filter by Tab
    if (activeFilterTab === "critical" && asset.health.toLowerCase() !== "critical") return false;
    if (activeFilterTab === "warning" && asset.health.toLowerCase() !== "warning") return false;
    if (activeFilterTab === "offline" && asset.connection.toLowerCase() !== "offline") return false;

    // Filter by Search Query
    if (assetSearchQuery.trim()) {
      const q = assetSearchQuery.toLowerCase();
      const matchTag = asset.tag.toLowerCase().includes(q);
      const matchEmployee = asset.employee.toLowerCase().includes(q);
      const matchOrg = asset.organization.toLowerCase().includes(q);
      if (!matchTag && !matchEmployee && !matchOrg) return false;
    }

    return true;
  });

  // Calculate dynamic pagination
  const totalPages = Math.max(1, Math.ceil(filteredAssets.length / pageSize));
  const safeCurrentPage = Math.min(currentPage, totalPages);
  const startIndex = (safeCurrentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const paginatedAssets = filteredAssets.slice(startIndex, endIndex);

  // Generate dynamic array of page buttons according to data
  const pageNumbers = Array.from({ length: totalPages }, (_, idx) => idx + 1);

  const handleTabChange = (key) => {
    setActiveFilterTab(key);
    setCurrentPage(1);
  };

  const handleSearchChange = (e) => {
    setAssetSearchQuery(e.target.value);
    setCurrentPage(1);
  };

  return (
    <article className="rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <h2 className="text-base font-bold text-[#03312E]">
          {data.title}
        </h2>

        {/* Search Bar inside table */}
        <div className="relative w-full sm:w-64">
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
            placeholder={data.searchPlaceholder}
            value={assetSearchQuery}
            onChange={handleSearchChange}
            className="w-full rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] py-1.5 pl-9 pr-3 text-xs focus:border-[#1976D2] focus:bg-white focus:outline-none"
          />
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="mt-4 flex flex-wrap gap-2">
        {data.filterTabs.map((tab) => {
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

      {/* Data Table */}
      <div className="mt-4 overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead>
            <tr className="border-b border-[#F1F5F9] text-[#64748B]">
              {data.columns.map((col) => (
                <th key={col.key} className="pb-3 font-semibold">
                  {col.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[#F8FAFC]">
            {paginatedAssets.length > 0 ? (
              paginatedAssets.map((asset) => (
                <tr key={asset.id} className="hover:bg-[#F8FAFC]">
                  <td className="py-3 font-bold text-[#03312E]">{asset.tag}</td>
                  <td className="py-3 text-[#334155]">{asset.employee}</td>
                  <td className="py-3 text-[#64748B]">{asset.organization}</td>
                  <td className="py-3">
                    <span
                      className={`inline-flex items-center rounded-md px-2.5 py-0.5 text-[11px] font-bold ${
                        asset.health === "Critical"
                          ? "bg-[#FEE2E2] text-[#DC2626]"
                          : asset.health === "Warning"
                          ? "bg-[#FEF3C7] text-[#D97706]"
                          : "bg-[#D1FAE5] text-[#059669]"
                      }`}
                    >
                      {asset.health}
                    </span>
                  </td>
                  <td className="py-3">
                    <div className="flex items-center gap-1.5">
                      <span
                        className={`h-2 w-2 rounded-full ${
                          asset.connection === "Online" ? "bg-[#00C896]" : "bg-[#EF4444]"
                        }`}
                      />
                      <span className="font-medium text-[#334155]">{asset.connection}</span>
                    </div>
                  </td>
                  <td className="py-3">
                    <ResourceMeter value={asset.cpu} />
                  </td>
                  <td className="py-3">
                    <ResourceMeter value={asset.ram} />
                  </td>
                  <td className="py-3">
                    <ResourceMeter value={asset.disk} />
                  </td>
                  <td className="py-3 text-[#64748B]">{asset.lastSeen}</td>
                  <td className="py-3">
                    <button type="button" className="font-bold text-[#1976D2] hover:underline">
                      View
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={10} className="py-6 text-center text-xs text-[#94A3B8]">
                  No assets found matching the criteria.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Dynamic Pagination Footer */}
      <div className="mt-4 flex items-center justify-between border-t border-[#F1F5F9] pt-4 text-xs text-[#64748B]">
        <span>
          Showing {filteredAssets.length === 0 ? 0 : startIndex + 1}–{Math.min(endIndex, filteredAssets.length)} of {filteredAssets.length} assets
        </span>

        {totalPages > 1 && (
          <div className="flex items-center gap-1.5">
            {/* Previous Page Button */}
            <button
              type="button"
              disabled={safeCurrentPage === 1}
              onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
              className={`flex h-7 w-7 items-center justify-center rounded-md border border-[#DCE5E1] bg-white transition-all ${
                safeCurrentPage === 1
                  ? "cursor-not-allowed opacity-40 text-[#94A3B8]"
                  : "text-[#64748B] hover:bg-[#F8FAFC]"
              }`}
              aria-label="Previous Page"
            >
              ‹
            </button>

            {/* Dynamic Page Buttons Generated According to Data */}
            {pageNumbers.map((page) => {
              const isActive = page === safeCurrentPage;
              return (
                <button
                  key={page}
                  type="button"
                  onClick={() => setCurrentPage(page)}
                  className={`flex h-7 w-7 items-center justify-center rounded-md transition-all ${
                    isActive
                      ? "bg-[#1976D2] font-bold text-white shadow-sm"
                      : "border border-[#DCE5E1] bg-white text-[#64748B] hover:bg-[#F8FAFC]"
                  }`}
                >
                  {page}
                </button>
              );
            })}

            {/* Next Page Button */}
            <button
              type="button"
              disabled={safeCurrentPage === totalPages}
              onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
              className={`flex h-7 w-7 items-center justify-center rounded-md border border-[#DCE5E1] bg-white transition-all ${
                safeCurrentPage === totalPages
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
