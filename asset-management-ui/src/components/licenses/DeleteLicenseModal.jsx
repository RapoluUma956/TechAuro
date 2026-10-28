import React, { useState } from "react";
import { Modal, Button } from "../common";

export default function DeleteLicenseModal({
  isOpen,
  onClose,
  license,
  onConfirmDelete,
}) {
  const [deleting, setDeleting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  if (!isOpen || !license) return null;

  const hasActiveAssignments = (license.assignedQuantity ?? 0) > 0;

  const handleDelete = async () => {
    try {
      setDeleting(true);
      setErrorMessage("");
      await onConfirmDelete(license.licenseId);
      onClose();
    } catch (err) {
      setErrorMessage(
        err.message ||
          "Failed to delete license. Please ensure all active seat assignments are released first."
      );
    } finally {
      setDeleting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Delete License"
      size="sm"
      footer={
        <div className="flex w-full items-center justify-end gap-3">
          <Button variant="outline" size="md" onClick={onClose}>
            Cancel
          </Button>
          <button
            type="button"
            onClick={handleDelete}
            disabled={deleting || hasActiveAssignments}
            className="cursor-pointer rounded-xl bg-rose-600 px-5 py-2.5 text-sm font-semibold text-white shadow-xs transition-colors hover:bg-rose-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {deleting ? "Deleting..." : "Delete License"}
          </button>
        </div>
      }
    >
      <div className="space-y-3">
        <p className="text-sm text-[#03312E]">
          Are you sure you want to permanently delete{" "}
          <strong className="font-bold text-rose-700">{license.licenseName}</strong>?
        </p>

        {hasActiveAssignments && (
          <div className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs font-medium text-amber-800">
            ⚠️ <strong>Cannot delete:</strong> This license currently has{" "}
            <strong>{license.assignedQuantity}</strong> active assigned seat(s). Please release all
            seats from the license view modal before deleting.
          </div>
        )}

        {errorMessage && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs font-semibold text-red-700">
            ⚠️ {errorMessage}
          </div>
        )}

        <p className="text-xs text-[#64748B]">
          This action will remove the license and its historical audit log from the database.
        </p>
      </div>
    </Modal>
  );
}
