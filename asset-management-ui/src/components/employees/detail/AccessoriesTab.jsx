import React, { useState, useMemo } from "react";
import { DataTable, StatusBadge, SearchBar } from "../../common";

export default function AccessoriesTab({ accessories = [] }) {
  const [searchQuery, setSearchQuery] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("ALL");

  const categories = useMemo(() => {
    return ["ALL", ...Array.from(new Set(accessories.map((a) => a.accessoryType).filter(Boolean)))];
  }, [accessories]);

  const filteredData = useMemo(() => {
    return accessories.filter((item) => {
      if (categoryFilter !== "ALL" && item.accessoryType !== categoryFilter) {
        return false;
      }
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase().trim();
        const searchable = [
          item.accessoryName,
          item.accessoryType,
          item.assetTag,
          item.serialNumber,
          item.status,
        ];
        return searchable.some((val) => (val || "").toLowerCase().includes(query));
      }
      return true;
    });
  }, [accessories, searchQuery, categoryFilter]);

  const columns = [
    {
      key: "accessoryName",
      header: "Accessory Name",
      render: (item) => (
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#EBF7F2] text-[#03312E]">
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
            </svg>
          </div>
          <div>
            <p className="font-bold text-sm text-[#03312E]">{item.accessoryName}</p>
            <div className="flex items-center gap-2 font-mono text-[11px] text-[#64748B]">
              <span>Tag: {item.assetTag || "N/A"}</span>
              <span>•</span>
              <span>SN: {item.serialNumber || "—"}</span>
            </div>
          </div>
        </div>
      ),
    },
    {
      key: "accessoryType",
      header: "Accessory Type",
      render: (item) => (
        <span className="inline-block rounded-md bg-[#F1F5F9] px-2.5 py-1 text-xs font-semibold text-[#334155]">
          {item.accessoryType}
        </span>
      ),
    },
    {
      key: "givenDate",
      header: "Given Date",
      render: (item) => (
        <div className="flex items-center gap-1.5 text-xs text-[#5C7370] font-mono">
          <svg className="h-3.5 w-3.5 text-[#8C9E9B]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="3" y="4" width="18" height="18" rx="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
          <span>{item.givenDate}</span>
        </div>
      ),
    },
    {
      key: "returnDate",
      header: "Return Date",
      render: (item) => {
        const isReturned = item.returnDate && !item.returnDate.includes("In Use") && item.returnDate !== "—";
        return (
          <span
            className={`font-mono text-xs font-semibold ${
              isReturned ? "text-slate-600" : "text-emerald-700"
            }`}
          >
            {item.returnDate || "— (Active in use)"}
          </span>
        );
      },
    },
    {
      key: "etc",
      header: "Status & Condition",
      render: (item) => (
        <div className="flex items-center gap-2">
          <StatusBadge status={item.status || "Assigned"} />
          {item.condition && (
            <span className="text-[11px] text-[#64748B] font-medium">
              ({item.condition})
            </span>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Header matching wireframe */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-black uppercase tracking-wider text-[#03312E]">
            ACCESSORIES
          </h2>
          <p className="text-xs text-[#5C7370]">
            Peripheral hardware items, monitors, docking hubs, and audio devices issued to this employee.
          </p>
        </div>

        {/* Search & Filter */}
        <div className="flex flex-wrap items-center gap-2.5">
          <div className="w-64">
            <SearchBar
              value={searchQuery}
              onChange={setSearchQuery}
              placeholder="Search accessories..."
            />
          </div>

          <div className="flex items-center gap-1 rounded-xl bg-white p-1 border border-[#DCE5E1]">
            {categories.map((cat) => (
              <button
                key={cat}
                type="button"
                onClick={() => setCategoryFilter(cat)}
                className={`cursor-pointer rounded-lg px-2.5 py-1 text-xs font-bold transition-all ${
                  categoryFilter === cat
                    ? "bg-[#1976D2] text-white shadow-2xs"
                    : "text-[#5C7370] hover:text-[#03312E]"
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Accessories Table */}
      <DataTable
        data={filteredData}
        columns={columns}
        keyField="id"
        showSerialNo={true}
        serialNoHeader="s.no"
        pageSize={5}
        emptyMessage="No accessories found"
        emptySubtitle="No peripheral hardware assets match your selected filter."
      />
    </div>
  );
}
