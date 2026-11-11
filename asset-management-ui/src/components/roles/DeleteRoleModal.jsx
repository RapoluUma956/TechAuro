import React, { useState } from "react";
import { Modal, Button } from "../common";
import { roleService } from "../../services/roleService";
import { AlertTriangleIcon } from "../common/Icons";

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
            <AlertTriangleIcon className="h-4 w-4 shrink-0 text-red-500" />
            <span>{error}</span>
          </div>
        )}

        {isSystemRole ? (
          <div className="rounded-xl bg-amber-50 border border-amber-200 p-3.5 text-xs text-amber-800 flex items-start gap-2.5">
            <AlertTriangleIcon className="h-5 w-5 shrink-0 text-amber-600 mt-0.5" />
            <div>
              <p className="font-semibold text-sm text-amber-900">Protected System Role</p>
              <p className="text-xs text-amber-700 mt-0.5">
                The <strong>{role.roleName}</strong> role is critical to system operations and cannot be deleted.
              </p>
            </div>
          </div>
        ) : hasEmployeesAssigned ? (
          <div className="rounded-xl bg-amber-50 border border-amber-200 p-3.5 text-xs text-amber-800 flex items-start gap-2.5">
            <AlertTriangleIcon className="h-5 w-5 shrink-0 text-amber-600 mt-0.5" />
            <div>
              <p className="font-semibold text-sm text-amber-900">Employees Currently Assigned</p>
              <p className="text-xs text-amber-700 mt-0.5">
                This role is currently assigned to <strong>{role.assignedEmployeeCount} employee(s)</strong>. To delete this role, reassign those employees to another role first.
              </p>
            </div>
          </div>
        ) : (
          <p className="text-sm text-[#5C7370] leading-relaxed">
            Are you sure you want to delete the role{" "}
            <strong className="text-[#03312E]">{role.roleName}</strong>? All associated permission mappings will be removed. This action cannot be undone.
          </p>
        )}
      </div>
    </Modal>
  );
}

