import React, { useState, useEffect, useCallback } from "react";
import { roleService } from "../../services/roleService";
import { usePermission } from "../../hooks/usePermission";
import Button from "../../components/common/Button";
import SearchBar from "../../components/common/SearchBar";
import AddEditRoleModal from "./AddEditRoleModal";
import RolePermissionMatrixModal from "./RolePermissionMatrixModal";
import DeleteRoleModal from "./DeleteRoleModal";
import AssignedEmployeesModal from "./AssignedEmployeesModal";

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
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-xl sm:text-2xl font-extrabold text-[#03312E] tracking-tight">
              Role Permissions
            </h1>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-[#EBF5FF] text-[#1976D2] border border-[#BFDBFE]">
              {roles.length} {roles.length === 1 ? "Role" : "Roles"}
            </span>
          </div>
          <p className="text-xs text-[#64748B] mt-1">
            Configure dynamic role-based access control, manage organizational roles, and govern page visibility.
          </p>
        </div>

        {canManage && (
          <Button
            variant="primary"
            size="md"
            onClick={() => setIsAddModalOpen(true)}
            icon={
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
            }
          >
            Create Role
          </Button>
        )}
      </div>

      {/* Search and Filters Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-[#DCE5E1] shadow-xs">
        <div className="w-full sm:w-80">
          <SearchBar
            value={searchQuery}
            onChange={(val) => setSearchQuery(val)}
            placeholder="Search roles by name or description..."
          />
        </div>
        <div className="flex items-center gap-2 text-xs text-[#64748B]">
          <span className="inline-block h-2 w-2 rounded-full bg-emerald-500" />
          <span>Database-Driven Dynamic RBAC</span>
        </div>
      </div>

      {/* Roles Table */}
      <div className="overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white shadow-xs">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-16 gap-3">
            <div className="h-8 w-8 animate-spin rounded-full border-3 border-[#1976D2] border-t-transparent" />
            <p className="text-xs text-[#64748B]">Loading organizational roles...</p>
          </div>
        ) : roles.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 px-4 text-center">
            <div className="h-12 w-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 mb-3">
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="8" y1="12" x2="16" y2="12" />
              </svg>
            </div>
            <p className="text-sm font-bold text-[#03312E]">No roles found</p>
            <p className="text-xs text-[#64748B] mt-1 max-w-sm">
              {searchQuery
                ? `No role records matched "${searchQuery}". Try a different keyword.`
                : "No roles registered in the database."}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-[#DCE5E1] text-left text-xs">
              <thead className="bg-[#F8FAF9] text-[#24211D] font-bold">
                <tr>
                  <th scope="col" className="px-5 py-3.5">Role Name</th>
                  <th scope="col" className="px-5 py-3.5">Description</th>
                  <th scope="col" className="px-5 py-3.5 text-center">Assigned Employees</th>
                  <th scope="col" className="px-5 py-3.5">Created Date</th>
                  <th scope="col" className="px-5 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#DCE5E1]">
                {roles.map((r) => {
                  const isSystem = Boolean(r.isSystemRole) || r.roleName === "ADMIN";
                  const assignedCount = Number(r.assignedEmployeeCount || 0);
                  const cannotDelete = isSystem || assignedCount > 0;

                  return (
                    <tr key={r.id} className="hover:bg-[#F5F8F7] transition-colors">
                      <td className="px-5 py-4">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-[#03312E] tracking-wide">
                            {r.roleName}
                          </span>
                          {isSystem && (
                            <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200">
                              System
                            </span>
                          )}
                        </div>
                      </td>

                      <td className="px-5 py-4 text-[#64748B] max-w-xs truncate">
                        {r.description || <span className="italic text-slate-400">No description provided</span>}
                      </td>

                      <td className="px-5 py-4 text-center">
                        <button
                          type="button"
                          onClick={() => setViewUsersRole(r)}
                          title={`View ${assignedCount} assigned employee(s)`}
                          className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold cursor-pointer transition-all hover:scale-105 ${
                            assignedCount > 0
                              ? "bg-emerald-50 text-emerald-800 border border-emerald-200 hover:bg-emerald-100"
                              : "bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200"
                          }`}
                        >
                          <svg className="h-3 w-3 shrink-0 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                            <circle cx="9" cy="7" r="4" />
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                          </svg>
                          <span>{assignedCount} {assignedCount === 1 ? "Employee" : "Employees"}</span>
                        </button>
                      </td>

                      <td className="px-5 py-4 text-[#64748B]">
                        {r.createdAt ? new Date(r.createdAt).toLocaleDateString("en-US", {
                          year: "numeric",
                          month: "short",
                          day: "numeric",
                        }) : "—"}
                      </td>

                      <td className="px-5 py-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {/* Configure Permissions Matrix Button */}
                          <button
                            type="button"
                            onClick={() => setMatrixRole(r)}
                            title="Configure Permissions"
                            className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg border border-[#DCE5E1] bg-white text-[#1976D2] hover:bg-[#EBF5FF] hover:border-[#1976D2] font-semibold text-xs shadow-2xs transition-all cursor-pointer"
                          >
                            <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <rect width="18" height="11" x="3" y="11" rx="2" ry="2" />
                              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                            </svg>
                            <span>Permissions</span>
                          </button>

                          {/* View Assigned Users Button */}
                          <button
                            type="button"
                            onClick={() => setViewUsersRole(r)}
                            title={`View ${assignedCount} assigned employee(s)`}
                            className="p-1.5 rounded-lg text-[#64748B] hover:text-[#1976D2] hover:bg-[#EBF5FF] transition-colors cursor-pointer"
                          >
                            <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                              <circle cx="9" cy="7" r="4" />
                              <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                            </svg>
                          </button>

                          {/* Edit Role Button */}
                          {canManage && (
                            <button
                              type="button"
                              onClick={() => setRoleToEdit(r)}
                              title="Edit Role"
                              className="p-1.5 rounded-lg text-[#64748B] hover:text-[#03312E] hover:bg-slate-100 transition-colors cursor-pointer"
                            >
                              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                              </svg>
                            </button>
                          )}

                          {/* Delete Role Button */}
                          {canManage && (
                            <button
                              type="button"
                              onClick={() => setRoleToDelete(r)}
                              disabled={cannotDelete}
                              title={
                                isSystem
                                  ? "System roles cannot be deleted"
                                  : assignedCount > 0
                                  ? `Cannot delete role with ${assignedCount} assigned employee(s)`
                                  : "Delete Role"
                              }
                              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                                cannotDelete
                                  ? "text-slate-300 cursor-not-allowed"
                                  : "text-red-500 hover:text-red-700 hover:bg-red-50"
                              }`}
                            >
                              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <polyline points="3 6 5 6 21 6" />
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                              </svg>
                            </button>
                          )}
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

