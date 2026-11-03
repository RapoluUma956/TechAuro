import React, { useState, useEffect } from "react";
import { Modal, Button } from "../common";
import { StackIcon } from "./LicenseIcons";
import { employeeService } from "../../services/employeeService";

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

      employeeService
        .getEmployees({ size: 100 })
        .then((res) => {
          if (!isMounted) return;
          const list = res?.content || (Array.isArray(res) ? res : []);
          setEmployees(
            list.map((u) => ({
              userId: u.employeeId || u.userId || u.id,
              name:
                u.fullName ||
                [u.firstName, u.lastName].filter(Boolean).join(" ") ||
                `Employee #${u.employeeId}`,
              code: u.employeeCode || `EMP-${u.employeeId}`,
              role: u.designation || u.role || "Staff",
            }))
          );
        })
        .catch(() => {
          if (isMounted) {
            setEmployees([]);
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

  const totalSeats = license.totalQuantity ?? 0;
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
      setErrorMessage("No available seats left for this license.");
      return;
    }

    const selectedEmp = employees.find((emp) => String(emp.userId) === String(selectedUserId));

    try {
      setSubmitting(true);
      await onAssign(
        license.licenseId,
        {
          userId: Number(selectedUserId),
          assignedDate: assignmentDate,
          notes: notes.trim(),
        },
        selectedEmp ? selectedEmp.name : "Employee"
      );
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
      title="Assign License Seat"
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
            className="cursor-pointer rounded-xl bg-[#03312E] px-5 py-2.5 text-sm font-semibold text-white shadow-xs transition-colors hover:bg-[#02211F] disabled:cursor-not-allowed disabled:opacity-50"
          >
            {submitting ? "Assigning..." : "Confirm Seat Assignment"}
          </button>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Top License Summary Banner */}
        <div className="flex items-center gap-3.5 rounded-xl border border-[#DCE5E1] bg-[#F0F8FF] p-4">
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-[#D6EBFF] text-[#1976D2]">
            <StackIcon className="h-6 w-6" />
          </div>
          <div className="min-w-0 flex-1">
            <h4 className="truncate text-sm font-bold text-[#03312E] sm:text-base">
              {license.licenseName}
            </h4>
            <div className="mt-0.5 flex flex-wrap items-center gap-x-4 text-xs text-[#5C7370]">
              <span>Software: <strong className="text-[#03312E]">{license.softwareName}</strong></span>
              <span>Publisher: <strong className="text-[#03312E]">{license.publisher}</strong></span>
            </div>
            <div className="mt-1 flex flex-wrap items-center gap-x-4 text-xs font-semibold text-[#03312E]">
              <span>Total Seats: <strong>{totalSeats}</strong></span>
              <span className="text-[#00C896]">Assigned: <strong>{assignedSeats}</strong></span>
              <span className="text-[#1976D2]">Available: <strong>{availableSeats}</strong></span>
            </div>
          </div>
        </div>

        {/* Error notification */}
        {errorMessage && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs font-medium text-red-700">
            {errorMessage}
          </div>
        )}

        {/* Available Seats Warning */}
        {availableSeats <= 0 && (
          <div className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs font-medium text-amber-800">
            ⚠️ This license has reached maximum allocation (0 seats remaining). You must purchase additional seats or release an existing assignment before assigning.
          </div>
        )}

        {/* Employee Select */}
        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Employee <span className="text-red-500">*</span>
          </label>
          <div className="relative mt-1.5">
            <select
              required
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              disabled={loadingEmployees}
              className="w-full appearance-none rounded-xl border border-[#DCE5E1] bg-white py-2.5 pl-3.5 pr-10 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
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
            className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
          />
        </div>

        {/* Notes Textarea */}
        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Notes
          </label>
          <textarea
            rows={3}
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Usage context, seat justification, or department bill code"
            className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white p-3 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
          />
        </div>
      </form>
    </Modal>
  );
}
