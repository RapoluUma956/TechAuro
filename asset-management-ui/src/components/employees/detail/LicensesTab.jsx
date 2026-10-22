import React, { useState, useMemo } from "react";
import { DataTable, StatusBadge, SearchBar } from "../../common";

export default function LicensesTab({ licenses = [] }) {
  const [searchQuery, setSearchQuery] = useState("");
  const [copiedKey, setCopiedKey] = useState("");

  const filteredData = useMemo(() => {
    if (!searchQuery.trim()) return licenses;
    const query = searchQuery.toLowerCase().trim();
    return licenses.filter((lic) => {
      const fields = [
        lic.softwareName,
        lic.vendor,
        lic.licenseType,
        lic.licenseKey,
        lic.status,
      ];
      return fields.some((f) => (f || "").toLowerCase().includes(query));
    });
  }, [licenses, searchQuery]);

  const handleCopy = (id, key) => {
    navigator.clipboard.writeText(key);
    setCopiedKey(id);
    setTimeout(() => setCopiedKey(""), 2000);
  };

  const columns = [
    {
      key: "softwareName",
      header: "Software & Subscription",
      render: (item) => (
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#EBF7F2] text-[#03312E]">
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="m21 2-2 2m-1.5 1.5L14 9a5 5 0 1 0 4 4l3.5-3.5a1 1 0 0 0 0-1.41l-2-2a1 1 0 0 0-1.41 0l-2.09 2.09" />
              <circle cx="7.5" cy="16.5" r="1.5" />
            </svg>
          </div>
          <div>
            <p className="font-bold text-sm text-[#03312E]">{item.softwareName}</p>
            {item.vendor && (
              <span className="text-xs text-[#5C7370]">{item.vendor}</span>
            )}
          </div>
        </div>
      ),
    },
    {
      key: "licenseKey",
      header: "License Key / Seat Tag",
      render: (item) => (
        <div className="flex items-center gap-1.5">
          <span className="font-mono text-xs font-semibold text-[#1E293B] bg-[#F1F5F9] px-2 py-0.5 rounded">
            {item.licenseKey}
          </span>
          <button
            type="button"
            onClick={() => handleCopy(item.id, item.licenseKey)}
            className="text-[#94A3B8] hover:text-[#1976D2] cursor-pointer text-xs"
            title="Copy Key"
          >
            {copiedKey === item.id ? "✓" : "📋"}
          </button>
        </div>
      ),
    },
    {
      key: "licenseType",
      header: "License Type",
      render: (item) => (
        <span className="text-xs font-medium text-[#334155]">
          {item.licenseType}
        </span>
      ),
    },
    {
      key: "assignedDate",
      header: "Assigned Date",
      render: (item) => (
        <span className="font-mono text-xs text-[#5C7370]">
          {item.assignedDate}
        </span>
      ),
    },
    {
      key: "expiryDate",
      header: "Expiry Date",
      render: (item) => (
        <span className="font-mono text-xs font-bold text-emerald-700">
          {item.expiryDate}
        </span>
      ),
    },
    {
      key: "status",
      header: "Status & Tier",
      render: (item) => (
        <div className="flex items-center gap-2">
          <StatusBadge status={item.status || "Active"} />
          {item.tier && (
            <span className="text-[11px] font-medium text-[#64748B]">
              {item.tier}
            </span>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-[#03312E]">
            Assigned Software Licences
          </h2>
          <p className="text-xs text-[#5C7370]">
            SaaS subscriptions, developer tooling keys, and enterprise commercial seats provisioned to this employee.
          </p>
        </div>

        {/* Search */}
        <div className="w-full sm:w-72">
          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Search license or vendor..."
          />
        </div>
      </div>

      {/* Licenses Table */}
      <DataTable
        data={filteredData}
        columns={columns}
        keyField="id"
        showSerialNo={true}
        serialNoHeader="s.no"
        pageSize={5}
        emptyMessage="No software licenses found"
        emptySubtitle="No enterprise software seats or subscriptions have been allocated to this employee."
      />
    </div>
  );
}
