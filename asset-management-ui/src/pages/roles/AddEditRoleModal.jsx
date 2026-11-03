import React, { useState, useEffect } from "react";
import Modal from "../../components/common/Modal";
import Button from "../../components/common/Button";
import { roleService } from "../../services/roleService";

export default function AddEditRoleModal({
  isOpen,
  onClose,
  roleToEdit = null,
  onSuccess,
}) {
  const isEditing = Boolean(roleToEdit);

  const [roleName, setRoleName] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isOpen) {
      if (roleToEdit) {
        setRoleName(roleToEdit.roleName || "");
        setDescription(roleToEdit.description || "");
      } else {
        setRoleName("");
        setDescription("");
      }
      setError("");
    }
  }, [isOpen, roleToEdit]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formattedName = roleName.trim().toUpperCase();

    if (!formattedName) {
      setError("Role name is required.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      if (isEditing) {
        await roleService.updateRole(roleToEdit.id, {
          roleName: formattedName,
          description: description.trim(),
        });
        onSuccess && onSuccess(`Role "${formattedName}" updated successfully.`);
      } else {
        await roleService.createRole({
          roleName: formattedName,
          description: description.trim(),
        });
        onSuccess && onSuccess(`Role "${formattedName}" created successfully with default permissions.`);
      }

      onClose();
    } catch (err) {
      setError(err.message || "Failed to save role. Please check input.");
    } finally {
      setLoading(false);
    }
  };

  const isSystem = Boolean(roleToEdit?.isSystemRole);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? `Edit Role: ${roleToEdit.roleName}` : "Create New Role"}
      subtitle={
        isEditing
          ? "Update the role name and description."
          : "Define a new organizational role with customizable module access."
      }
      size="md"
      footer={
        <div className="flex items-center justify-end gap-2 w-full">
          <Button variant="outline" size="sm" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={handleSubmit}
            isLoading={loading}
          >
            {isEditing ? "Save Changes" : "Create Role"}
          </Button>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
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

        <div>
          <label className="block text-xs font-bold text-[#03312E] uppercase tracking-wider mb-1.5">
            Role Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            required
            disabled={isSystem || loading}
            value={roleName}
            onChange={(e) => setRoleName(e.target.value.toUpperCase())}
            placeholder="e.g. IT_SUPPORT, AUDITOR, MANAGER"
            className="w-full rounded-xl border border-[#DCE5E1] bg-white px-3.5 py-2.5 text-xs text-[#24211D] placeholder-slate-400 focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 disabled:bg-[#F1F5F9] disabled:cursor-not-allowed font-medium tracking-wide uppercase"
          />
          {isSystem && (
            <p className="text-[11px] text-[#64748B] mt-1">
              System role names are protected and cannot be changed.
            </p>
          )}
        </div>

        <div>
          <label className="block text-xs font-bold text-[#03312E] uppercase tracking-wider mb-1.5">
            Description
          </label>
          <textarea
            rows={3}
            disabled={loading}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Describe what responsibilities and privileges this role has..."
            className="w-full rounded-xl border border-[#DCE5E1] bg-white px-3.5 py-2.5 text-xs text-[#24211D] placeholder-slate-400 focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 font-normal resize-none"
          />
        </div>

        {!isEditing && (
          <div className="rounded-xl bg-slate-50 border border-slate-200 p-3 text-[11px] text-[#64748B] flex items-center gap-2">
            <svg className="h-4 w-4 shrink-0 text-[#1976D2]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <span>
              New roles are created with all module permissions defaulted to <strong>HIDE</strong>. You can configure specific READ/WRITE access immediately after creation.
            </span>
          </div>
        )}
      </form>
    </Modal>
  );
}

