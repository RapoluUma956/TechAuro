import React, { useState, useEffect } from "react";
import { Modal, Button } from "../common";
import { StackIcon } from "./LicenseIcons";
import { licenseService } from "../../services/licenseService";
import { initialEmployeesData } from "../../data/employeesData";

export default function AssignLicenseModal({
  isOpen,
  onClose,
  license,
  onAssign,
}) {
  const [employees, setEmployees] = useState([]);
  const [loadingEmployees, setLoadingEmployees] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [assignmentDate, setAssignmentDate] = useState(
    () => new Date().toISOString().split("T")[0]
  );
  const [notes, setNotes] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // Load employee directory from API on modal open
  useEffect(() => {
    let isMounted = true;
    if (isOpen) {
      setLoadingEmployees(true);
      setErrorMessage("");
      setSelectedUserId("");
      setAssignmentDate(new Date().toISOString().split("T")[0]);
      setNotes("");

      licenseService
        .getEmployeesForAssignment()
        .then((res) => {
          if (isMounted && Array.isArray(res) && res.length > 0) {
            setEmployees(
              res.map((u) => ({
                userId: u.userId,
                name: [u.firstName, u.lastName].filter(Boolean).join(" ") || `Employee #${u.userId}`,
                code: u.employeeCode || `EMP-${u.userId}`,
                role: u.designation || "Staff",
              }))
            );
          } else if (isMounted) {
            setEmployees(
              initialEmployeesData.map((e) => ({
                userId: e.id,
                name: e.employeeName,
                code: e.employeeId,
                role: e.role,
              }))
            );
          }
        })
        .catch(() => {
          if (isMounted) {
            setEmployees(
              initialEmployeesData.map((e) => ({
                userId: e.id,
                name: e.employeeName,
                code: e.employeeId,
                role: e.role,
              }))
            );
          }
        })
        .finally(() => {
          if (isMounted) setLoadingEmployees(false);
        });
    }
    return () => {
      isMounted = false;
    };
  }, [isOpen]);

  if (!isOpen || !license) return null;

  const totalSeats = license.totalQuantity ?? license.totalLicenses ?? 0;
  const assignedSeats = license.assignedQuantity ?? 0;
  const availableSeats = license.availableQuantity ?? Math.max(0, totalSeats - assignedSeats);

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setErrorMessage("");

    if (!selectedUserId) {
      setErrorMessage("Please select an employee to assign this license seat.");
      return;
    }

    if (availableSeats <= 0) {
      setErrorMessage("No seats are currently available for this license.");
      return;
    }

    const payload = {
      userId: Number(selectedUserId),
      assignedAt: assignmentDate,
      notes: notes.trim() || null,
    };

    const selectedEmp = employees.find(
      (emp) => String(emp.userId) === String(selectedUserId)
    );

    try {
      setSubmitting(true);
      await onAssign(license.licenseId, payload, selectedEmp ? selectedEmp.name : "Employee");
      onClose();
    } catch (err) {
      setErrorMessage(err.message || "Failed to assign license seat.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Assign License"
      size="md"
      footer={
        <div className="flex w-full items-center justify-end gap-3">
          <Button variant="outline" size="md" onClick={onClose}>
            Cancel
          </Button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={submitting || availableSeats <= 0}
            className="cursor-pointer rounded-xl bg-[#03312E] px-6 py-2.5 text-sm font-semibold text-white shadow-xs transition-colors hover:bg-[#02211F] disabled:cursor-not-allowed disabled:opacity-50"
          >
            {submitting ? "Assigning..." : "Assign License"}
          </button>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Top Summary Card matching Wireframe reference */}
        <div className="flex items-center gap-3.5 rounded-xl border border-[#DCE5E1] bg-[#F0F8FF] p-4">
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-[#D6EBFF] text-[#1976D2]">
            <StackIcon className="h-6 w-6" />
          </div>
          <div className="min-w-0 flex-1">
            <h4 className="truncate text-base font-bold text-[#03312E]">
              {license.licenseName}
            </h4>
            <div className="mt-0.5 flex flex-wrap items-center gap-x-4 text-xs text-[#5C7370]">
              <span>Software: <strong className="text-[#03312E]">{license.softwareName || "—"}</strong></span>
              <span>Publisher: <strong className="text-[#03312E]">{license.publisher || "—"}</strong></span>
            </div>
            <div className="mt-1 flex flex-wrap items-center gap-x-4 text-xs font-semibold text-[#03312E]">
              <span>Total Seats: <strong>{totalSeats}</strong></span>
              <span className="text-[#00C896]">Assigned: <strong>{assignedSeats}</strong></span>
              <span className="text-[#1976D2]">Available: <strong>{availableSeats}</strong></span>
            </div>
          </div>
        </div>

        {/* Error Notification */}
        {errorMessage && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs font-semibold text-red-700">
            ⚠️ {errorMessage}
          </div>
        )}

        {/* Available seats warning */}
        {availableSeats <= 0 && (
          <div className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs font-medium text-amber-800">
            ⚠️ No seats available. All {totalSeats} seats are currently assigned.
          </div>
        )}

        {/* Employee Dropdown */}
        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Employee <span className="text-red-500">*</span>
          </label>
          <div className="relative mt-1.5">
            <select
              required
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              disabled={loadingEmployees || availableSeats <= 0}
              className="w-full appearance-none rounded-xl border border-[#DCE5E1] bg-white py-2.5 pl-3.5 pr-10 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer disabled:cursor-not-allowed disabled:bg-slate-50"
            >
              <option value="">
                {loadingEmployees ? "Loading employee directory..." : "Select employee"}
              </option>
              {employees.map((emp) => (
                <option key={emp.userId} value={emp.userId}>
                  {emp.name} ({emp.code}) — {emp.role}
                </option>
              ))}
            </select>
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3.5 text-[#64748B]">
              <svg className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
          </div>
        </div>

        {/* Assignment Date */}
        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Assignment Date <span className="text-red-500">*</span>
          </label>
          <input
            type="date"
            required
            value={assignmentDate}
            onChange={(e) => setAssignmentDate(e.target.value)}
            disabled={availableSeats <= 0}
            className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer disabled:cursor-not-allowed disabled:bg-slate-50"
          />
        </div>

        {/* Notes */}
        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Notes
          </label>
          <textarea
            rows={3}
            placeholder="Additional notes (optional)"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            disabled={availableSeats <= 0}
            className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white p-3 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 disabled:cursor-not-allowed disabled:bg-slate-50"
          />
        </div>
      </form>
    </Modal>
  );
}
