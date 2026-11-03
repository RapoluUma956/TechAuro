import React, { useState, useEffect } from "react";
import Modal from "../../components/common/Modal";
import Button from "../../components/common/Button";
import { roleService } from "../../services/roleService";

export default function RolePermissionMatrixModal({
  isOpen,
  onClose,
  role,
  onPermissionsUpdated,
}) {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [permissions, setPermissions] = useState([]);
  const [error, setError] = useState("");
  const [hasChanges, setHasChanges] = useState(false);

  useEffect(() => {
    if (isOpen && role) {
      fetchRolePermissions();
    } else {
      setPermissions([]);
      setError("");
      setHasChanges(false);
    }
  }, [isOpen, role]);

  const fetchRolePermissions = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await roleService.getRolePermissions(role.id);
      setPermissions(data?.permissions || []);
      setHasChanges(false);
    } catch (err) {
      setError(err.message || "Failed to load role permissions matrix.");
    } finally {
      setLoading(false);
    }
  };

  const handleLevelChange = (permissionId, newLevel) => {
    if (role?.roleName === "ADMIN" || role?.isSystemRole) {
      return; // Admin permissions cannot be modified
    }

    setPermissions((prev) =>
      prev.map((p) =>
        p.permissionId === permissionId ? { ...p, accessLevel: newLevel } : p
      )
    );
    setHasChanges(true);
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError("");
      const payload = permissions.map((p) => ({
        permissionId: p.permissionId,
        accessLevel: p.accessLevel,
      }));

      await roleService.updateRolePermissions(role.id, payload);
      onPermissionsUpdated && onPermissionsUpdated(role.roleName);
      onClose();
    } catch (err) {
      setError(err.message || "Failed to save role permissions.");
    } finally {
      setSaving(false);
    }
  };

  const isAdminRole = role?.roleName === "ADMIN" || role?.isSystemRole;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Configure Permissions: ${role?.roleName || ""}`}
      subtitle="Define module-level access and action privileges for this role."
      size="lg"
      footer={
        <div className="flex items-center justify-between w-full">
          <p className="text-xs text-[#64748B]">
            {isAdminRole
              ? "System Admin role retains permanent write permissions across all modules."
              : hasChanges
              ? "You have unsaved permission modifications."
              : "All changes are saved."}
          </p>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={onClose} disabled={saving}>
              Cancel
            </Button>
            <Button
              variant="primary"
              size="sm"
              onClick={handleSave}
              disabled={isAdminRole || !hasChanges || saving}
              isLoading={saving}
            >
              Save Permissions
            </Button>
          </div>
        </div>
      }
    >
      <div className="space-y-4">
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

        {isAdminRole && (
          <div className="rounded-xl bg-blue-50 border border-blue-200 p-3 text-xs text-blue-800 flex items-start gap-2.5">
            <svg className="h-4 w-4 shrink-0 text-[#1976D2] mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <div>
              <p className="font-semibold text-[#1976D2]">Protected System Role</p>
              <p className="text-[11px] text-blue-700 mt-0.5">
                The ADMIN role always retains full WRITE access across all application modules to ensure uninterrupted administrative control.
              </p>
            </div>
          </div>
        )}

        {loading ? (
          <div className="flex flex-col items-center justify-center py-12 gap-3">
            <div className="h-8 w-8 animate-spin rounded-full border-3 border-[#1976D2] border-t-transparent" />
            <p className="text-xs text-[#64748B]">Loading permission matrix...</p>
          </div>
        ) : (
          <div className="overflow-hidden rounded-xl border border-[#DCE5E1]">
            <table className="min-w-full divide-y divide-[#DCE5E1] text-left text-xs">
              <thead className="bg-[#F8FAF9] text-[#24211D] font-bold">
                <tr>
                  <th scope="col" className="px-4 py-3">Module</th>
                  <th scope="col" className="px-4 py-3">Description</th>
                  <th scope="col" className="px-4 py-3 text-center w-56">Access Level</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#DCE5E1] bg-white">
                {permissions.map((p) => {
                  const currentLevel = p.accessLevel || "HIDE";
                  return (
                    <tr key={p.permissionId} className="hover:bg-[#F5F8F7] transition-colors">
                      <td className="px-4 py-3 font-semibold text-[#03312E]">
                        {p.moduleName || p.permissionCode}
                      </td>
                      <td className="px-4 py-3 text-[#64748B] text-[11px] max-w-xs">
                        {p.description || "Module access and action controls"}
                      </td>
                      <td className="px-4 py-3">
                        <div className="inline-flex rounded-lg border border-[#DCE5E1] p-0.5 bg-[#F1F5F9] w-full justify-center">
                          {["HIDE", "READ", "WRITE"].map((level) => {
                            const isSelected = currentLevel === level;
                            let activeClass = "";
                            if (isSelected) {
                              if (level === "HIDE") {
                                activeClass = "bg-white text-slate-800 shadow-xs font-bold border border-slate-300";
                              } else if (level === "READ") {
                                activeClass = "bg-[#1976D2] text-white shadow-xs font-bold";
                              } else if (level === "WRITE") {
                                activeClass = "bg-[#00C896] text-white shadow-xs font-bold";
                              }
                            } else {
                              activeClass = "text-[#64748B] hover:text-[#03312E]";
                            }

                            return (
                              <button
                                key={level}
                                type="button"
                                disabled={isAdminRole}
                                onClick={() => handleLevelChange(p.permissionId, level)}
                                className={`flex-1 px-2.5 py-1 text-[11px] rounded-md transition-all cursor-pointer disabled:cursor-not-allowed disabled:opacity-70 ${activeClass}`}
                              >
                                {level}
                              </button>
                            );
                          })}
                        </div>
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

