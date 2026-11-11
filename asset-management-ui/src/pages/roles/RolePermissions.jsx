import React, { useState, useEffect, useCallback } from "react";
import { roleService } from "../../services/roleService";
import { usePermission } from "../../hooks/usePermission";
import {
  RoleHeader,
  RoleFilters,
  RoleTable,
  AddEditRoleModal,
  RolePermissionMatrixModal,
  DeleteRoleModal,
  AssignedEmployeesModal,
} from "../../components/roles";

export default function RolePermissions() {
  const { canWrite } = usePermission();
  const canManage = canWrite("ROLE_PERMISSIONS");

  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [toast, setToast] = useState({ message: "", type: "success" });

  // Modal states
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [roleToEdit, setRoleToEdit] = useState(null);
  const [matrixRole, setMatrixRole] = useState(null);
  const [roleToDelete, setRoleToDelete] = useState(null);
  const [viewUsersRole, setViewUsersRole] = useState(null);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => {
      setToast({ message: "", type: "success" });
    }, 4500);
  };

  const fetchRoles = useCallback(async () => {
    try {
      setLoading(true);
      const data = await roleService.getRoles(searchQuery);
      setRoles(data || []);
    } catch (err) {
      showToast(err.message || "Failed to load roles list", "error");
    } finally {
      setLoading(false);
    }
  }, [searchQuery]);

  useEffect(() => {
    fetchRoles();
  }, [fetchRoles]);

  const handlePermissionsUpdated = (roleName) => {
    showToast(`Permissions updated successfully for role "${roleName}".`);
    fetchRoles();
  };

  const handleRoleActionSuccess = (msg) => {
    showToast(msg);
    fetchRoles();
  };

  return (
    <div className="space-y-6">
      {/* Toast Alert */}
      {toast.message && (
        <div
          className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 rounded-xl px-4 py-3 text-xs font-semibold shadow-lg transition-all ${
            toast.type === "error"
              ? "bg-red-500 text-white"
              : "bg-[#03312E] text-white"
          }`}
        >
          <svg className="h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            {toast.type === "error" ? (
              <>
                <circle cx="12" cy="12" r="10" />
                <line x1="15" y1="9" x2="9" y2="15" />
                <line x1="9" y1="9" x2="15" y2="15" />
              </>
            ) : (
              <>
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </>
            )}
          </svg>
          <span>{toast.message}</span>
        </div>
      )}

      {/* Page Header */}
      <RoleHeader
        totalRoles={roles.length}
        canManage={canManage}
        onCreateRole={() => setIsAddModalOpen(true)}
      />

      {/* Search and Filters Bar */}
      <RoleFilters
        searchQuery={searchQuery}
        onSearchChange={(val) => setSearchQuery(val)}
        totalRoles={roles.length}
      />

      {/* Roles Table */}
      <RoleTable
        roles={roles}
        loading={loading}
        canManage={canManage}
        searchQuery={searchQuery}
        onConfigurePermissions={(role) => setMatrixRole(role)}
        onViewEmployees={(role) => setViewUsersRole(role)}
        onEditRole={(role) => setRoleToEdit(role)}
        onDeleteRole={(role) => setRoleToDelete(role)}
      />

      {/* Modals */}
      <AddEditRoleModal
        isOpen={isAddModalOpen || Boolean(roleToEdit)}
        onClose={() => {
          setIsAddModalOpen(false);
          setRoleToEdit(null);
        }}
        roleToEdit={roleToEdit}
        onSuccess={handleRoleActionSuccess}
      />

      <RolePermissionMatrixModal
        isOpen={Boolean(matrixRole)}
        onClose={() => setMatrixRole(null)}
        role={matrixRole}
        onPermissionsUpdated={handlePermissionsUpdated}
      />

      <DeleteRoleModal
        isOpen={Boolean(roleToDelete)}
        onClose={() => setRoleToDelete(null)}
        role={roleToDelete}
        onSuccess={handleRoleActionSuccess}
      />

      <AssignedEmployeesModal
        isOpen={Boolean(viewUsersRole)}
        onClose={() => setViewUsersRole(null)}
        role={viewUsersRole}
      />
    </div>
  );
}
