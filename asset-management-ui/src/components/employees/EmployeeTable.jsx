import React from "react";
import { DataTable, StatusBadge, Button } from "../common";
import { CalendarIcon, PencilIcon, TrashIcon } from "../common/Icons";

function formatWarrantyDate(dateString) {
  if (!dateString) return "N/A";
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    return date.toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  } catch {
    return dateString;
  }
}

function getInitials(name = "") {
  return name
    .split(" ")
    .map((part) => part[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

export default function EmployeeTable({
  employees = [],
  totalEmployeesCount = 0,
  currentPage = 1,
  pageSize = 10,
  totalPages = 1,
  onPageChange,
  onRowClick,
  onClearFilters,
  sortBy,
  sortDir,
  onSortChange,
  onEdit,
  onDelete,
}) {
  const columns = [
    {
      key: "employeeId",
      header: "Employee ID",
      render: (emp) => (
        <span className="inline-block rounded-md bg-[#F1F5F9] px-2.5 py-1 font-mono text-xs font-bold text-[#1E293B]">
          {emp.employeeId}
        </span>
      ),
    },
    {
      key: "employeeName",
      header: "Employee Name",
      render: (emp) => {
        const initials = getInitials(emp.employeeName);
        return (
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[#E3F2FD] font-bold text-xs text-[#1976D2] ring-2 ring-white shadow-xs">
              {initials}
            </div>
            <div>
              <div className="font-bold text-sm text-[#03312E] group-hover:text-[#1976D2] transition-colors">
                {emp.employeeName}
              </div>
              {emp.email && (
                <div className="text-xs font-medium text-[#64748B]">{emp.email}</div>
              )}
            </div>
          </div>
        );
      },
    },
    {
      key: "role",
      header: "Role",
      render: (emp) => (
        <span className="text-sm font-medium text-[#334155]">
          {emp.role || "—"}
        </span>
      ),
    },
    {
      key: "serialNumber",
      header: "Serial Number",
      render: (emp) => {
        const serial = emp.serialNumber || emp.deviceId;
        if (!serial || serial === "Unassigned" || serial === "—") {
          return (
            <span className="inline-block rounded-md bg-gray-100 px-2.5 py-1 font-mono text-xs font-medium text-gray-500">
              Unassigned
            </span>
          );
        }
        return (
          <span className="inline-block rounded-md bg-[#E3F2FD] px-2.5 py-1 font-mono text-xs font-bold text-[#1976D2] border border-[#BBDEFB]">
            {serial}
          </span>
        );
      },
    },
    {
      key: "deviceName",
      header: "Device Name",
      render: (emp) => (
        <span className="text-sm font-medium text-[#334155]">
          {emp.deviceName || "—"}
        </span>
      ),
    },
    {
      key: "deviceStatus",
      header: "Device Status",
      render: (emp) => <StatusBadge status={emp.deviceStatus || "Unassigned"} variant="pill" />,
    },
    {
      key: "warrantyDate",
      header: "Warranty Date",
      render: (emp) => (
        <div className="flex items-center gap-1.5 text-xs font-medium text-[#5C7370]">
          <CalendarIcon className="h-3.5 w-3.5 text-[#8C9E9B]" />
          <span>{formatWarrantyDate(emp.warrantyDate)}</span>
        </div>
      ),
    },
    ...(onEdit || onDelete
      ? [
          {
            key: "actions",
            header: "Actions",
            align: "right",
            render: (emp) => (
              <div
                className="flex items-center justify-end gap-1"
                onClick={(e) => e.stopPropagation()}
              >
                {onEdit && (
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      onEdit(emp);
                    }}
                    title="Edit Employee"
                    aria-label={`Edit ${emp.employeeName}`}
                    className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-yellow-50 hover:text-[#EAB308] cursor-pointer"
                  >
                    <PencilIcon className="h-4 w-4" />
                  </button>
                )}
                {onDelete && (
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      onDelete(emp);
                    }}
                    title="Delete Employee"
                    aria-label={`Delete ${emp.employeeName}`}
                    className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-rose-50 hover:text-[#EF4444] cursor-pointer"
                  >
                    <TrashIcon className="h-4 w-4" />
                  </button>
                )}
              </div>
            ),
          },
        ]
      : []),
  ];

  return (
    <DataTable
      data={employees}
      columns={columns}
      keyField="id"
      showSerialNo={true}
      serialNoHeader="#"
      currentPage={currentPage}
      totalPages={totalPages}
      totalItems={totalEmployeesCount}
      pageSize={pageSize}
      itemLabel="employees"
      onPageChange={onPageChange}
      onRowClick={onRowClick}
      showRowArrow={true}
      sortBy={sortBy}
      sortDir={sortDir}
      onSortChange={onSortChange}
      emptyMessage="No matching employees or devices found"
      emptySubtitle="Try adjusting your search criteria or adding a new employee."
      emptyAction={
        onClearFilters ? (
          <Button variant="primary" size="sm" onClick={onClearFilters}>
            Clear All Filters
          </Button>
        ) : null
      }
    />
  );
}
