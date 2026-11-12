import React, { useMemo } from "react";
import { DataTable, SearchBar, Button } from "../common";
import {
  PlusIcon,
  PencilIcon,
  TrashIcon,
  RotateCcwIcon,
  UserPlusIcon,
} from "../common/Icons";

export default function AccessoryTable({
  accessories = [],
  loading = false,
  totalElements = 0,
  currentPage = 0, // 0-indexed for backend API
  pageSize = 10,
  totalPages = 1,
  onPageChange,
  searchQuery = "",
  onSearchChange,
  onEditAccessory,
  onDeleteAccessory,
  onQuickAddQuantity,
  onAssignAccessory,
}) {
  // Deduplicate: merge rows with the same accessoryName+category+brand+model into one,
  // summing all quantity metrics. The backend may return separate records for the same
  // product if it was added in multiple batches.
  const deduplicatedAccessories = useMemo(() => {
    const map = new Map();
    for (const item of accessories || []) {
      const key = `${item.accessoryName || item.productName}__${item.category}__${item.brand}__${item.model}`;
      if (map.has(key)) {
        const ex = map.get(key);
        ex.totalQuantity = (ex.totalQuantity || 0) + (item.totalQuantity || 0);
        ex.assignedQuantity = (ex.assignedQuantity || 0) + (item.assignedQuantity || 0);
        ex.availableQuantity = (ex.availableQuantity || 0) + (item.availableQuantity || 0);
        ex.repairQuantity = (ex.repairQuantity || 0) + (item.repairQuantity || item.damagedQuantity || 0);
        ex.damagedQuantity = ex.repairQuantity;
      } else {
        map.set(key, {
          ...item,
          repairQuantity: item.repairQuantity ?? item.damagedQuantity ?? 0,
        });
      }
    }
    return Array.from(map.values());
  }, [accessories]);

  const columns = [
    {
      key: "accessoryName",
      header: "Accessory Name",
      render: (item) => (
        <div>
          <div className="font-bold text-sm text-[#03312E] group-hover:text-[#1976D2] transition-colors">
            {item.accessoryName || item.productName || "Unnamed Accessory"}
          </div>
          <div className="mt-0.5 text-xs font-medium text-[#64748B]">
            {[item.brand, item.model, item.subCategory].filter(Boolean).join(" · ") || "Standard"}
          </div>
        </div>
      ),
    },
    {
      key: "category",
      header: "Category",
      render: (item) => (
        <span className="inline-block rounded-md bg-[#F1F5F9] px-2.5 py-1 text-xs font-semibold text-[#1E293B]">
          {item.category || "General"}
        </span>
      ),
    },
    {
      key: "totalQuantity",
      header: "Total Stock",
      align: "center",
      render: (item) => (
        <span className="font-semibold text-sm text-[#03312E]">
          {item.totalQuantity ?? 0}
        </span>
      ),
    },
    {
      key: "assignedQuantity",
      header: "Assigned",
      align: "center",
      render: (item) => (
        <span className="font-semibold text-sm text-[#00C896]">
          {item.assignedQuantity ?? 0}
        </span>
      ),
    },
    {
      key: "availableQuantity",
      header: "Available",
      align: "center",
      render: (item) => (
        <span className="font-semibold text-sm text-[#1976D2]">
          {item.availableQuantity ?? 0}
        </span>
      ),
    },
    {
      key: "repairQuantity",
      header: "In Repair",
      align: "center",
      render: (item) => (
        <span className="font-semibold text-sm text-[#F97316]">
          {item.repairQuantity ?? item.damagedQuantity ?? 0}
        </span>
      ),
    },
    ...(onAssignAccessory || onQuickAddQuantity || onEditAccessory || onDeleteAccessory
      ? [
          {
            key: "actions",
            header: "Actions",
            align: "center",
            render: (item) => {
              const unassigned = item.availableQuantity ?? 0;

              return (
                <div
                  className="flex items-center justify-center gap-1"
                  onClick={(e) => e.stopPropagation()}
                >
                  {onAssignAccessory && (
                    <button
                      type="button"
                      onClick={() => onAssignAccessory(item)}
                      title={unassigned > 0 ? "Assign to Employee" : "No Available Units"}
                      disabled={unassigned <= 0}
                      aria-label={`Assign ${item.accessoryName || "Accessory"}`}
                      className={`rounded-lg p-1.5 transition-colors ${
                        unassigned <= 0
                          ? "cursor-not-allowed text-slate-300 opacity-40"
                          : "text-[#64748B] hover:bg-emerald-50 hover:text-[#00C896] cursor-pointer"
                      }`}
                    >
                      <UserPlusIcon className="h-4 w-4" />
                    </button>
                  )}

                  {onQuickAddQuantity && (
                    <button
                      type="button"
                      onClick={() => onQuickAddQuantity(item)}
                      title="Add Stock Units"
                      aria-label={`Add stock to ${item.accessoryName || "Accessory"}`}
                      className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-sky-50 hover:text-[#1976D2] cursor-pointer"
                    >
                      <PlusIcon className="h-4 w-4" />
                    </button>
                  )}

                  {onEditAccessory && (
                    <button
                      type="button"
                      onClick={() => onEditAccessory(item)}
                      title="Edit Accessory"
                      aria-label={`Edit ${item.accessoryName || "Accessory"}`}
                      className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-yellow-50 hover:text-[#EAB308] cursor-pointer"
                    >
                      <PencilIcon className="h-4 w-4" />
                    </button>
                  )}

                  {onDeleteAccessory && (
                    <button
                      type="button"
                      onClick={() => onDeleteAccessory(item.accessoryId)}
                      title="Delete Accessory"
                      aria-label={`Delete ${item.accessoryName || "Accessory"}`}
                      className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-rose-50 hover:text-[#EF4444] cursor-pointer"
                    >
                      <TrashIcon className="h-4 w-4" />
                    </button>
                  )}
                </div>
              );
            },
          },
        ]
      : []),
  ];

  return (
    <div className="space-y-4">
      {/* Top Search Card */}
      <div className="rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-sm">
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-lg font-bold text-[#03312E] sm:text-xl">
              Accessory Inventory
            </h2>
            <p className="text-xs font-medium text-[#64748B]">
              Quantity-based peripheral inventory per product and model combination
            </p>
          </div>
        </div>

        {/* Search Bar */}
        <div className="mt-4">
          <div className="w-full">
            <SearchBar
              value={searchQuery}
              onChange={onSearchChange}
              placeholder="Search accessory, brand or model..."
              variant="long"
            />
          </div>
        </div>
      </div>

      {/* Reusable DataTable with automatic sorting on all columns */}
      <DataTable
        data={deduplicatedAccessories}
        columns={columns}
        keyField="accessoryId"
        showSerialNo={true}
        serialNoHeader="#"
        currentPage={currentPage + 1}
        totalPages={totalPages}
        totalItems={deduplicatedAccessories.length}
        pageSize={pageSize}
        itemLabel="accessories"
        onPageChange={(newPage) => onPageChange && onPageChange(newPage - 1)}
        loading={loading}
        emptyMessage="No accessories found"
        emptySubtitle="Try adjusting your search query."
      />
    </div>
  );
}
