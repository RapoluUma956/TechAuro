import React from "react";
import { SearchBar } from "../common";

export default function RoleFilters({
  searchQuery = "",
  onSearchChange,
  totalRoles = 0,
}) {
  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-[#DCE5E1] shadow-xs">
      <div className="w-full sm:w-80">
        <SearchBar
          value={searchQuery}
          onChange={onSearchChange}
          placeholder="Search roles by name or description..."
        />
      </div>
      <div className="flex items-center gap-2 text-xs font-medium text-[#64748B]">
        <span className="inline-block h-2 w-2 rounded-full bg-emerald-500" />
        <span>Database-Driven Dynamic RBAC</span>
      </div>
    </div>
  );
}

