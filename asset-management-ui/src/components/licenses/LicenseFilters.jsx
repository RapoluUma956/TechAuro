import React from "react";
import { SearchBar } from "../common";

export default function LicenseFilters({
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
            placeholder="Search by license, software name, publisher, or license key..."
            variant="long"
          />
        </div>
      </div>
    </div>
  );
}
