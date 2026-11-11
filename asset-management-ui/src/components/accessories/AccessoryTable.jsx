import React from "react";
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
  selectedCategory = "All",
  onCategoryChange,
  categories = [],
  onEditAccessory,
  onDeleteAccessory,
  onQuickAddQuantity,
  onAssignAccessory,
}) {
  const defaultCategories = [
    "All",
    "Mouse",
    "Keyboard",
    "Headset",
    "Monitor",
    "Docking Station",
    "Charger / Adapter",
    "Other",
  ];

  const categoryList =
    categories && categories.length > 0
      ? ["All", ...categories.map((c) => (typeof c === "string" ? c : c.category))]
      : defaultCategories;

  const isFiltered = Boolean(
    searchQuery.trim() || (selectedCategory && selectedCategory !== "All")
  );

  const handleClearFilters = () => {
    onSearchChange && onSearchChange("");
    onCategoryChange && onCategoryChange("All");
  };

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
      {/* Top Filter Card */}
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

          {isFiltered && (
            <Button
              variant="outline"
              size="sm"
              icon={<RotateCcwIcon className="h-3.5 w-3.5" />}
              onClick={handleClearFilters}
              className="self-start text-[#5C7370] hover:border-[#EF4444] hover:bg-[#FEECEB] hover:text-[#EF4444] sm:self-auto"
            >
              Reset Filters
            </Button>
          )}
        </div>

        {/* Search Bar & Category Filter Pills */}
        <div className="mt-4 space-y-3">
          <div className="w-full">
            <SearchBar
              value={searchQuery}
              onChange={onSearchChange}
              placeholder="Search accessory, brand or model..."
              variant="long"
            />
          </div>

          <div className="flex flex-wrap items-center gap-1.5 pt-1">
            <span className="mr-1 text-xs font-semibold text-[#64748B]">
              Category:
            </span>
            {categoryList.map((cat) => {
              const isSelected =
                selectedCategory === cat ||
                (!selectedCategory && cat === "All") ||
                selectedCategory?.toLowerCase() === cat.toLowerCase();

              return (
                <button
                  key={cat}
                  type="button"
                  onClick={() => onCategoryChange && onCategoryChange(cat)}
                  className={`cursor-pointer rounded-full px-3 py-1 text-xs font-semibold transition-all select-none ${
                    isSelected
                      ? "bg-[#1976D2] text-white shadow-xs ring-1 ring-[#1976D2]"
                      : "border border-[#DCE5E1] bg-white text-[#5C7370] hover:border-[#1976D2] hover:bg-[#F8FAF9] hover:text-[#03312E]"
                  }`}
                >
                  {cat}
                </button>
              );
            })}
          </div>

          {isFiltered && (
            <div className="flex items-center justify-between border-t border-[#EBF0EE] pt-2.5 text-xs text-[#64748B]">
              <span>
                Showing <strong className="text-[#03312E]">{accessories.length}</strong> matching accessories
              </span>
              <button
                type="button"
                onClick={handleClearFilters}
                className="cursor-pointer font-semibold text-[#1976D2] hover:underline"
              >
                Clear all filters
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Reusable DataTable matching LicenseTable styling */}
      <DataTable
        data={accessories}
        columns={columns}
        keyField="accessoryId"
        showSerialNo={true}
        serialNoHeader="#"
        currentPage={currentPage + 1}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={pageSize}
        itemLabel="accessories"
        onPageChange={(newPage) => onPageChange && onPageChange(newPage - 1)}
        loading={loading}
        emptyMessage="No accessories found"
        emptySubtitle="Try adjusting your search query or selecting another category filter."
        emptyAction={
          isFiltered ? (
            <Button variant="primary" size="sm" onClick={handleClearFilters}>
              Clear All Filters
            </Button>
          ) : null
        }
      />
    </div>
  );
}
