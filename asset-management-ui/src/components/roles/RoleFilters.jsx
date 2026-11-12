import React from "react";
import { SearchBar } from "../common";

export default function RoleFilters({
  searchQuery = "",
  onSearchChange,
  totalRoles = 0,
}) {
  return (
    <div className="flex flex-col gap-3 bg-white p-4 rounded-2xl border border-[#DCE5E1] shadow-xs">
      <div className="w-full">
        <SearchBar
          value={searchQuery}
          onChange={onSearchChange}
          placeholder="Search roles by name or description..."
          variant="long"
        />
      </div>
    </div>
  );
}

