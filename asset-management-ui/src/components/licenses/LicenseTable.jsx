import React from "react";
import { DataTable, StatusBadge } from "../common";
import {
  EyeIcon,
  PencilIcon,
  UserPlusIcon,
  TrashIcon,
} from "../common/Icons";

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

  const getStatusBadge = (status, expiryDate) => {
    const s = String(status || "").toUpperCase();

    // Check if expiring within 30 days
    if (s === "ACTIVE" && expiryDate) {
      const today = new Date();
      const exp = new Date(expiryDate);
      const diffDays = Math.ceil((exp - today) / (1000 * 60 * 60 * 24));
      if (diffDays >= 0 && diffDays <= 30) {
        return (
          <StatusBadge
            status="Expiring Soon"
            variant="pill"
            customColors={{
              bg: "bg-amber-50",
              text: "text-amber-700",
              border: "border-amber-200",
            }}
          />
        );
      }
    }

    if (s === "ACTIVE") {
      return <StatusBadge status="Active" variant="pill" />;
    }
    if (s === "EXPIRING_SOON") {
      return (
        <StatusBadge
          status="Expiring Soon"
          variant="pill"
          customColors={{
            bg: "bg-amber-50",
            text: "text-amber-700",
            border: "border-amber-200",
          }}
        />
      );
    }
    if (s === "EXPIRED") {
      return <StatusBadge status="Expired" variant="pill" />;
    }
    if (s === "SUSPENDED") {
      return (
        <StatusBadge
          status="Suspended"
          variant="pill"
          customColors={{
            bg: "bg-purple-50",
            text: "text-purple-700",
            border: "border-purple-200",
          }}
        />
      );
    }
    if (s === "INACTIVE") {
      return <StatusBadge status="Inactive" variant="pill" />;
    }

    return <StatusBadge status={status || "Active"} variant="pill" />;
  };

  const columns = [
    {
      key: "licenseName",
      header: "License Name",
      render: (lic) => (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onViewLicense && onViewLicense(lic);
          }}
          className="font-bold text-sm text-[#03312E] hover:text-[#1976D2] cursor-pointer text-left transition-colors"
        >
          {lic.licenseName}
        </button>
      ),
    },
    {
      key: "softwareName",
      header: "Software",
      render: (lic) => (
        <span className="text-sm font-medium text-[#334155]">
          {lic.softwareName || "—"}
        </span>
      ),
    },
    {
      key: "publisher",
      header: "Publisher",
      render: (lic) => (
        <span className="text-sm text-[#334155]">{lic.publisher || "—"}</span>
      ),
    },
    {
      key: "licenseType",
      header: "Type",
      render: (lic) => (
        <span className="text-xs font-medium text-[#5C7370]">
          {formatLicenseType(lic.licenseType)}
        </span>
      ),
    },
    {
      key: "totalQuantity",
      header: "Total Seats",
      align: "center",
      render: (lic) => {
        const total = lic.totalQuantity ?? lic.totalLicenses ?? 0;
        return <span className="font-semibold text-sm text-[#03312E]">{total}</span>;
      },
    },
    {
      key: "assignedQuantity",
      header: "Assigned",
      align: "center",
      render: (lic) => {
        const assigned = lic.assignedQuantity ?? 0;
        return <span className="font-semibold text-sm text-[#00C896]">{assigned}</span>;
      },
    },
    {
      key: "availableQuantity",
      header: "Available",
      align: "center",
      render: (lic) => {
        const total = lic.totalQuantity ?? lic.totalLicenses ?? 0;
        const assigned = lic.assignedQuantity ?? 0;
        const available = lic.availableQuantity ?? Math.max(0, total - assigned);
        return <span className="font-semibold text-sm text-[#1976D2]">{available}</span>;
      },
    },
    {
      key: "expiryDate",
      header: "Expiry Date",
      render: (lic) => (
        <span className="text-xs font-medium text-[#5C7370]">
          {formatDate(lic.expiryDate)}
        </span>
      ),
    },
    {
      key: "status",
      header: "Status",
      align: "center",
      render: (lic) => getStatusBadge(lic.status, lic.expiryDate),
    },
    {
      key: "actions",
      header: "Actions",
      align: "center",
      render: (lic) => {
        const total = lic.totalQuantity ?? lic.totalLicenses ?? 0;
        const assigned = lic.assignedQuantity ?? 0;
        const available = lic.availableQuantity ?? Math.max(0, total - assigned);

        return (
          <div
            className="flex items-center justify-center gap-1"
            onClick={(e) => e.stopPropagation()}
          >

            {/* Edit Action */}
            {onEditLicense && (
              <button
                type="button"
                onClick={() => onEditLicense(lic)}
                title="Edit License"
                aria-label={`Edit license ${lic.licenseName}`}
                className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-yellow-50 hover:text-[#EAB308] cursor-pointer"
              >
                <PencilIcon className="h-4 w-4" />
              </button>
            )}

            {/* Assign Action */}
            {onAssignLicense && (
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
            )}

            {/* Delete Action */}
            {onDeleteLicense && (
              <button
                type="button"
                onClick={() => onDeleteLicense(lic)}
                title="Delete License"
                aria-label={`Delete license ${lic.licenseName}`}
                className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-rose-50 hover:text-[#EF4444] cursor-pointer"
              >
                <TrashIcon className="h-4 w-4" />
              </button>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <DataTable
      data={licenses}
      columns={columns}
      keyField="licenseId"
      showSerialNo={true}
      serialNoHeader="#"
      currentPage={currentPage + 1}
      totalPages={totalPages}
      totalItems={totalElements}
      pageSize={pageSize}
      itemLabel="licenses"
      onPageChange={(newPage) => onPageChange && onPageChange(newPage - 1)}
      onRowClick={onViewLicense ? (lic) => onViewLicense(lic) : undefined}
      showRowArrow={false}
      loading={loading}
      emptyMessage="No licenses found"
      emptySubtitle="Try adjusting your search criteria or add a new license."
    />
  );
}
