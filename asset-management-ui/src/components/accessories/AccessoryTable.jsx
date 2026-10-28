import React, { useState } from "react";
import { SearchBar, Button, Pagination } from "../common";
import {
  PlusIcon,
  EditIcon,
  TrashIcon,
  RotateCcwIcon,
  AccessoryPackageIcon,
  UserPlusIcon,
} from "./AccessoryIcons";

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
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);

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

  return (
    <div className="overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white shadow-sm">
      {/* Top Header & Search Bar matching EmployeeFilters */}
      <div className="border-b border-[#EBF0EE] p-5 sm:p-6">
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-lg font-bold text-[#03312E] sm:text-xl">
              Accessory Inventory
            </h2>
            <p className="text-xs text-[#64748B]">
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

      {/* Table Content */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-[#03312E]">
          <thead className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-xs font-bold uppercase tracking-wider text-[#5C7370]">
            <tr>
              <th scope="col" className="w-16 py-3.5 pl-6 pr-4 text-center">
                S.No
              </th>
              <th scope="col" className="py-3.5 px-4">
                Accessory Name
              </th>
              <th scope="col" className="py-3.5 px-4">
                Category
              </th>
              <th scope="col" className="py-3.5 px-4 text-center">
                Total Stock
              </th>
              <th scope="col" className="py-3.5 px-4 text-center">
                Assigned
              </th>
              <th scope="col" className="py-3.5 px-4 text-center">
                Available
              </th>
              <th scope="col" className="py-3.5 px-4 text-center">
                In Repair
              </th>
              <th scope="col" className="py-3.5 px-4 text-right pr-6">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#EBF0EE]">
            {loading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <tr key={i} className="animate-pulse">
                  <td className="py-4 pl-6 pr-4 text-center">
                    <div className="mx-auto h-4 w-6 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="h-4 w-40 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="h-4 w-20 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="mx-auto h-4 w-8 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="mx-auto h-4 w-8 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="mx-auto h-4 w-8 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4">
                    <div className="mx-auto h-4 w-8 rounded bg-slate-100" />
                  </td>
                  <td className="py-4 px-4 pr-6">
                    <div className="ml-auto h-4 w-16 rounded bg-slate-100" />
                  </td>
                </tr>
              ))
            ) : accessories.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-16 text-center">
                  <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[#EBF7F2] text-[#03312E]">
                    <AccessoryPackageIcon className="h-7 w-7 text-[#03312E]" />
                  </div>
                  <h3 className="mt-4 text-base font-bold text-[#03312E]">
                    No accessories found
                  </h3>
                  <p className="mt-1 text-xs text-[#64748B]">
                    Try adjusting your search query or selecting another category filter.
                  </p>
                  {isFiltered && (
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={handleClearFilters}
                      className="mt-4"
                    >
                      Clear All Filters
                    </Button>
                  )}
                </td>
              </tr>
            ) : (
              accessories.map((item, index) => {
                const sNo = currentPage * pageSize + index + 1;
                const total = item.totalQuantity ?? 0;
                const assigned = item.assignedQuantity ?? 0;
                const unassigned = item.availableQuantity ?? 0;
                const repair = item.repairQuantity ?? item.damagedQuantity ?? 0;

                return (
                  <tr
                    key={item.accessoryId || index}
                    className="group transition-colors hover:bg-[#F0FDF8]"
                  >
                    <td className="py-4 pl-6 pr-4 text-center">
                      <span className="inline-block rounded-md bg-[#F1F5F9] px-2.5 py-1 font-mono text-xs font-bold text-[#1E293B]">
                        {sNo}
                      </span>
                    </td>
                    <td className="py-4 px-4">
                      <div className="font-semibold text-sm text-[#03312E] group-hover:text-[#1976D2] transition-colors">
                        {item.accessoryName || item.productName || "Unnamed Accessory"}
                      </div>
                      <div className="mt-0.5 text-xs text-[#64748B]">
                        {[item.brand, item.model, item.subCategory].filter(Boolean).join(" · ") || "Standard"}
                      </div>
                    </td>
                    <td className="py-4 px-4">
                      <span className="inline-flex items-center rounded-md bg-[#F1F5F9] px-2.5 py-1 text-xs font-semibold text-[#1E293B]">
                        {item.category || "General"}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-center font-bold text-sm text-[#03312E]">
                      {total}
                    </td>
                    <td className="py-4 px-4 text-center">
                      <span className="inline-flex items-center rounded-full bg-[#E8F8F2] px-2.5 py-0.5 text-xs font-bold text-[#00C896]">
                        {assigned}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-center">
                      <span className="inline-flex items-center rounded-full bg-[#EBF5FF] px-2.5 py-0.5 text-xs font-bold text-[#1976D2]">
                        {unassigned}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-center">
                      <span className="inline-flex items-center rounded-full bg-[#FFF4E5] px-2.5 py-0.5 text-xs font-bold text-[#F97316]">
                        {repair}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-right pr-6">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          type="button"
                          onClick={() => onAssignAccessory && onAssignAccessory(item)}
                          title={unassigned > 0 ? "Assign to Employee" : "No Available Units"}
                          disabled={unassigned <= 0}
                          className="cursor-pointer rounded-lg border border-[#DCE5E1] bg-white p-1.5 text-xs text-[#5C7370] transition-colors hover:border-[#00C896] hover:bg-[#E8F8F2] hover:text-[#00C896] disabled:cursor-not-allowed disabled:opacity-40"
                        >
                          <UserPlusIcon className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => onQuickAddQuantity && onQuickAddQuantity(item)}
                          title="Add Stock Units"
                          className="cursor-pointer rounded-lg border border-[#DCE5E1] bg-white p-1.5 text-xs text-[#5C7370] transition-colors hover:border-[#1976D2] hover:bg-[#E3F2FD] hover:text-[#1976D2]"
                        >
                          <PlusIcon className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => onEditAccessory && onEditAccessory(item)}
                          title="Edit Accessory"
                          className="cursor-pointer rounded-lg border border-[#DCE5E1] bg-white p-1.5 text-xs text-[#5C7370] transition-colors hover:border-[#03312E] hover:bg-[#F8FAF9] hover:text-[#03312E]"
                        >
                          <EditIcon className="h-4 w-4" />
                        </button>
                        {deleteConfirmId === item.accessoryId ? (
                          <div className="flex items-center gap-1">
                            <button
                              type="button"
                              onClick={() => {
                                onDeleteAccessory && onDeleteAccessory(item.accessoryId);
                                setDeleteConfirmId(null);
                              }}
                              className="cursor-pointer rounded-lg bg-[#EF4444] px-2 py-1 text-xs font-bold text-white hover:bg-[#DC2626]"
                            >
                              Confirm
                            </button>
                            <button
                              type="button"
                              onClick={() => setDeleteConfirmId(null)}
                              className="cursor-pointer rounded-lg bg-slate-200 px-2 py-1 text-xs text-[#64748B] hover:bg-slate-300"
                            >
                              ✕
                            </button>
                          </div>
                        ) : (
                          <button
                            type="button"
                            onClick={() => setDeleteConfirmId(item.accessoryId)}
                            title="Delete Accessory"
                            className="cursor-pointer rounded-lg border border-[#DCE5E1] bg-white p-1.5 text-xs text-[#5C7370] transition-colors hover:border-red-300 hover:bg-[#FEECEB] hover:text-[#EF4444]"
                          >
                            <TrashIcon className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination using Common Pagination */}
      <Pagination
        currentPage={currentPage + 1}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={pageSize}
        itemLabel="accessories"
        onPageChange={(pageOneIndexed) => onPageChange && onPageChange(pageOneIndexed - 1)}
      />
    </div>
  );
}
