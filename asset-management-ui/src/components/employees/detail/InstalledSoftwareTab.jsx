import React, { useState, useMemo } from "react";
import { DataTable, StatusBadge, SearchBar } from "../../common";

export default function InstalledSoftwareTab({ installedSoftwares = [] }) {
  const [searchQuery, setSearchQuery] = useState("");

  const filteredData = useMemo(() => {
    if (!searchQuery.trim()) return installedSoftwares;
    const query = searchQuery.toLowerCase().trim();
    return installedSoftwares.filter((app) => {
      const fields = [
        app.softwareName,
        app.publisher,
        app.version,
        app.installPath,
        app.category,
        app.status,
      ];
      return fields.some((val) => (val || "").toLowerCase().includes(query));
    });
  }, [installedSoftwares, searchQuery]);

  const columns = [
    {
      key: "softwareName",
      header: "softwareName",
      render: (item) => (
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="m9 8 6 4-6 4Z" />
            </svg>
          </div>
          <div>
            <p className="font-bold text-sm text-[#03312E]">{item.softwareName}</p>
            {item.category && (
              <span className="text-[11px] text-[#64748B]">{item.category}</span>
            )}
          </div>
        </div>
      ),
    },
    {
      key: "version",
      header: "version",
      render: (item) => (
        <span className="font-mono text-xs font-bold text-[#1E293B] bg-[#F1F5F9] px-2 py-0.5 rounded">
          {item.version}
        </span>
      ),
    },
    {
      key: "publisher",
      header: "Publisher",
      render: (item) => (
        <span className="text-xs font-medium text-[#334155]">
          {item.publisher || "—"}
        </span>
      ),
    },
    {
      key: "installDate",
      header: "installDate",
      render: (item) => (
        <div className="flex items-center gap-1.5 text-xs text-[#5C7370] font-mono">
          <svg className="h-3.5 w-3.5 text-[#8C9E9B]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="3" y="4" width="18" height="18" rx="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
          <span>{item.installDate}</span>
        </div>
      ),
    },
    {
      key: "installPath",
      header: "installPath",
      render: (item) => (
        <span
          className="font-mono text-xs text-[#64748B] truncate max-w-xs block"
          title={item.installPath}
        >
          {item.installPath || "—"}
        </span>
      ),
    },
    {
      key: "etc",
      header: "Architecture & Status",
      render: (item) => (
        <div className="flex items-center gap-2">
          {item.architecture && (
            <span className="rounded bg-[#EBF7F2] px-1.5 py-0.5 font-mono text-[11px] font-bold text-[#03312E]">
              {item.architecture}
            </span>
          )}
          {item.size && (
            <span className="text-[11px] text-[#64748B]">
              {item.size}
            </span>
          )}
          <StatusBadge status={item.status || "Up to date"} />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Header matching wireframe */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-[#03312E]">
            Installed Softwares
          </h2>
          <p className="text-xs text-[#5C7370]">
            Binaries, system dependencies, IDEs, and business applications discovered on this host.
          </p>
        </div>

        {/* Search Input */}
        <div className="w-full sm:w-72">
          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Filter installed apps..."
          />
        </div>
      </div>

      {/* Installed Softwares Table */}
      <DataTable
        data={filteredData}
        columns={columns}
        keyField="id"
        showSerialNo={true}
        serialNoHeader="s.no"
        pageSize={6}
        emptyMessage="No software applications found"
        emptySubtitle="Try searching with a different application name or vendor."
      />
    </div>
  );
}
