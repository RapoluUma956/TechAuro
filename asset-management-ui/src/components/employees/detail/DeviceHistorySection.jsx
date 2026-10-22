import React, { useState } from "react";

export default function DeviceHistorySection({ deviceHistory, deviceId }) {
  const [activeFilter, setActiveFilter] = useState("all"); // "all" | "ownership" | "maintenance"

  if (!deviceHistory) return null;

  const ownershipList = deviceHistory.ownership || [];
  const maintenanceList = deviceHistory.maintenance || [];

  // Combine and sort chronologically (most recent first)
  const combinedHistory = [
    ...ownershipList.map((item) => ({
      ...item,
      category: "ownership",
      eventDate: item.fromDate || "2023-11-20",
    })),
    ...maintenanceList.map((item) => ({
      ...item,
      category: "maintenance",
      eventDate: item.date || "2023-11-18",
    })),
  ].sort((a, b) => new Date(b.eventDate) - new Date(a.eventDate));

  const filteredItems = combinedHistory.filter((item) => {
    if (activeFilter === "ownership") return item.category === "ownership";
    if (activeFilter === "maintenance") return item.category === "maintenance";
    return true;
  });

  return (
    <div className="space-y-4 rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
      {/* Header and Filter Buttons */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between border-b border-[#EBF0EE] pb-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold tracking-tight text-[#03312E]">
              Device History &amp; Lifecycle Audit
            </h2>
            <span className="rounded-full bg-[#EBF7F2] px-2.5 py-0.5 text-xs font-bold font-mono text-[#03312E] border border-[#A9D4C0]">
              {deviceId}
            </span>
          </div>
          <p className="mt-1 text-xs text-[#5C7370]">
            Complete chronological audit trail including prior employee custodians, formatting/resets, repairs, and diagnostics.
          </p>
        </div>

        {/* Filter Pills */}
        <div className="flex flex-wrap items-center gap-1.5 rounded-xl bg-[#F8FAF9] p-1 border border-[#EBF0EE]">
          <button
            type="button"
            onClick={() => setActiveFilter("all")}
            className={`cursor-pointer rounded-lg px-3 py-1.5 text-xs font-bold transition-all ${
              activeFilter === "all"
                ? "bg-[#1976D2] text-white shadow-2xs"
                : "text-[#5C7370] hover:text-[#03312E]"
            }`}
          >
            All Events ({combinedHistory.length})
          </button>
          <button
            type="button"
            onClick={() => setActiveFilter("ownership")}
            className={`cursor-pointer rounded-lg px-3 py-1.5 text-xs font-bold transition-all ${
              activeFilter === "ownership"
                ? "bg-[#1976D2] text-white shadow-2xs"
                : "text-[#5C7370] hover:text-[#03312E]"
            }`}
          >
            Past &amp; Current Custodians ({ownershipList.length})
          </button>
          <button
            type="button"
            onClick={() => setActiveFilter("maintenance")}
            className={`cursor-pointer rounded-lg px-3 py-1.5 text-xs font-bold transition-all ${
              activeFilter === "maintenance"
                ? "bg-[#1976D2] text-white shadow-2xs"
                : "text-[#5C7370] hover:text-[#03312E]"
            }`}
          >
            Format, Resets &amp; Repairs ({maintenanceList.length})
          </button>
        </div>
      </div>

      {/* Visual Timeline */}
      <div className="relative pt-2">
        {/* Continuous timeline line */}
        <div className="absolute top-4 bottom-4 left-6 w-0.5 bg-[#E2E8F0]" />

        <div className="space-y-6">
          {filteredItems.map((event, index) => {
            const isOwnership = event.category === "ownership";
            const isCurrent = isOwnership && event.toDate === "Present";
            const isReset = !isOwnership && (event.type || "").toLowerCase().includes("reset");
            const isRepair = !isOwnership && (event.type || "").toLowerCase().includes("repair");

            return (
              <div key={event.id || index} className="relative flex items-start gap-5">
                {/* Timeline Icon Node */}
                <div
                  className={`relative z-10 flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl shadow-xs ring-4 ring-white ${
                    isCurrent
                      ? "bg-emerald-600 text-white"
                      : isOwnership
                      ? "bg-[#1976D2] text-white"
                      : isReset
                      ? "bg-purple-600 text-white"
                      : isRepair
                      ? "bg-amber-500 text-white"
                      : "bg-[#03312E] text-white"
                  }`}
                >
                  {isOwnership ? (
                    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                      <circle cx="9" cy="7" r="4" />
                      <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                    </svg>
                  ) : isReset ? (
                    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polyline points="1 4 1 10 7 10" />
                      <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
                    </svg>
                  ) : (
                    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
                    </svg>
                  )}
                </div>

                {/* Event Card Content */}
                <div className="flex-1 rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] p-4 transition-all hover:bg-white hover:shadow-xs">
                  {/* Top line: Badges & Date */}
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="flex flex-wrap items-center gap-2">
                      <span
                        className={`inline-flex items-center gap-1 rounded-md px-2.5 py-0.5 text-xs font-bold uppercase tracking-wider ${
                          isCurrent
                            ? "bg-emerald-100 text-emerald-800"
                            : isOwnership
                            ? "bg-blue-100 text-blue-800"
                            : isReset
                            ? "bg-purple-100 text-purple-800"
                            : "bg-amber-100 text-amber-800"
                        }`}
                      >
                        {isOwnership ? event.role : event.type}
                      </span>

                      {isCurrent && (
                        <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700 border border-emerald-200">
                          <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
                          Active Custodian
                        </span>
                      )}

                      {event.certificateId && (
                        <span className="font-mono text-[11px] text-[#64748B] bg-white px-2 py-0.5 rounded border border-[#E2E8F0]">
                          Ref: {event.certificateId}
                        </span>
                      )}
                    </div>

                    <span className="text-xs font-semibold text-[#5C7370] font-mono">
                      {isOwnership ? `${event.fromDate} → ${event.toDate}` : event.date}
                    </span>
                  </div>

                  {/* Body Info */}
                  {isOwnership ? (
                    <div className="mt-3 space-y-2">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="text-base font-bold text-[#03312E]">
                          {event.employeeName}
                        </span>
                        <span className="font-mono text-xs font-bold text-[#1976D2] bg-[#E3F2FD] px-2 py-0.5 rounded">
                          {event.employeeId}
                        </span>
                        <span className="text-xs text-[#5C7370]">({event.department})</span>
                      </div>

                      <p className="text-xs text-[#334155] leading-relaxed">
                        <span className="font-semibold text-[#03312E]">Transfer Context: </span>
                        {event.handoverReason}
                      </p>

                      <div className="flex flex-wrap items-center gap-4 pt-1 text-[11px] text-[#64748B]">
                        <div>
                          <span className="font-semibold text-[#03312E]">Condition: </span>
                          <span className="text-emerald-700 font-medium">{event.condition}</span>
                        </div>
                        {event.authorizedBy && (
                          <div>
                            <span className="font-semibold text-[#03312E]">Authorized By: </span>
                            <span>{event.authorizedBy}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ) : (
                    <div className="mt-3 space-y-2">
                      <h3 className="text-sm font-bold text-[#03312E]">
                        {event.title}
                      </h3>

                      <p className="text-xs text-[#334155] leading-relaxed">
                        {event.description}
                      </p>

                      <div className="flex flex-wrap items-center gap-4 pt-1 text-[11px] text-[#64748B]">
                        <div>
                          <span className="font-semibold text-[#03312E]">Technician: </span>
                          <span>{event.performedBy}</span>
                        </div>
                        {event.facility && (
                          <div>
                            <span className="font-semibold text-[#03312E]">Facility: </span>
                            <span>{event.facility}</span>
                          </div>
                        )}
                        <div>
                          <span className="font-semibold text-[#03312E]">Status: </span>
                          <span className="text-emerald-700 font-bold">{event.status}</span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
