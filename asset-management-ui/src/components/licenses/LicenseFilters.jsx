import React from "react";
import { SearchBar, Dropdown, Button } from "../common";
import { RotateCcwIcon } from "../common/Icons";

export default function LicenseFilters({
  searchQuery,
  onSearchChange,
  selectedSoftware,
  onSoftwareChange,
  selectedPublisher,
  onPublisherChange,
  selectedStatus,
  onStatusChange,
  selectedExpiry,
  onExpiryChange,
  softwareOptions = [],
  publisherOptions = [],
  onReset,
}) {
  const statusOptions = [
    { value: "All", label: "All Statuses" },
    { value: "ACTIVE", label: "Active" },
    { value: "EXPIRING_SOON", label: "Expiring Soon" },
    { value: "EXPIRED", label: "Expired" },
    { value: "SUSPENDED", label: "Suspended" },
    { value: "INACTIVE", label: "Inactive" },
  ];

  const expiryOptions = [
    { value: "All", label: "All Expiries" },
    { value: "EXPIRING_SOON", label: "Within 30 Days" },
    { value: "ACTIVE", label: "Active" },
    { value: "EXPIRED", label: "Expired" },
  ];

  return (
    <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 shadow-sm">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        {/* Search Bar */}
        <div className="flex-1 min-w-[240px]">
          <label className="mb-1.5 block text-xs font-semibold text-[#03312E]">
            Search
          </label>
          <SearchBar
            value={searchQuery}
            onChange={onSearchChange}
            placeholder="Search by license or software name..."
            variant="long"
          />
        </div>

        {/* Filters Group */}
        <div className="flex flex-wrap items-end gap-2.5">
          {/* Software Filter */}
          <div className="w-full sm:w-44">
            <Dropdown
              label="Software"
              value={selectedSoftware}
              onChange={onSoftwareChange}
              options={["All", ...softwareOptions]}
              placeholder="All Software"
              placeholderValue="All"
            />
          </div>

          {/* Publisher Filter */}
          <div className="w-full sm:w-44">
            <Dropdown
              label="Publisher"
              value={selectedPublisher}
              onChange={onPublisherChange}
              options={["All", ...publisherOptions]}
              placeholder="All Publishers"
              placeholderValue="All"
            />
          </div>

          {/* Status Filter */}
          <div className="w-full sm:w-36">
            <Dropdown
              label="Status"
              value={selectedStatus}
              onChange={onStatusChange}
              options={statusOptions}
              placeholder="All Statuses"
              placeholderValue="All"
            />
          </div>

          {/* Expiry Filter */}
          <div className="w-full sm:w-36">
            <Dropdown
              label="Expiry"
              value={selectedExpiry}
              onChange={onExpiryChange}
              options={expiryOptions}
              placeholder="All Expiries"
              placeholderValue="All"
            />
          </div>

          {/* Reset Button */}
          {onReset && (
            <div className="shrink-0 pb-0.5">
              <Button
                variant="outline"
                size="md"
                onClick={onReset}
                icon={<RotateCcwIcon className="h-4 w-4" />}
              >
                Reset
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
