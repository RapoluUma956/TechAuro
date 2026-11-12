import React, { useState, useMemo } from "react";
import { DataTable, StatusBadge, SearchBar } from "../../common";

export default function AccessoriesTab({ accessories = [] }) {
  const [searchQuery, setSearchQuery] = useState("");

  const filteredData = useMemo(() => {
    if (!searchQuery.trim()) return accessories;
    const query = searchQuery.toLowerCase().trim();
    return accessories.filter((item) => {
      const searchable = [
        item.accessoryName,
        item.accessoryType,
        item.assetTag,
        item.serialNumber,
        item.status,
      ];
      return searchable.some((val) => (val || "").toLowerCase().includes(query));
    });
  }, [accessories, searchQuery]);

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
      header: "Type",
      render: (item) => (
        <span className="inline-block rounded-md bg-[#F1F5F9] px-2.5 py-1 text-xs font-semibold text-[#1E293B]">
          {item.accessoryType || "Accessory"}
        </span>
      ),
    },
    {
      key: "assignedDate",
      header: "Assigned Date",
      render: (item) => (
        <span className="text-xs font-medium text-[#5C7370]">
          {item.assignedDate
            ? new Date(item.assignedDate).toLocaleDateString("en-GB", {
                day: "2-digit",
                month: "short",
                year: "numeric",
              })
            : "—"}
        </span>
      ),
    },
    {
      key: "status",
      header: "Status",
      align: "center",
      render: (item) => (
        <StatusBadge
          status={item.status || "Assigned"}
          variant="pill"
        />
      ),
    },
    {
      key: "returnStatus",
      header: "Condition",
      render: (item) => (
        <div className="flex items-center gap-1.5 text-xs text-[#5C7370]">
          <span>{item.isReturned ? "Returned" : "In Possession"}</span>
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
      <div className="flex flex-col gap-3">
        <div>
          <h2 className="text-xl font-black uppercase tracking-wider text-[#03312E]">
            ACCESSORIES
          </h2>
          <p className="text-xs text-[#5C7370]">
            Peripheral hardware items, monitors, docking hubs, and audio devices issued to this employee.
          </p>
        </div>

        {/* Full Length Search */}
        <div className="w-full">
          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Search accessories by name, type, serial number, or tag..."
            variant="long"
          />
        </div>
      </div>

      {/* Accessories Table with sorting on all columns */}
      <DataTable
        data={filteredData}
        columns={columns}
        keyField="id"
        showSerialNo={true}
        serialNoHeader="#"
        pageSize={5}
        emptyMessage="No accessories found"
        emptySubtitle="No peripheral hardware assets match your search."
      />
    </div>
  );
}
