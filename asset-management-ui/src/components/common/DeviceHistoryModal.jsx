import React, { useState, useEffect } from "react";
import Modal from "./Modal";
import Button from "./Button";
import { employeeService } from "../../services/employeeService";

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

export default function DeviceHistoryModal({
  isOpen = false,
  onClose,
  deviceId,
  device = null,
}) {
  const [historyList, setHistoryList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!isOpen || !deviceId) {
      setHistoryList([]);
      setError(null);
      return;
    }

    let isMounted = true;
    setLoading(true);
    setError(null);

    employeeService
      .getDeviceHistory(deviceId)
      .then((res) => {
        if (!isMounted) return;
        const list = res?.content || res?.history || (Array.isArray(res) ? res : []);
        // Sort chronologically by assigned date (earliest to latest)
        const sorted = [...list].sort((a, b) => {
          const da = new Date(a.assignedAt || a.eventDate || 0);
          const db = new Date(b.assignedAt || b.eventDate || 0);
          return da - db;
        });
        setHistoryList(sorted);
      })
      .catch((err) => {
        if (!isMounted) return;
        console.error("Failed to load device assignment history:", err);
        setError(err?.message || "Could not retrieve assignment history for this device.");
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, deviceId]);

  const deviceName =
    device?.deviceName ||
    (device?.brand && device?.model
      ? `${device.brand} ${device.model}`
      : "Selected Device");
  const serialNumber = device?.serialNumber || "—";
  const assetTag = device?.assetTag || (deviceId ? `AST-${deviceId}` : "—");

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Device Assignment History"
      subtitle={`${deviceName} • Serial: ${serialNumber} • Asset Tag: ${assetTag}`}
      size="xl"
      footer={
        <div className="flex justify-end w-full">
          <Button variant="secondary" size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      }
    >
      <div className="space-y-4">
        {/* Loading State */}
        {loading && (
          <div className="py-12 text-center text-sm text-[#64748B] space-y-3">
            <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent" />
            <p>Loading assignment history...</p>
          </div>
        )}

        {/* Error State */}
        {!loading && error && (
          <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-700">
            <p className="font-bold">Error loading history</p>
            <p className="mt-1">{error}</p>
          </div>
        )}

        {/* Empty State */}
        {!loading && !error && historyList.length === 0 && (
          <div className="rounded-2xl border border-dashed border-[#DCE5E1] p-12 text-center space-y-3">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-[#64748B]">
              <svg
                className="h-6 w-6"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <h3 className="text-base font-bold text-[#03312E]">
              No Assignment History Found
            </h3>
            <p className="text-xs text-[#64748B] max-w-sm mx-auto">
              This device has never been assigned to any employee in the system.
            </p>
          </div>
        )}

        {/* Simplified History Table strictly showing the 6 required columns */}
        {!loading && !error && historyList.length > 0 && (
          <div className="overflow-x-auto rounded-xl border border-[#DCE5E1]">
            <table className="w-full text-left text-xs text-[#03312E]">
              <thead className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-xs font-bold uppercase tracking-wider text-[#64748B]">
                <tr>
                  <th className="py-3.5 px-3 text-center w-14 text-xs font-bold uppercase tracking-wider text-[#64748B]">#</th>
                  <th className="py-3.5 px-4">Employee Name</th>
                  <th className="py-3.5 px-4">Employee Code</th>
                  <th className="py-3.5 px-4">Assigned Date</th>
                  <th className="py-3.5 px-4">Unassigned Date</th>
                  <th className="py-3.5 px-4 text-center">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#EBF0EE] bg-white">
                {historyList.map((item, index) => {
                  const isCurrentlyAssigned =
                    !item.returnedAt || item.status === "ACTIVE";
                  const assignedDate = formatDate(
                    item.assignedAt || item.eventDate
                  );
                  const unassignedDate = isCurrentlyAssigned ? (
                    <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-bold text-emerald-700 border border-emerald-200">
                      <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
                      Currently Assigned
                    </span>
                  ) : (
                    <span className="font-mono text-xs text-[#03312E]">
                      {formatDate(item.returnedAt)}
                    </span>
                  );

                  return (
                    <tr
                      key={item.assignmentId || item.eventId || index}
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
                        {item.employeeName || "—"}
                      </td>
                      <td className="py-3.5 px-4">
                        <span className="rounded bg-[#E3F2FD] px-2 py-0.5 font-mono text-xs font-bold text-[#1976D2]">
                          {item.employeeCode || "—"}
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
    </Modal>
  );
}
