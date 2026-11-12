import React from "react";
import { SearchBar } from "../common";

export default function EmployeeFilters({
  searchQuery,
  onSearchChange,
  totalCount = 0,
}) {
  return (
    <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 shadow-sm">
      <div className="flex flex-col gap-3">
        {/* Full Length Search Bar */}
        <div className="w-full">
          <SearchBar
            value={searchQuery}
            onChange={onSearchChange}
            placeholder="Search by employee name, ID, role, device name, or serial number..."
            variant="long"
            size="md"
          />
        </div>
      </div>
    </div>
  );
}
