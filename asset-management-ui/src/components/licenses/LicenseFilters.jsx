import React from "react";
import { SearchIcon, ResetIcon } from "./LicenseIcons";

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
  return (
    <div className="flex flex-col gap-3 rounded-2xl border border-[#DCE5E1] bg-white p-4 shadow-2xs lg:flex-row lg:items-end">
      {/* Search Input */}
      <div className="flex-1 min-w-[240px]">
        <label className="mb-1 block text-xs font-semibold text-[#03312E]">
          Search
        </label>
        <div className="relative">
          <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-[#64748B]">
            <SearchIcon className="h-4 w-4" />
          </div>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search by license or software name..."
            className="w-full rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] py-2 pl-9 pr-3 text-sm text-[#03312E] transition-colors placeholder:text-[#94A3B8] focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => onSearchChange("")}
              className="absolute inset-y-0 right-0 flex items-center pr-3 text-xs text-[#64748B] hover:text-[#03312E]"
            >
              ✕
            </button>
          )}
        </div>
      </div>

      {/* Software Filter */}
      <div className="w-full sm:w-44">
        <label className="mb-1 block text-xs font-semibold text-[#03312E]">
          Software
        </label>
        <select
          value={selectedSoftware}
          onChange={(e) => onSoftwareChange(e.target.value)}
          className="w-full appearance-none rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] py-2 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
        >
          <option value="All">All</option>
          {softwareOptions.map((sw) => (
            <option key={sw} value={sw}>
              {sw}
            </option>
          ))}
        </select>
      </div>

      {/* Publisher Filter */}
      <div className="w-full sm:w-44">
        <label className="mb-1 block text-xs font-semibold text-[#03312E]">
          Publisher
        </label>
        <select
          value={selectedPublisher}
          onChange={(e) => onPublisherChange(e.target.value)}
          className="w-full appearance-none rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] py-2 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
        >
          <option value="All">All</option>
          {publisherOptions.map((pub) => (
            <option key={pub} value={pub}>
              {pub}
            </option>
          ))}
        </select>
      </div>

      {/* Status Filter */}
      <div className="w-full sm:w-36">
        <label className="mb-1 block text-xs font-semibold text-[#03312E]">
          Status
        </label>
        <select
          value={selectedStatus}
          onChange={(e) => onStatusChange(e.target.value)}
          className="w-full appearance-none rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] py-2 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
        >
          <option value="All">All</option>
          <option value="ACTIVE">Active</option>
          <option value="EXPIRING_SOON">Expiring Soon</option>
          <option value="EXPIRED">Expired</option>
          <option value="SUSPENDED">Suspended</option>
          <option value="INACTIVE">Inactive</option>
        </select>
      </div>

      {/* Expiry Filter */}
      <div className="w-full sm:w-36">
        <label className="mb-1 block text-xs font-semibold text-[#03312E]">
          Expiry
        </label>
        <select
          value={selectedExpiry}
          onChange={(e) => onExpiryChange(e.target.value)}
          className="w-full appearance-none rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] py-2 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
        >
          <option value="All">All</option>
          <option value="EXPIRING_SOON">Within 30 days</option>
          <option value="ACTIVE">Active</option>
          <option value="EXPIRED">Expired</option>
        </select>
      </div>

      {/* Reset Button */}
      <div className="shrink-0">
        <button
          type="button"
          onClick={onReset}
          className="flex h-[38px] w-full items-center justify-center gap-1.5 rounded-xl border border-[#DCE5E1] bg-white px-4 text-sm font-semibold text-[#5C7370] shadow-2xs transition-all hover:border-[#1976D2] hover:bg-[#F8FAF9] hover:text-[#1976D2] cursor-pointer sm:w-auto"
        >
          <ResetIcon className="h-3.5 w-3.5" />
          <span>Reset</span>
        </button>
      </div>
    </div>
  );
}
