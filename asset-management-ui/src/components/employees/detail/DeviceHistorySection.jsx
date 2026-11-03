import React from "react";

function formatDate(dateStr) {
  if (!dateStr || dateStr === "—") return "—";
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    const day = String(d.getDate()).padStart(2, "0");
    const months = [
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    ];
    const month = months[d.getMonth()];
    const year = d.getFullYear();
    return `${day}-${month}-${year}`;
  } catch {
    return dateStr;
  }
}

export default function DeviceHistorySection({ deviceHistory, deviceId }) {
  if (!deviceHistory) return null;

  const rawList = deviceHistory.ownership || [];

  // Sort chronologically by assigned_at date (earliest to latest)
  const assignments = [...rawList].sort((a, b) => {
    const da = new Date(a.assignedAt || a.fromDate || a.eventDate || 0);
    const db = new Date(b.assignedAt || b.fromDate || b.eventDate || 0);
    return da - db;
  });

  return (
    <div
      id="device-history-section"
      className="space-y-4 rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm"
    >
      {/* Header */}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between border-b border-[#EBF0EE] pb-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-bold tracking-tight text-[#03312E] sm:text-xl">
              Device Assignment History
            </h2>
            {deviceId && (
              <span className="rounded-full bg-[#EBF7F2] px-2.5 py-0.5 text-xs font-bold font-mono text-[#03312E] border border-[#A9D4C0]">
                {deviceId}
              </span>
            )}
          </div>
          <p className="mt-0.5 text-xs text-[#5C7370]">
            Complete chronological record of all employees to whom this device has been assigned.
          </p>
        </div>

        <span className="text-xs font-semibold text-[#1976D2] bg-[#E3F2FD] px-3 py-1 rounded-lg border border-[#BBDEFB] self-start sm:self-auto">
          {assignments.length} {assignments.length === 1 ? "Assignment" : "Assignments"} Recorded
        </span>
      </div>

      {/* Empty State */}
      {assignments.length === 0 ? (
        <div className="rounded-xl border border-dashed border-[#DCE5E1] bg-[#F8FAF9] p-8 text-center space-y-2">
          <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-full bg-[#E3F2FD] text-[#1976D2]">
            <svg
              className="h-5 w-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
          </div>
          <h3 className="text-sm font-bold text-[#03312E]">
            No Assignment History Found
          </h3>
          <p className="text-xs text-[#64748B] max-w-sm mx-auto">
            This device has never been assigned to any employee in the system.
          </p>
        </div>
      ) : (
        /* Table strictly showing the 6 required columns */
        <div className="overflow-x-auto rounded-xl border border-[#DCE5E1]">
          <table className="w-full text-left text-xs text-[#03312E]">
            <thead className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-[11px] font-bold uppercase tracking-wider text-[#5C7370]">
              <tr>
                <th className="py-3 px-3 text-center w-14">S.No</th>
                <th className="py-3 px-4">Employee Name</th>
                <th className="py-3 px-4">Employee Code</th>
                <th className="py-3 px-4">Assigned Date</th>
                <th className="py-3 px-4">Unassigned Date</th>
                <th className="py-3 px-4 text-center">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#EBF0EE] bg-white">
              {assignments.map((item, index) => {
                const isCurrentlyAssigned =
                  !item.returnedAt || item.status === "ACTIVE";
                const assignedDate = formatDate(
                  item.assignedAt || item.fromDate || item.eventDate
                );
                const unassignedDate = isCurrentlyAssigned ? (
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-bold text-emerald-700 border border-emerald-200">
                    <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
                    Currently Assigned
                  </span>
                ) : (
                  <span className="font-mono text-xs text-[#03312E]">
                    {formatDate(item.returnedAt || item.toDate)}
                  </span>
                );

                return (
                  <tr
                    key={item.id || item.assignmentId || index}
                    className={`transition-colors ${
                      isCurrentlyAssigned
                        ? "bg-[#F0FDF8]/60 font-semibold"
                        : "hover:bg-[#F8FAF9]"
                    }`}
                  >
                    <td className="py-3.5 px-3 text-center font-mono text-xs text-[#64748B]">
                      {index + 1}
                    </td>
                    <td className="py-3.5 px-4 font-bold text-sm text-[#03312E]">
                      {item.employeeName || item.name || "—"}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="rounded bg-[#E3F2FD] px-2 py-0.5 font-mono text-xs font-bold text-[#1976D2]">
                        {item.employeeCode || item.employeeId || "—"}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-xs text-[#03312E]">
                      {assignedDate}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-xs">
                      {unassignedDate}
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <span
                        className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] font-bold ${
                          isCurrentlyAssigned
                            ? "bg-emerald-100 text-emerald-800 border border-emerald-200"
                            : "bg-slate-100 text-slate-700 border border-slate-200"
                        }`}
                      >
                        {isCurrentlyAssigned ? "ACTIVE" : "RETURNED"}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
