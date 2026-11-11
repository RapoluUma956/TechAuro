import React, { useState } from "react";
import { DataTable, SearchBar, Button } from "../common";
import {
  RotateCcwIcon,
  HistoryIcon,
  PencilIcon,
  TrashIcon,
  UserPlusIcon,
  ChevronDownIcon,
} from "../common/Icons";

export default function LaptopTable({
  laptopConfigurations = [],
  loading = false,
  totalElements = 0,
  currentPage = 0, // 0-indexed for backend API
  pageSize = 10,
  totalPages = 1,
  onPageChange,
  searchQuery = "",
  onSearchChange,
  selectedBrand = "All",
  onBrandChange,
  onRefresh,
  onAssignLaptop,
  onViewDeviceHistory,
  onEditLaptop,
  onDeleteLaptop,
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

  const preparedData = (laptopConfigurations || []).map((item, idx) => ({
    ...item,
    configKey: `${item.brand}-${item.model}-${item.ram}-${item.ssd}` || idx,
  }));

  const renderExpandedUnits = (item) => {
    return (
      <div className="rounded-xl border border-[#DCE5E1] bg-white p-4 shadow-2xs">
        <div className="mb-3 flex items-center justify-between text-xs font-bold text-[#03312E]">
          <span>
            Registered Units: {item.brand} {item.model} ({item.ram} / {item.ssd})
          </span>
          <span className="font-medium text-[#64748B]">
            {item.devices?.length || 0} physical assets in database
          </span>
        </div>

        {item.devices && item.devices.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-[#03312E]">
              <thead className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-xs font-bold uppercase tracking-wider text-[#64748B]">
                <tr>
                  <th className="py-3 px-3">Asset Tag</th>
                  <th className="py-3 px-3">Serial Number</th>
                  <th className="py-3 px-3">Warranty Expiry</th>
                  <th className="py-3 px-3">Assignment Status</th>
                  <th className="py-3 px-3">Hardware Health</th>
                  <th className="py-3 px-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#EBF0EE]">
                {item.devices.map((dev) => (
                  <tr
                    key={dev.deviceId || dev.serialNumber}
                    className="hover:bg-[#F8FAF9] transition-colors"
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
                              className="rounded-lg p-1 text-[#64748B] hover:bg-emerald-50 hover:text-[#00C896] cursor-pointer"
                              title="Assign to Employee"
                            >
                              <UserPlusIcon className="h-4 w-4" />
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
                    <td className="py-2.5 px-3 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            onViewDeviceHistory &&
                              onViewDeviceHistory({
                                ...dev,
                                brand: item.brand,
                                model: item.model,
                                ram: item.ram,
                                ssd: item.ssd,
                              });
                          }}
                          className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-sky-50 hover:text-[#1976D2] cursor-pointer"
                          title="View Assignment History"
                        >
                          <HistoryIcon className="h-4 w-4" />
                        </button>

                        {onEditLaptop && (
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              onEditLaptop({
                                ...dev,
                                brand: item.brand,
                                model: item.model,
                              });
                            }}
                            className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-yellow-50 hover:text-[#EAB308] cursor-pointer"
                            title="Edit Laptop Details"
                          >
                            <PencilIcon className="h-4 w-4" />
                          </button>
                        )}

                        {onDeleteLaptop && (
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              onDeleteLaptop({
                                ...dev,
                                brand: item.brand,
                                model: item.model,
                              });
                            }}
                            className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-rose-50 hover:text-[#EF4444] cursor-pointer"
                            title="Delete Laptop from Registry"
                          >
                            <TrashIcon className="h-4 w-4" />
                          </button>
                        )}
                      </div>
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
    );
  };

  const columns = [
    {
      key: "model",
      header: "Configuration & Model",
      render: (item) => (
        <div>
          <div className="font-bold text-sm text-[#03312E] group-hover:text-[#1976D2] transition-colors">
            {item.brand} {item.model}
          </div>
          <div className="mt-0.5 text-xs font-medium text-[#64748B]">
            Hardware records tracked in devices database
          </div>
        </div>
      ),
    },
    {
      key: "specs",
      header: "RAM / Storage",
      render: (item) => (
        <div className="flex items-center gap-1.5">
          <span className="inline-flex items-center rounded-md border border-[#DCE5E1] bg-[#F8FAF9] px-2 py-0.5 text-xs font-medium text-[#03312E]">
            {item.ram || "16 GB"} RAM
          </span>
          <span className="inline-flex items-center rounded-md border border-[#DCE5E1] bg-[#F8FAF9] px-2 py-0.5 text-xs font-medium text-[#03312E]">
            {item.ssd || "512 GB"} SSD
          </span>
        </div>
      ),
    },
    {
      key: "total",
      header: "Total Stock",
      align: "center",
      render: (item) => (
        <span className="font-semibold text-sm text-[#03312E]">
          {item.total ?? 0}
        </span>
      ),
    },
    {
      key: "assigned",
      header: "Assigned",
      align: "center",
      render: (item) => (
        <span className="font-semibold text-sm text-[#00C896]">
          {item.assigned ?? 0}
        </span>
      ),
    },
    {
      key: "remaining",
      header: "Available",
      align: "center",
      render: (item) => (
        <span className="font-semibold text-sm text-[#1976D2]">
          {item.remaining ?? 0}
        </span>
      ),
    },
    {
      key: "repair",
      header: "In Repair",
      align: "center",
      render: (item) => (
        <span className="font-semibold text-sm text-[#F97316]">
          {item.repair ?? 0}
        </span>
      ),
    },
    {
      key: "actions",
      header: "Hardware Units",
      align: "right",
      render: (item) => {
        const rowKey = item.configKey;
        const isExpanded = !!expandedKeys[rowKey];
        const unitCount = item.devices?.length || item.total || 0;

        return (
          <div
            className="flex items-center justify-end gap-2"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              type="button"
              onClick={() => {
                const targetDev = item.devices?.[0];
                if (targetDev && onViewDeviceHistory) {
                  onViewDeviceHistory({
                    ...targetDev,
                    brand: item.brand,
                    model: item.model,
                    ram: item.ram,
                    ssd: item.ssd,
                  });
                } else {
                  toggleExpand(rowKey);
                }
              }}
              className="inline-flex cursor-pointer items-center gap-1.5 rounded-lg border border-[#BBDEFB] bg-[#E3F2FD] px-2.5 py-1.5 text-xs font-bold text-[#1976D2] transition-colors hover:bg-[#1976D2] hover:text-white shadow-2xs"
              title="View assignment history for this laptop model"
            >
              <HistoryIcon className="h-3.5 w-3.5" />
              <span>Device History</span>
            </button>

            <button
              type="button"
              onClick={() => toggleExpand(rowKey)}
              className={`inline-flex cursor-pointer items-center gap-1.5 rounded-lg border px-3 py-1.5 text-xs font-semibold transition-all ${
                isExpanded
                  ? "border-[#1976D2] bg-[#E3F2FD] text-[#1976D2]"
                  : "border-[#DCE5E1] bg-white text-[#03312E] hover:border-[#1976D2] hover:bg-[#F8FAF9] hover:text-[#1976D2]"
              }`}
            >
              <span>{unitCount} units</span>
              <ChevronDownIcon
                className={`h-3.5 w-3.5 transition-transform duration-200 ${
                  isExpanded ? "rotate-180" : ""
                }`}
              />
            </button>
          </div>
        );
      },
    },
  ];

  return (
    <div className="space-y-4">
      {/* Top Filter Card matching AccessoryTable */}
      <div className="rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-sm">
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-lg font-bold text-[#03312E] sm:text-xl">
              Laptop Inventory
            </h2>
            <p className="text-xs font-medium text-[#64748B]">
              Individually serialized hardware records grouped by configuration
            </p>
          </div>

          <div className="flex items-center gap-2">
            {isFiltered && (
              <Button
                variant="outline"
                size="sm"
                icon={<RotateCcwIcon className="h-3.5 w-3.5" />}
                onClick={handleClearFilters}
                className="self-start text-[#5C7370] hover:border-[#EF4444] hover:bg-[#FEECEB] hover:text-[#EF4444] sm:self-auto"
              >
                Reset Filters
              </Button>
            )}
            {onRefresh && (
              <Button
                variant="outline"
                size="sm"
                onClick={onRefresh}
                icon={<RotateCcwIcon className="h-3.5 w-3.5" />}
              >
                Refresh
              </Button>
            )}
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

      {/* Reusable DataTable matching AccessoryTable & LicenseTable styling */}
      <DataTable
        data={preparedData}
        columns={columns}
        keyField="configKey"
        showSerialNo={true}
        serialNoHeader="#"
        currentPage={currentPage + 1}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={pageSize}
        itemLabel="configurations"
        onPageChange={(newPage) => onPageChange && onPageChange(newPage - 1)}
        loading={loading}
        emptyMessage="No laptop configurations found"
        emptySubtitle="Try adjusting your search query or selecting another brand filter."
        emptyAction={
          isFiltered ? (
            <Button variant="primary" size="sm" onClick={handleClearFilters}>
              Clear All Filters
            </Button>
          ) : null
        }
        renderExpandedRow={renderExpandedUnits}
        expandedRowKeys={expandedKeys}
      />
    </div>
  );
}
