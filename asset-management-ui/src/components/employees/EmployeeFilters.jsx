import React from "react";
import { SearchBar } from "../common";

export default function EmployeeFilters({
  searchQuery,
  onSearchChange,
  totalCount = 0,
}) {
  return (
    <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 shadow-sm">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        {/* Search Bar using Common SearchBar */}
        <div className="w-full sm:max-w-xl">
          <SearchBar
            value={searchQuery}
            onChange={onSearchChange}
            placeholder="Search by employee name, ID, role, device name, or serial number..."
            variant="responsive"
            size="md"
          />
        </div>

        {totalCount > 0 && (
          <div className="text-xs font-semibold text-[#64748B]">
            Total: <span className="font-bold text-[#03312E]">{totalCount}</span> employees
          </div>
        )}
      </div>
    </div>
  );
}
