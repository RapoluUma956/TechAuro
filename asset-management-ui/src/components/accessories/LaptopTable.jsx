import React, { useState } from "react";
import { SearchBar, Button, Pagination } from "../common";
import { LaptopIcon, RotateCcwIcon } from "./AccessoryIcons";

export default function LaptopTable({
  laptopConfigurations = [],
  loading = false,
  totalElements = 0,
  currentPage = 0,
  pageSize = 10,
  totalPages = 1,
  onPageChange,
  searchQuery = "",
  onSearchChange,
  selectedBrand = "All",
  onBrandChange,
  onRefresh,
  onAssignLaptop,
}) {
  const [expandedKeys, setExpandedKeys] = useState({});

  const toggleExpand = (key) => {
    setExpandedKeys((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const brands = ["All", "Lenovo", "Dell", "HP", "Apple", "Asus"];

  const isFiltered = Boolean(
    searchQuery.trim() || (selectedBrand && selectedBrand !== "All")
  );

  const handleClearFilters = () => {
    onSearchChange && onSearchChange("");
    onBrandChange && onBrandChange("All");
  };

  return (
    <div className="overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white shadow-sm">
      {/* Top Header */}
      <div className="border-b border-[#EBF0EE] p-5 sm:p-6">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-[#E3F2FD] text-[#1976D2]">
              <LaptopIcon className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-[#03312E] sm:text-xl">
                Laptop Inventory
              </h2>
              <p className="text-xs text-[#64748B]">
                Individually serialized hardware records grouped by configuration
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {isFiltered && (
              <Button
                variant="outline"
                size="sm"
                icon={<RotateCcwIcon className="h-3.5 w-3.5" />}
                onClick={handleClearFilters}
                className="text-[#5C7370] hover:border-[#EF4444] hover:bg-[#FEECEB] hover:text-[#EF4444]"
              >
                Reset
              </Button>
            )}
            <Button
              variant="outline"
              size="sm"
              onClick={onRefresh}
              icon={<RotateCcwIcon className="h-3.5 w-3.5" />}
            >
              Refresh
            </Button>
          </div>
        </div>

        {/* Search Bar & Brand Filter Pills */}
        <div className="mt-4 space-y-3">
          <div className="w-full">
            <SearchBar
              value={searchQuery}
              onChange={onSearchChange}
              placeholder="Search laptop brand, model, RAM, SSD..."
              variant="long"
            />
          </div>

          <div className="flex flex-wrap items-center gap-1.5 pt-1">
            <span className="mr-1 text-xs font-semibold text-[#64748B]">
              Brand:
            </span>
            {brands.map((b) => {
              const isSelected =
                selectedBrand === b ||
                (!selectedBrand && b === "All") ||
                selectedBrand?.toLowerCase() === b.toLowerCase();

              return (
                <button
                  key={b}
                  type="button"
                  onClick={() => onBrandChange && onBrandChange(b)}
                  className={`cursor-pointer rounded-full px-3 py-1 text-xs font-semibold transition-all select-none ${
                    isSelected
                      ? "bg-[#1976D2] text-white shadow-xs ring-1 ring-[#1976D2]"
                      : "border border-[#DCE5E1] bg-white text-[#5C7370] hover:border-[#1976D2] hover:bg-[#F8FAF9] hover:text-[#03312E]"
                  }`}
                >
                  {b}
                </button>
              );
            })}
          </div>

          {isFiltered && (
            <div className="flex items-center justify-between border-t border-[#EBF0EE] pt-2.5 text-xs text-[#64748B]">
              <span>
                Showing <strong className="text-[#03312E]">{laptopConfigurations.length}</strong> matching configurations
              </span>
              <button
                type="button"
                onClick={handleClearFilters}
                className="cursor-pointer font-semibold text-[#1976D2] hover:underline"
              >
                Clear all filters
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-[#03312E]">
          <thead className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-xs font-bold uppercase tracking-wider text-[#5C7370]">
            <tr>
              <th scope="col" className="w-16 py-3.5 pl-6 pr-2 text-center">
                S.No
              </th>
              <th scope="col" className="py-3.5 px-4">
                Configuration & Model
              </th>
              <th scope="col" className="py-3.5 px-4">
                RAM / Storage
              </th>
              <th scope="col" className="py-3.5 px-4 text-center">
                Total
              </th>
              <th scope="col" className="py-3.5 px-4 text-center">
                Assigned
              </th>
              <th scope="col" className="py-3.5 px-4 text-center">
                Available
              </th>
              <th scope="col" className="py-3.5 px-4 text-center">
                In Repair
              </th>
              <th scope="col" className="py-3.5 px-4 text-right pr-6">
                Hardware Units
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#EBF0EE]">
            {loading ? (
              Array.from({ length: 4 }).map((_, i) => (
                <tr key={i} className="animate-pulse">
                  <td className="py-4 pl-6 pr-2 text-center">
                    <div className="mx-auto h-4 w-6 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="h-4 w-40 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="h-4 w-28 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="mx-auto h-4 w-8 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="mx-auto h-4 w-8 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="mx-auto h-4 w-8 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="mx-auto h-4 w-8 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4 pr-6">
                    <div className="ml-auto h-4 w-16 rounded bg-slate-100" />
                  </td>
                </tr>
              ))
            ) : laptopConfigurations.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-16 text-center">
                  <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[#EBF7F2] text-[#03312E]">
                    <LaptopIcon className="h-7 w-7 text-[#03312E]" />
                  </div>
                  <h3 className="mt-4 text-base font-bold text-[#03312E]">
                    No laptop configurations found
                  </h3>
                  <p className="mt-1 text-xs text-[#64748B]">
                    Register new laptops using the "+ Add Accessory" button.
                  </p>
                  {isFiltered && (
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={handleClearFilters}
                      className="mt-4"
                    >
                      Clear All Filters
                    </Button>
                  )}
                </td>
              </tr>
            ) : (
              laptopConfigurations.map((item, index) => {
                const sNo = currentPage * pageSize + index + 1;
                const rowKey = `${item.brand}-${item.model}-${item.ram}-${item.ssd}`;
                const isExpanded = !!expandedKeys[rowKey];

                return (
                  <React.Fragment key={rowKey}>
                    <tr
                      onClick={() => toggleExpand(rowKey)}
                      className="group cursor-pointer transition-colors hover:bg-[#F0FDF8]"
                    >
                      <td className="py-4 pl-6 pr-2 text-center">
                        <span className="inline-block rounded-md bg-[#F1F5F9] px-2.5 py-1 font-mono text-xs font-bold text-[#1E293B]">
                          {sNo}
                        </span>
                      </td>
                      <td className="py-4 px-4">
                        <div className="font-semibold text-sm text-[#03312E] group-hover:text-[#1976D2] transition-colors">
                          {item.brand} {item.model}
                        </div>
                        <div className="mt-0.5 text-xs text-[#64748B]">
                          Hardware records tracked in devices database
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-1.5">
                          <span className="inline-flex items-center rounded-md border border-[#DCE5E1] bg-[#F8FAF9] px-2 py-0.5 text-xs font-medium text-[#03312E]">
                            {item.ram || "16 GB"} RAM
                          </span>
                          <span className="inline-flex items-center rounded-md border border-[#DCE5E1] bg-[#F8FAF9] px-2 py-0.5 text-xs font-medium text-[#03312E]">
                            {item.ssd || "512 GB"} SSD
                          </span>
                        </div>
                      </td>
                      <td className="py-4 px-4 text-center font-bold text-sm text-[#03312E]">
                        {item.total}
                      </td>
                      <td className="py-4 px-4 text-center">
                        <span className="inline-flex items-center rounded-full bg-[#E8F8F2] px-2.5 py-0.5 text-xs font-bold text-[#00C896]">
                          {item.assigned}
                        </span>
                      </td>
                      <td className="py-4 px-4 text-center">
                        <span className="inline-flex items-center rounded-full bg-[#EBF5FF] px-2.5 py-0.5 text-xs font-bold text-[#1976D2]">
                          {item.remaining}
                        </span>
                      </td>
                      <td className="py-4 px-4 text-center">
                        <span className="inline-flex items-center rounded-full bg-[#FFF4E5] px-2.5 py-0.5 text-xs font-bold text-[#F97316]">
                          {item.repair}
                        </span>
                      </td>
                      <td className="py-4 px-4 text-right pr-6">
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            toggleExpand(rowKey);
                          }}
                          className="inline-flex cursor-pointer items-center gap-1.5 rounded-lg border border-[#DCE5E1] bg-white px-3 py-1.5 text-xs font-semibold text-[#03312E] transition-colors hover:border-[#1976D2] hover:bg-[#E3F2FD] hover:text-[#1976D2]"
                        >
                          <span>{item.devices?.length || item.total} units</span>
                          <svg
                            className={`h-3.5 w-3.5 transition-transform duration-200 ${
                              isExpanded ? "rotate-180" : ""
                            }`}
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                          >
                            <polyline points="6 9 12 15 18 9" />
                          </svg>
                        </button>
                      </td>
                    </tr>

                    {/* Expandable Physical Units Drawer */}
                    {isExpanded && (
                      <tr className="bg-[#F8FAF9]">
                        <td colSpan={8} className="p-4 pl-10 pr-6">
                          <div className="rounded-xl border border-[#DCE5E1] bg-white p-4 shadow-2xs">
                            <div className="mb-3 flex items-center justify-between text-xs font-bold text-[#03312E]">
                              <span>
                                Registered Units: {item.brand} {item.model} ({item.ram} / {item.ssd})
                              </span>
                              <span className="text-[#64748B]">
                                {item.devices?.length || 0} physical assets in database
                              </span>
                            </div>

                            {item.devices && item.devices.length > 0 ? (
                              <div className="overflow-x-auto">
                                <table className="w-full text-left text-xs text-[#03312E]">
                                  <thead className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-[11px] font-bold uppercase tracking-wider text-[#5C7370]">
                                    <tr>
                                      <th className="py-2.5 px-3">Asset Tag</th>
                                      <th className="py-2.5 px-3">Serial Number</th>
                                      <th className="py-2.5 px-3">Warranty Expiry</th>
                                      <th className="py-2.5 px-3">Assignment Status</th>
                                      <th className="py-2.5 px-3">Hardware Health</th>
                                    </tr>
                                  </thead>
                                  <tbody className="divide-y divide-[#EBF0EE]">
                                    {item.devices.map((dev) => (
                                      <tr
                                        key={dev.deviceId || dev.serialNumber}
                                        className="hover:bg-[#F0FDF8]"
                                      >
                                        <td className="py-2.5 px-3 font-mono font-bold text-[#1976D2]">
                                          {dev.assetTag || `AST-${dev.deviceId}`}
                                        </td>
                                        <td className="py-2.5 px-3 font-mono text-[#03312E]">
                                          {dev.serialNumber}
                                        </td>
                                        <td className="py-2.5 px-3 text-[#64748B]">
                                          {dev.warrantyExpiryDate ? String(dev.warrantyExpiryDate) : "N/A"}
                                        </td>
                                        <td className="py-2.5 px-3">
                                          {dev.assignmentStatus === "ASSIGNED" ? (
                                            <span className="inline-flex items-center gap-1 rounded-full bg-[#E8F8F2] px-2.5 py-0.5 text-[11px] font-bold text-[#00C896]">
                                              ● Assigned {dev.assignedUserName ? `to ${dev.assignedUserName}` : ""}
                                            </span>
                                          ) : (
                                            <div className="flex items-center gap-2">
                                              <span className="inline-flex items-center gap-1 rounded-full bg-[#EBF5FF] px-2.5 py-0.5 text-[11px] font-bold text-[#1976D2]">
                                                ○ Available (Unassigned)
                                              </span>
                                              {onAssignLaptop && (
                                                <button
                                                  type="button"
                                                  onClick={() =>
                                                    onAssignLaptop({
                                                      ...dev,
                                                      brand: item.brand,
                                                      model: item.model,
                                                      ram: item.ram,
                                                      ssd: item.ssd,
                                                    })
                                                  }
                                                  className="cursor-pointer rounded-md bg-[#03312E] px-2 py-0.5 text-[11px] font-semibold text-white transition-colors hover:bg-[#02211F]"
                                                >
                                                  Assign
                                                </button>
                                              )}
                                            </div>
                                          )}
                                        </td>
                                        <td className="py-2.5 px-3">
                                          <span
                                            className={`inline-flex items-center rounded-md px-2 py-0.5 text-[11px] font-semibold ${
                                              dev.deviceStatus === "REPAIR"
                                                ? "border border-amber-200 bg-amber-50 text-amber-700"
                                                : "bg-[#F1F5F9] text-[#1E293B]"
                                            }`}
                                          >
                                            {dev.deviceStatus || "ACTIVE"}
                                          </span>
                                        </td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            ) : (
                              <div className="py-4 text-center text-xs text-[#64748B]">
                                No physical unit records found for this configuration.
                              </div>
                            )}
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination using Common Pagination */}
      <Pagination
        currentPage={currentPage + 1}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={pageSize}
        itemLabel="configurations"
        onPageChange={(pageOneIndexed) => onPageChange && onPageChange(pageOneIndexed - 1)}
      />
    </div>
  );
}
