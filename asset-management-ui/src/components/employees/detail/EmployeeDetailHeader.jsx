import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { StatusBadge, Button } from "../../common";

function getInitials(name = "") {
  return name
    .split(" ")
    .map((part) => part[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

export default function EmployeeDetailHeader({
  employee,
  device,
  summaryCounts = {},
  onExport,
}) {
  const navigate = useNavigate();
  const [copiedEmail, setCopiedEmail] = useState(false);

  if (!employee) return null;

  const initials = getInitials(employee.employeeName);

  const handleCopyEmail = () => {
    if (employee.email) {
      navigator.clipboard.writeText(employee.email);
      setCopiedEmail(true);
      setTimeout(() => setCopiedEmail(false), 2000);
    }
  };

  return (
    <div className="space-y-4">
      {/* Breadcrumb Navigation Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-sm text-[#5C7370]">
          <button
            type="button"
            onClick={() => navigate("/employees")}
            className="font-medium text-[#1976D2] hover:underline cursor-pointer flex items-center gap-1.5 transition-colors"
          >
            <svg
              className="h-4 w-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <line x1="19" y1="12" x2="5" y2="12" />
              <polyline points="12 19 5 12 12 5" />
            </svg>
            Employees
          </button>
          {/* <span className="text-[#A9D4C0]">/</span>
          <span className="font-mono text-xs font-bold text-[#03312E] bg-[#E3F2FD] px-2 py-0.5 rounded-md text-[#1976D2]">
            {employee.employeeId}
          </span>
          <span className="text-[#A9D4C0]">/</span>
          <span className="font-semibold text-[#03312E] truncate max-w-[200px] sm:max-w-none">
            {employee.employeeName}
          </span> */}
        </div>

        <div className="flex items-center gap-2.5">
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate("/employees")}
            className="flex items-center gap-2"
          >
            ← Back to List
          </Button>

          {onExport && (
            <Button
              variant="secondary"
              size="sm"
              onClick={onExport}
              className="flex items-center gap-2 text-xs"
            >
              <svg
                className="h-4 w-4"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="7 10 12 15 17 10" />
                <line x1="12" y1="15" x2="12" y2="3" />
              </svg>
              Export Specs
            </Button>
          )}
        </div>
      </div>

      {/* Main Employee Information Card */}
      <div className="relative overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
        {/* Subtle decorative gradient backdrop */}
        <div className="absolute top-0 right-0 h-32 w-96 bg-gradient-to-l from-[#EBF7F2] to-transparent pointer-events-none opacity-80" />

        <div className="relative flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
          {/* Left: Avatar + Details */}
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
            {/* Avatar with status indicator */}
            <div className="relative shrink-0">
              <div className="flex h-18 w-18 items-center justify-center rounded-2xl bg-gradient-to-br from-[#03312E] to-[#1976D2] text-xl font-extrabold text-white shadow-md ring-4 ring-[#EBF7F2]">
                {initials}
              </div>
              <span
                className={`absolute -bottom-1 -right-1 h-5 w-5 rounded-full border-2 border-white ${
                  (employee.accountStatus || "").toLowerCase() === "active"
                    ? "bg-emerald-500"
                    : "bg-amber-500"
                }`}
                title={`Status: ${employee.accountStatus || "Active"}`}
              />
            </div>

            {/* Employee Titles & Key Meta */}
            <div className="space-y-1.5">
              <div className="flex flex-wrap items-center gap-3">
                <h1 className="text-2xl font-extrabold text-[#03312E] tracking-tight">
                  {employee.employeeName}
                </h1>
                <span className="font-mono text-xs font-bold px-2.5 py-0.5 rounded-full bg-[#EBF7F2] text-[#03312E] border border-[#A9D4C0]">
                  {employee.employeeId}
                </span>
                <StatusBadge status={employee.accountStatus || "Active"} />
              </div>

              <div className="flex flex-wrap items-center gap-2 text-sm text-[#5C7370]">
                <span className="font-semibold text-[#03312E]">
                  {employee.role || "Specialist"}
                </span>
                <span className="text-[#CBD5E1]">•</span>
                <span className="text-[#5C7370]">
                  {employee.department || "Enterprise Engineering"}
                </span>
                <span className="text-[#CBD5E1]">•</span>
                <span className="text-[#5C7370]">
                  {employee.officeLocation || "San Francisco HQ"}
                </span>
              </div>

              {/* Contact Pill Row */}
              <div className="flex flex-wrap items-center gap-3 pt-1 text-xs">
                {employee.email && (
                  <button
                    type="button"
                    onClick={handleCopyEmail}
                    className="inline-flex items-center gap-1.5 rounded-lg bg-[#F8FAF9] px-2.5 py-1 text-[#03312E] border border-[#EBF0EE] hover:bg-[#E3F2FD] hover:text-[#1976D2] transition-colors cursor-pointer group"
                    title="Click to copy email address"
                  >
                    <svg
                      className="h-3.5 w-3.5 text-[#5C7370] group-hover:text-[#1976D2]"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                      <polyline points="22,6 12,13 2,6" />
                    </svg>
                    <span>{employee.email}</span>
                    <span className="text-[10px] text-[#8C9E9B]">
                      {copiedEmail ? "✓ Copied" : "Copy"}
                    </span>
                  </button>
                )}

                {employee.phone && (
                  <div className="inline-flex items-center gap-1.5 rounded-lg bg-[#F8FAF9] px-2.5 py-1 text-[#03312E] border border-[#EBF0EE]">
                    <svg
                      className="h-3.5 w-3.5 text-[#5C7370]"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
                    </svg>
                    <span>{employee.phone}</span>
                  </div>
                )}

                {employee.manager && (
                  <div className="inline-flex items-center gap-1.5 rounded-lg bg-[#F8FAF9] px-2.5 py-1 text-[#5C7370] border border-[#EBF0EE]">
                    <span className="font-semibold text-[#03312E]">Reports to:</span>
                    <span>{employee.manager}</span>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Right: Assigned Hardware Quick Badge */}
          {device && (
            <div className="flex shrink-0 flex-col sm:flex-row items-stretch sm:items-center gap-4 rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] p-4 lg:max-w-sm">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-[#E3F2FD] text-[#1976D2] shadow-2xs">
                <svg
                  className="h-6 w-6"
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
              </div>

              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-[#5C7370]">
                    Assigned Device
                  </span>
                  <StatusBadge status={device.status || "Active"} />
                </div>
                <p className="mt-0.5 truncate text-sm font-bold text-[#03312E]" title={device.deviceName}>
                  {device.deviceName}
                </p>
                <div className="mt-1 flex items-center gap-2 font-mono text-[11px] text-[#64748B]">
                  <span className="rounded bg-[#E2E8F0] px-1.5 py-0.2 text-[#1E293B] font-semibold">
                    {device.deviceId}
                  </span>
                  <span>•</span>
                  <span className="truncate">{device.os}</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Quick KPI Count Strip */}
        <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-5 border-t border-[#EBF0EE] pt-4">
          <div className="rounded-xl bg-[#F8FAF9] p-3 text-center border border-[#EBF0EE]">
            <p className="text-[11px] font-bold uppercase tracking-wider text-[#64748B]">
              Device
            </p>
            <p className="mt-0.5 text-lg font-black text-[#03312E]">
              {device ? "1 Assigned" : "None"}
            </p>
          </div>

          <div className="rounded-xl bg-[#F8FAF9] p-3 text-center border border-[#EBF0EE]">
            <p className="text-[11px] font-bold uppercase tracking-wider text-[#64748B]">
              Accessories
            </p>
            <p className="mt-0.5 text-lg font-black text-[#03312E]">
              {summaryCounts.accessories ?? 0}
            </p>
          </div>

          <div className="rounded-xl bg-[#F8FAF9] p-3 text-center border border-[#EBF0EE]">
            <p className="text-[11px] font-bold uppercase tracking-wider text-[#64748B]">
              Softwares
            </p>
            <p className="mt-0.5 text-lg font-black text-[#03312E]">
              {summaryCounts.softwares ?? 0}
            </p>
          </div>

          <div className="rounded-xl bg-[#F8FAF9] p-3 text-center border border-[#EBF0EE]">
            <p className="text-[11px] font-bold uppercase tracking-wider text-[#64748B]">
              Vulnerabilities
            </p>
            <p
              className={`mt-0.5 text-lg font-black ${
                (summaryCounts.vulnerabilities ?? 0) > 0
                  ? "text-rose-600"
                  : "text-emerald-600"
              }`}
            >
              {summaryCounts.vulnerabilities ?? 0}
            </p>
          </div>

          <div className="col-span-2 sm:col-span-1 rounded-xl bg-[#F8FAF9] p-3 text-center border border-[#EBF0EE]">
            <p className="text-[11px] font-bold uppercase tracking-wider text-[#64748B]">
              Licences
            </p>
            <p className="mt-0.5 text-lg font-black text-[#03312E]">
              {summaryCounts.licenses ?? 0}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
