import React from "react";
import { SearchBar, Dropdown, Button } from "../common";
import { RotateCcwIcon } from "./EmployeeIcons";

export default function EmployeeFilters({
  searchQuery,
  onSearchChange,
  statusFilter,
  onStatusChange,
  roleFilter,
  onRoleChange,
  roles = [],
  onClearFilters,
  totalCount = 0,
  filteredCount = 0,
}) {
  const isFiltered = Boolean(
    searchQuery.trim() || statusFilter !== "ALL" || roleFilter !== "ALL"
  );

  const statusOptions = [
    { value: "Active", label: "Active" },
    { value: "Repair", label: "In Repair" },
    { value: "Inactive", label: "Inactive" },
  ];

  return (
    <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 shadow-sm">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        {/* Search Bar using Common SearchBar */}
        <div className="flex-1">
          <SearchBar
            value={searchQuery}
            onChange={onSearchChange}
            placeholder="Search by employee name, ID, role, device name, or device ID..."
            variant="long"
          />
        </div>

        {/* Filters Group */}
        <div className="flex flex-wrap items-center gap-2.5">
          {/* Status Filter */}
          <div className="min-w-[150px]">
            <Dropdown
              value={statusFilter}
              onChange={onStatusChange}
              placeholder="All Statuses"
              placeholderValue="ALL"
              options={statusOptions}
            />
          </div>

          {/* Role Filter */}
          <div className="min-w-[170px]">
            <Dropdown
              value={roleFilter}
              onChange={onRoleChange}
              placeholder="All Roles"
              placeholderValue="ALL"
              options={roles}
            />
          </div>

          {/* Clear Filters Button using Common Button */}
          {isFiltered && (
            <Button
              variant="outline"
              size="md"
              icon={<RotateCcwIcon className="h-4 w-4" />}
              onClick={onClearFilters}
              className="text-[#5C7370] hover:text-[#EF4444] hover:border-[#EF4444] hover:bg-[#FEECEB]"
              title="Reset all filters and search query"
            >
              Clear
            </Button>
          )}
        </div>
      </div>

      {/* Result Counter when filtered */}
      {isFiltered && (
        <div className="mt-3 flex items-center justify-between border-t border-[#EBF0EE] pt-2 text-xs text-[#64748B]">
          <span>
            Showing <strong className="text-[#03312E]">{filteredCount}</strong> of{" "}
            <strong className="text-[#03312E]">{totalCount}</strong> matching employees
          </span>
          <button
            type="button"
            onClick={onClearFilters}
            className="text-xs font-semibold text-[#1976D2] hover:underline cursor-pointer"
          >
            Reset Filters
          </button>
        </div>
      )}
    </div>
  );
}
