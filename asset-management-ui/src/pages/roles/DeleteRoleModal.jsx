import React, { useState } from "react";
import Modal from "../../components/common/Modal";
import Button from "../../components/common/Button";
import { roleService } from "../../services/roleService";

export default function DeleteRoleModal({
  isOpen,
  onClose,
  role,
  onSuccess,
}) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  if (!role) return null;

  const isSystemRole = Boolean(role.isSystemRole) || role.roleName === "ADMIN";
  const hasEmployeesAssigned = Number(role.assignedEmployeeCount || 0) > 0;
  const cannotDelete = isSystemRole || hasEmployeesAssigned;

  const handleDelete = async () => {
    if (cannotDelete) return;

    try {
      setLoading(true);
      setError("");
      await roleService.deleteRole(role.id);
      onSuccess && onSuccess(`Role "${role.roleName}" deleted successfully.`);
      onClose();
    } catch (err) {
      setError(err.message || "Failed to delete role.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Delete Role"
      subtitle={`Permanently remove role "${role.roleName}".`}
      size="sm"
      footer={
        <div className="flex items-center justify-end gap-2 w-full">
          <Button variant="outline" size="sm" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            variant="danger"
            size="sm"
            onClick={handleDelete}
            disabled={cannotDelete || loading}
            isLoading={loading}
          >
            Delete Role
          </Button>
        </div>
      }
    >
      <div className="space-y-3">
        {error && (
          <div className="rounded-xl bg-red-50 border border-red-200 p-3 text-xs text-red-700 flex items-center gap-2">
            <svg className="h-4 w-4 shrink-0 text-red-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{error}</span>
          </div>
        )}

        {isSystemRole ? (
          <div className="rounded-xl bg-amber-50 border border-amber-200 p-3.5 text-xs text-amber-800 flex items-start gap-2.5">
            <svg className="h-5 w-5 shrink-0 text-amber-600 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
            <div>
              <p className="font-semibold text-amber-900">Protected System Role</p>
              <p className="text-[11px] text-amber-700 mt-0.5">
                The <strong>{role.roleName}</strong> role is critical to system operations and cannot be deleted.
              </p>
            </div>
          </div>
        ) : hasEmployeesAssigned ? (
          <div className="rounded-xl bg-amber-50 border border-amber-200 p-3.5 text-xs text-amber-800 flex items-start gap-2.5">
            <svg className="h-5 w-5 shrink-0 text-amber-600 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
            <div>
              <p className="font-semibold text-amber-900">Employees Currently Assigned</p>
              <p className="text-[11px] text-amber-700 mt-0.5">
                This role is currently assigned to <strong>{role.assignedEmployeeCount} employee(s)</strong>. To delete this role, reassign those employees to another role first.
              </p>
            </div>
          </div>
        ) : (
          <p className="text-xs text-[#64748B] leading-relaxed">
            Are you sure you want to permanently delete the role <strong>{role.roleName}</strong>? All associated module permission mappings will be removed. This action cannot be undone.
          </p>
        )}
      </div>
    </Modal>
  );
}

