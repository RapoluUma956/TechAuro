import React, { useState, useEffect, useCallback } from "react";
import { Modal, Button } from "../common";
import { StackIcon } from "./LicenseIcons";
import { licenseService } from "../../services/licenseService";

export default function LicenseDetailModal({
  isOpen,
  onClose,
  licenseId,
  onReleaseSuccess,
}) {
  const [license, setLicense] = useState(null);
  const [loading, setLoading] = useState(true);
  const [releasingId, setReleasingId] = useState(null);
  const [releaseError, setReleaseError] = useState("");

  const fetchLicenseDetail = useCallback(async () => {
    if (!licenseId) return;
    try {
      setLoading(true);
      setReleaseError("");
      const data = await licenseService.getLicenseById(licenseId);
      setLicense(data);
    } catch (err) {
      setReleaseError(err.message || "Failed to load license details.");
    } finally {
      setLoading(false);
    }
  }, [licenseId]);

  useEffect(() => {
    if (isOpen && licenseId) {
      fetchLicenseDetail();
    } else {
      setLicense(null);
    }
  }, [isOpen, licenseId, fetchLicenseDetail]);

  if (!isOpen) return null;

  const handleRelease = async (assignmentId) => {
    if (!window.confirm("Are you sure you want to release / return this license seat?")) {
      return;
    }

    try {
      setReleasingId(assignmentId);
      setReleaseError("");
      const returnedAt = new Date().toISOString().split("T")[0];
      await licenseService.releaseLicense(licenseId, {
        assignmentId,
        returnedAt,
      });
      // Refresh current modal details
      await fetchLicenseDetail();
      if (onReleaseSuccess) {
        onReleaseSuccess();
      }
    } catch (err) {
      setReleaseError(err.message || "Failed to release license assignment.");
    } finally {
      setReleasingId(null);
    }
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

  const formatDateTime = (dtStr) => {
    if (!dtStr) return "—";
    try {
      const d = new Date(dtStr);
      if (isNaN(d.getTime())) return dtStr;
      return d.toLocaleString("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return dtStr;
    }
  };

  const totalSeats = license?.totalQuantity ?? 0;
  const assignedSeats = license?.assignedQuantity ?? 0;
  const availableSeats = license?.availableQuantity ?? Math.max(0, totalSeats - assignedSeats);
  const utilizationPercent = totalSeats > 0 ? Math.round((assignedSeats / totalSeats) * 100) : 0;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={license ? license.licenseName : "License Information"}
      subtitle={license ? `${license.softwareName} · Published by ${license.publisher}` : ""}
      size="xl"
      footer={
        <div className="flex w-full items-center justify-end">
          <Button variant="outline" size="md" onClick={onClose}>
            Close
          </Button>
        </div>
      }
    >
      {loading ? (
        <div className="flex flex-col items-center justify-center py-12">
          <div className="h-6 w-6 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent" />
          <p className="mt-2 text-xs font-semibold text-[#64748B]">
            Loading license details & assignments...
          </p>
        </div>
      ) : !license ? (
        <div className="py-8 text-center text-sm text-red-600">
          {releaseError || "Failed to load license details."}
        </div>
      ) : (
        <div className="space-y-6">
          {releaseError && (
            <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs font-semibold text-red-700">
              ⚠️ {releaseError}
            </div>
          )}

          {/* Top Summary Card */}
          <div className="rounded-2xl border border-[#DCE5E1] bg-[#F8FAF9] p-5">
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
              <div>
                <p className="text-xs font-semibold uppercase text-[#64748B]">Type</p>
                <p className="mt-1 text-sm font-bold text-[#03312E]">{license.licenseType || "Subscription"}</p>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase text-[#64748B]">Renewal</p>
                <p className="mt-1 text-sm font-bold text-[#03312E]">
                  {license.renewalType || "Yearly"} ({license.renewalMethod || "Auto"})
                </p>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase text-[#64748B]">Status</p>
                <p className="mt-1 text-sm font-bold text-emerald-600">{license.status || "Active"}</p>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase text-[#64748B]">Expiry Date</p>
                <p className="mt-1 text-sm font-bold text-[#03312E]">{formatDate(license.expiryDate)}</p>
              </div>
            </div>

            {/* Seat Utilization Bar */}
            <div className="mt-4 border-t border-[#EBF0EE] pt-4">
              <div className="flex items-center justify-between text-xs font-semibold text-[#03312E]">
                <span>
                  Seat Allocation: <strong>{assignedSeats}</strong> of <strong>{totalSeats}</strong> seats used
                </span>
                <span className="text-[#1976D2]">
                  <strong>{availableSeats}</strong> available ({utilizationPercent}% assigned)
                </span>
              </div>
              <div className="mt-2 h-2.5 w-full overflow-hidden rounded-full bg-slate-200">
                <div
                  className="h-full rounded-full bg-[#1976D2] transition-all duration-300"
                  style={{ width: `${Math.min(100, utilizationPercent)}%` }}
                />
              </div>
            </div>

            {license.licenseKey && (
              <div className="mt-4 rounded-xl border border-blue-200 bg-blue-50/50 p-3 text-xs">
                <span className="font-semibold text-[#1976D2]">License Key:</span>{" "}
                <code className="font-mono text-slate-800">{license.licenseKey}</code>
              </div>
            )}

            {license.notes && (
              <div className="mt-3 text-xs text-[#64748B]">
                <span className="font-semibold text-[#03312E]">Notes:</span> {license.notes}
              </div>
            )}
          </div>

          {/* Assignments History */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h4 className="text-sm font-bold text-[#03312E]">
                Seat Assignments History ({license.assignments?.length || 0})
              </h4>
            </div>

            <div className="overflow-hidden rounded-xl border border-[#DCE5E1] bg-white">
              <table className="w-full text-left text-xs text-[#03312E]">
                <thead className="border-b border-[#DCE5E1] bg-[#F8FAF9] font-bold uppercase tracking-wider text-[#64748B]">
                  <tr>
                    <th className="px-3.5 py-2.5">Employee</th>
                    <th className="px-3.5 py-2.5">Code</th>
                    <th className="px-3.5 py-2.5">Assigned At</th>
                    <th className="px-3.5 py-2.5">Returned At</th>
                    <th className="px-3.5 py-2.5 text-center">Status</th>
                    <th className="px-3.5 py-2.5">Notes</th>
                    <th className="px-3.5 py-2.5 text-center">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#EBF0EE]">
                  {(!license.assignments || license.assignments.length === 0) ? (
                    <tr>
                      <td colSpan="7" className="py-6 text-center text-[#64748B]">
                        No active or past assignments recorded for this license.
                      </td>
                    </tr>
                  ) : (
                    license.assignments.map((asg) => {
                      const isActive = asg.status === "ACTIVE";
                      return (
                        <tr key={asg.assignmentId} className="hover:bg-[#F8FAF9]">
                          <td className="px-3.5 py-2.5 font-bold text-[#03312E]">
                            {asg.employeeName}
                          </td>
                          <td className="px-3.5 py-2.5 font-mono text-[#64748B]">
                            {asg.employeeCode || "—"}
                          </td>
                          <td className="px-3.5 py-2.5 text-[#5C7370]">
                            {formatDateTime(asg.assignedAt)}
                          </td>
                          <td className="px-3.5 py-2.5 text-[#5C7370]">
                            {formatDateTime(asg.returnedAt)}
                          </td>
                          <td className="px-3.5 py-2.5 text-center">
                            <span
                              className={`inline-flex rounded-full px-2 py-0.5 text-2xs font-semibold ${
                                isActive
                                  ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                                  : "bg-slate-100 text-slate-600 border border-slate-200"
                              }`}
                            >
                              {asg.status}
                            </span>
                          </td>
                          <td className="px-3.5 py-2.5 text-[#64748B] max-w-[150px] truncate">
                            {asg.notes || "—"}
                          </td>
                          <td className="px-3.5 py-2.5 text-center">
                            {isActive ? (
                              <button
                                type="button"
                                onClick={() => handleRelease(asg.assignmentId)}
                                disabled={releasingId === asg.assignmentId}
                                className="cursor-pointer rounded-lg border border-amber-200 bg-amber-50 px-2 py-1 text-2xs font-semibold text-amber-800 transition-colors hover:bg-amber-100 hover:text-amber-900 disabled:opacity-50"
                              >
                                {releasingId === asg.assignmentId ? "Releasing..." : "Release Seat"}
                              </button>
                            ) : (
                              <span className="text-2xs text-[#94A3B8]">Released</span>
                            )}
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}
