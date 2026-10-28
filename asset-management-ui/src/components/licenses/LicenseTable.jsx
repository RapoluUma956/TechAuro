import React, { useState } from "react";
import {
  EyeIcon,
  PencilIcon,
  UserPlusIcon,
  TrashIcon,
} from "./LicenseIcons";

export default function LicenseTable({
  licenses = [],
  loading = false,
  totalElements = 0,
  currentPage = 0,
  pageSize = 10,
  totalPages = 1,
  onPageChange,
  onViewLicense,
  onEditLicense,
  onAssignLicense,
  onDeleteLicense,
}) {
  const [selectedIds, setSelectedIds] = useState([]);

  const allIds = licenses.map((l) => l.licenseId);
  const isAllSelected = licenses.length > 0 && selectedIds.length === licenses.length;

  const toggleSelectAll = () => {
    if (isAllSelected) {
      setSelectedIds([]);
    } else {
      setSelectedIds(allIds);
    }
  };

  const toggleSelect = (id) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    try {
      const d = new Date(dateStr);
      if (isNaN(d.getTime())) return dateStr;
      return d.toLocaleDateString("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      });
    } catch {
      return dateStr;
    }
  };

  const getStatusBadge = (status, expiryDate) => {
    const s = String(status || "").toUpperCase();

    // Check if expiring within 30 days
    if (s === "ACTIVE" && expiryDate) {
      const today = new Date();
      const exp = new Date(expiryDate);
      const diffDays = Math.ceil((exp - today) / (1000 * 60 * 60 * 24));
      if (diffDays >= 0 && diffDays <= 30) {
        return (
          <span className="inline-flex items-center rounded-full bg-amber-50 px-2.5 py-0.5 text-xs font-semibold text-amber-700 border border-amber-200">
            Expiring Soon
          </span>
        );
      }
    }

    switch (s) {
      case "ACTIVE":
        return (
          <span className="inline-flex items-center rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700 border border-emerald-200">
            Active
          </span>
        );
      case "EXPIRING_SOON":
        return (
          <span className="inline-flex items-center rounded-full bg-amber-50 px-2.5 py-0.5 text-xs font-semibold text-amber-700 border border-amber-200">
            Expiring Soon
          </span>
        );
      case "EXPIRED":
        return (
          <span className="inline-flex items-center rounded-full bg-rose-50 px-2.5 py-0.5 text-xs font-semibold text-rose-700 border border-rose-200">
            Expired
          </span>
        );
      case "SUSPENDED":
        return (
          <span className="inline-flex items-center rounded-full bg-purple-50 px-2.5 py-0.5 text-xs font-semibold text-purple-700 border border-purple-200">
            Suspended
          </span>
        );
      case "INACTIVE":
        return (
          <span className="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-700 border border-slate-200">
            Inactive
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-700 border border-slate-200">
            {status || "Active"}
          </span>
        );
    }
  };

  const formatLicenseType = (type) => {
    if (!type) return "Subscription";
    const t = String(type).toUpperCase();
    if (t === "SUBSCRIPTION") return "Subscription";
    if (t === "PERPETUAL") return "Perpetual";
    if (t === "VOLUME") return "Volume";
    if (t === "OEM") return "OEM";
    if (t === "FREE_OPEN_SOURCE") return "Open Source";
    return type;
  };

  const startEntry = totalElements === 0 ? 0 : currentPage * pageSize + 1;
  const endEntry = Math.min((currentPage + 1) * pageSize, totalElements);

  return (
    <div className="overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white shadow-2xs">
      {/* Table Container */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-[#03312E]">
          <thead className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-xs font-bold uppercase tracking-wider text-[#64748B]">
            <tr>
              <th scope="col" className="w-10 px-4 py-3.5 text-center">
                <input
                  type="checkbox"
                  checked={isAllSelected}
                  onChange={toggleSelectAll}
                  aria-label="Select all licenses"
                  className="h-4 w-4 rounded border-slate-300 text-[#1976D2] focus:ring-[#1976D2]/20 cursor-pointer"
                />
              </th>
              <th scope="col" className="w-12 px-3 py-3.5 text-center">
                #
              </th>
              <th scope="col" className="px-4 py-3.5">
                License Name
              </th>
              <th scope="col" className="px-4 py-3.5">
                Software
              </th>
              <th scope="col" className="px-4 py-3.5">
                Publisher
              </th>
              <th scope="col" className="px-4 py-3.5">
                Type
              </th>
              <th scope="col" className="px-4 py-3.5 text-center">
                Total Seats
              </th>
              <th scope="col" className="px-4 py-3.5 text-center">
                Assigned
              </th>
              <th scope="col" className="px-4 py-3.5 text-center">
                Available
              </th>
              <th scope="col" className="px-4 py-3.5">
                Expiry Date
              </th>
              <th scope="col" className="px-4 py-3.5 text-center">
                Status
              </th>
              <th scope="col" className="px-4 py-3.5 text-center">
                Actions
              </th>
            </tr>
          </thead>

          <tbody className="divide-y divide-[#EBF0EE]">
            {loading ? (
              <tr>
                <td colSpan="12" className="px-6 py-12 text-center text-sm text-[#64748B]">
                  <div className="flex items-center justify-center gap-2">
                    <div className="h-5 w-5 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent" />
                    <span>Loading licenses inventory...</span>
                  </div>
                </td>
              </tr>
            ) : licenses.length === 0 ? (
              <tr>
                <td colSpan="12" className="px-6 py-12 text-center text-sm text-[#64748B]">
                  <div className="flex flex-col items-center justify-center gap-2">
                    <span className="text-3xl">📄</span>
                    <p className="font-semibold text-[#03312E]">No licenses found</p>
                    <p className="text-xs text-[#64748B]">
                      Try adjusting your search criteria or add a new license.
                    </p>
                  </div>
                </td>
              </tr>
            ) : (
              licenses.map((lic, index) => {
                const isSelected = selectedIds.includes(lic.licenseId);
                const sNo = currentPage * pageSize + index + 1;
                const total = lic.totalQuantity ?? lic.totalLicenses ?? 0;
                const assigned = lic.assignedQuantity ?? 0;
                const available = lic.availableQuantity ?? Math.max(0, total - assigned);

                return (
                  <tr
                    key={lic.licenseId || index}
                    className={`transition-colors hover:bg-[#F8FAF9] ${
                      isSelected ? "bg-blue-50/40" : ""
                    }`}
                  >
                    {/* Checkbox */}
                    <td className="px-4 py-3.5 text-center">
                      <input
                        type="checkbox"
                        checked={isSelected}
                        onChange={() => toggleSelect(lic.licenseId)}
                        aria-label={`Select license ${lic.licenseName}`}
                        className="h-4 w-4 rounded border-slate-300 text-[#1976D2] focus:ring-[#1976D2]/20 cursor-pointer"
                      />
                    </td>

                    {/* S.No */}
                    <td className="px-3 py-3.5 text-center text-xs font-semibold text-[#64748B]">
                      {sNo}
                    </td>

                    {/* License Name */}
                    <td className="px-4 py-3.5 font-bold text-[#03312E]">
                      <button
                        type="button"
                        onClick={() => onViewLicense(lic)}
                        className="text-left hover:text-[#1976D2] cursor-pointer"
                      >
                        {lic.licenseName}
                      </button>
                    </td>

                    {/* Software */}
                    <td className="px-4 py-3.5 text-sm text-[#334155]">
                      {lic.softwareName || "—"}
                    </td>

                    {/* Publisher */}
                    <td className="px-4 py-3.5 text-sm text-[#334155]">
                      {lic.publisher || "—"}
                    </td>

                    {/* Type */}
                    <td className="px-4 py-3.5 text-xs text-[#5C7370]">
                      {formatLicenseType(lic.licenseType)}
                    </td>

                    {/* Total Seats */}
                    <td className="px-4 py-3.5 text-center font-semibold text-[#03312E]">
                      {total}
                    </td>

                    {/* Assigned */}
                    <td className="px-4 py-3.5 text-center font-semibold text-[#00C896]">
                      {assigned}
                    </td>

                    {/* Available */}
                    <td className="px-4 py-3.5 text-center font-semibold text-[#1976D2]">
                      {available}
                    </td>

                    {/* Expiry Date */}
                    <td className="px-4 py-3.5 text-xs text-[#5C7370]">
                      {formatDate(lic.expiryDate)}
                    </td>

                    {/* Status */}
                    <td className="px-4 py-3.5 text-center">
                      {getStatusBadge(lic.status, lic.expiryDate)}
                    </td>

                    {/* Actions: View, Edit, Assign, Delete */}
                    <td className="px-4 py-3.5 text-center">
                      <div className="flex items-center justify-center gap-1">
                        {/* View Action */}
                        <button
                          type="button"
                          onClick={() => onViewLicense(lic)}
                          title="View Details & Assignments"
                          aria-label={`View details for ${lic.licenseName}`}
                          className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-sky-50 hover:text-[#1976D2] cursor-pointer"
                        >
                          <EyeIcon className="h-4 w-4" />
                        </button>

                        {/* Edit Action */}
                        <button
                          type="button"
                          onClick={() => onEditLicense(lic)}
                          title="Edit License"
                          aria-label={`Edit license ${lic.licenseName}`}
                          className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-slate-100 hover:text-[#03312E] cursor-pointer"
                        >
                          <PencilIcon className="h-4 w-4" />
                        </button>

                        {/* Assign Action */}
                        <button
                          type="button"
                          onClick={() => onAssignLicense(lic)}
                          disabled={available <= 0}
                          title={
                            available <= 0
                              ? "No available seats to assign"
                              : "Assign Seat to Employee"
                          }
                          aria-label={`Assign seat for ${lic.licenseName}`}
                          className={`rounded-lg p-1.5 transition-colors ${
                            available <= 0
                              ? "cursor-not-allowed text-slate-300 opacity-40"
                              : "text-[#64748B] hover:bg-emerald-50 hover:text-[#00C896] cursor-pointer"
                          }`}
                        >
                          <UserPlusIcon className="h-4 w-4" />
                        </button>

                        {/* Delete Action */}
                        <button
                          type="button"
                          onClick={() => onDeleteLicense(lic)}
                          title="Delete License"
                          aria-label={`Delete license ${lic.licenseName}`}
                          className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-rose-50 hover:text-[#EF4444] cursor-pointer"
                        >
                          <TrashIcon className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer matching wireframe */}
      <div className="flex flex-col items-center justify-between gap-3 border-t border-[#DCE5E1] bg-[#F8FAF9] px-5 py-3.5 sm:flex-row">
        <p className="text-xs text-[#64748B]">
          Showing <span className="font-semibold text-[#03312E]">{startEntry}</span> to{" "}
          <span className="font-semibold text-[#03312E]">{endEntry}</span> of{" "}
          <span className="font-semibold text-[#03312E]">{totalElements}</span> entries
        </p>

        {totalPages > 1 && (
          <nav aria-label="Pagination" className="flex items-center gap-1">
            {/* Prev button */}
            <button
              type="button"
              disabled={currentPage === 0}
              onClick={() => onPageChange(currentPage - 1)}
              aria-label="Previous page"
              className="flex h-8 w-8 items-center justify-center rounded-lg border border-[#DCE5E1] bg-white text-xs font-semibold text-[#64748B] shadow-2xs transition-colors hover:border-[#1976D2] hover:text-[#1976D2] disabled:cursor-not-allowed disabled:opacity-40 cursor-pointer"
            >
              ‹
            </button>

            {/* Page number buttons */}
            {Array.from({ length: totalPages }).map((_, idx) => {
              const isCurrent = idx === currentPage;
              return (
                <button
                  key={idx}
                  type="button"
                  onClick={() => onPageChange(idx)}
                  aria-current={isCurrent ? "page" : undefined}
                  className={`flex h-8 w-8 items-center justify-center rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    isCurrent
                      ? "bg-[#03312E] text-white shadow-xs"
                      : "border border-[#DCE5E1] bg-white text-[#5C7370] hover:border-[#1976D2] hover:text-[#1976D2]"
                  }`}
                >
                  {idx + 1}
                </button>
              );
            })}

            {/* Next button */}
            <button
              type="button"
              disabled={currentPage >= totalPages - 1}
              onClick={() => onPageChange(currentPage + 1)}
              aria-label="Next page"
              className="flex h-8 w-8 items-center justify-center rounded-lg border border-[#DCE5E1] bg-white text-xs font-semibold text-[#64748B] shadow-2xs transition-colors hover:border-[#1976D2] hover:text-[#1976D2] disabled:cursor-not-allowed disabled:opacity-40 cursor-pointer"
            >
              ›
            </button>
          </nav>
        )}
      </div>
    </div>
  );
}
