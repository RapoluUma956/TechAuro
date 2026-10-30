import React from "react";
import { DataTable, StatusBadge, Button } from "../common";
import { CalendarIcon } from "./EmployeeIcons";

function formatWarrantyDate(dateString) {
  if (!dateString) return "N/A";
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
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
              <div className="font-semibold text-sm text-[#03312E] group-hover:text-[#1976D2] transition-colors">
                {emp.employeeName}
              </div>
              {emp.email && (
                <div className="text-xs text-[#64748B]">{emp.email}</div>
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
        <div className="flex items-center gap-2">
          <svg
            className="h-4 w-4 shrink-0 text-[#64748B]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <rect x="2" y="3" width="20" height="14" rx="2" />
            <line x1="8" y1="21" x2="16" y2="21" />
            <line x1="12" y1="17" x2="12" y2="21" />
          </svg>
          <span className="text-sm font-medium text-[#03312E]">
            {emp.deviceName || "—"}
          </span>
        </div>
      ),
    },
    {
      key: "deviceStatus",
      header: "Device Status",
      render: (emp) => <StatusBadge status={emp.deviceStatus || "Unassigned"} />,
    },
    {
      key: "warrantyDate",
      header: "Warranty Date",
      render: (emp) => (
        <div className="flex items-center gap-1.5 text-xs text-[#5C7370]">
          <CalendarIcon className="h-3.5 w-3.5 text-[#8C9E9B]" />
          <span>{formatWarrantyDate(emp.warrantyDate)}</span>
        </div>
      ),
    },
  ];

  return (
    <DataTable
      data={employees}
      columns={columns}
      keyField="id"
      showSerialNo={true}
      currentPage={currentPage}
      totalPages={totalPages}
      totalItems={totalEmployeesCount}
      pageSize={pageSize}
      itemLabel="employees"
      onPageChange={onPageChange}
      onRowClick={onRowClick}
      showRowArrow={true}
      emptyMessage="No matching employees or devices found"
      emptySubtitle="Try adjusting your search criteria, clearing selected filters, or adding a new employee."
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
