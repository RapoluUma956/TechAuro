import React, { useState } from "react";
import { Modal, Button } from "../common";

export default function DeleteLaptopModal({
  isOpen,
  onClose,
  laptop,
  onConfirm,
}) {
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  if (!isOpen || !laptop) return null;

  const handleConfirm = async () => {
    try {
      setSubmitting(true);
      setErrorMessage("");
      await onConfirm(laptop.deviceId);
      onClose();
    } catch (err) {
      console.error("Laptop deletion failed:", err);
      setErrorMessage(
        err.message || "Failed to delete laptop. Please check permissions."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      closeOnBackdrop={false}
      title="Delete Laptop Device"
      subtitle="Confirm deletion of laptop hardware record from the system registry"
      size="md"
      footer={
        <div className="flex w-full items-center justify-end gap-3">
          <Button
            variant="outline"
            size="md"
            onClick={onClose}
            disabled={submitting}
          >
            Cancel
          </Button>
          <Button
            variant="danger"
            size="md"
            onClick={handleConfirm}
            isLoading={submitting}
          >
            {submitting ? "Deleting..." : "Delete Laptop"}
          </Button>
        </div>
      }
    >
      <div className="space-y-4">
        {errorMessage && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3.5 text-xs text-red-700 font-medium">
            {errorMessage}
          </div>
        )}

        <div className="flex items-start gap-3.5 rounded-2xl border border-red-200 bg-red-50/80 p-4">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-red-100 text-red-600">
            <svg
              className="h-5 w-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
          </div>
          <div>
            <h4 className="text-sm font-bold text-red-900">
              Are you sure you want to permanently delete this laptop?
            </h4>
            <p className="mt-1 text-xs text-red-700 leading-relaxed">
              This will remove the device record, historical assignments, and telemetry link from the system.
            </p>
          </div>
        </div>

        <div className="rounded-2xl border border-[#DCE5E1] bg-[#F8FAF9] p-4 text-xs space-y-2">
          <div className="flex items-center justify-between">
            <span className="font-bold text-[#03312E] text-sm">
              {[laptop.brand, laptop.model].filter(Boolean).join(" ") || "Laptop Device"}
            </span>
            <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded-md bg-white border border-[#DCE5E1] text-[#1976D2]">
              SN: {laptop.serialNumber}
            </span>
          </div>

          {laptop.assetTag && (
            <div className="text-[#64748B]">
              Asset Tag: <span className="font-mono font-medium text-[#03312E]">{laptop.assetTag}</span>
            </div>
          )}

          {laptop.assignmentStatus === "ASSIGNED" && (
            <div className="mt-2 rounded-xl border border-amber-200 bg-amber-50 p-2.5 text-amber-900">
              <span className="font-semibold">⚠️ Active Assignment:</span>{" "}
              This device is currently assigned {laptop.assignedUserName ? `to ${laptop.assignedUserName}` : ""}. Deleting will terminate the active assignment.
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
}

