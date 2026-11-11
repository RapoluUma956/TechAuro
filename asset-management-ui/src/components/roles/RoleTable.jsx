import React from "react";
import { DataTable } from "../common";
import { ShieldLockIcon, UsersIcon, EditIcon, TrashIcon } from "../common/Icons";

export default function RoleTable({
  roles = [],
  loading = false,
  canManage = false,
  searchQuery = "",
  onConfigurePermissions,
  onViewEmployees,
  onEditRole,
  onDeleteRole,
}) {
  const columns = [
    {
      key: "roleName",
      header: "Role Name",
      render: (r) => {
        const isSystem = Boolean(r.isSystemRole) || r.roleName === "ADMIN";
        return (
          <div className="flex items-center gap-2">
            <span className="font-bold text-sm text-[#03312E] tracking-wide">
              {r.roleName}
            </span>
            {isSystem && (
              <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200">
                System
              </span>
            )}
          </div>
        );
      },
    },
    {
      key: "description",
      header: "Description",
      render: (r) => (
        <span className="text-sm font-medium text-[#334155] max-w-xs block truncate">
          {r.description || <span className="italic text-slate-400">No description provided</span>}
        </span>
      ),
    },
    {
      key: "assignedEmployees",
      header: "Assigned Employees",
      align: "center",
      render: (r) => {
        const assignedCount = Number(r.assignedEmployeeCount || 0);
        return (
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onViewEmployees && onViewEmployees(r);
            }}
            title={`View ${assignedCount} assigned employee(s)`}
            className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold cursor-pointer transition-all hover:scale-105 ${
              assignedCount > 0
                ? "bg-emerald-50 text-emerald-800 border border-emerald-200 hover:bg-emerald-100"
                : "bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200"
            }`}
          >
            <UsersIcon className="h-3.5 w-3.5 shrink-0 text-emerald-600" />
            <span>{assignedCount} {assignedCount === 1 ? "Employee" : "Employees"}</span>
          </button>
        );
      },
    },
    {
      key: "createdAt",
      header: "Created Date",
      render: (r) => (
        <span className="text-xs font-medium text-[#5C7370]">
          {r.createdAt
            ? new Date(r.createdAt).toLocaleDateString("en-GB", {
                day: "2-digit",
                month: "short",
                year: "numeric",
              })
            : "—"}
        </span>
      ),
    },
    {
      key: "actions",
      header: "Actions",
      align: "right",
      render: (r) => {
        const isSystem = Boolean(r.isSystemRole) || r.roleName === "ADMIN";
        const assignedCount = Number(r.assignedEmployeeCount || 0);
        const cannotDelete = isSystem || assignedCount > 0;

        return (
          <div
            className="flex items-center justify-end gap-1"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Configure Permissions Matrix Button */}
            <button
              type="button"
              onClick={() => onConfigurePermissions && onConfigurePermissions(r)}
              title="Configure Permissions"
              className="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg border border-[#DCE5E1] bg-white text-[#1976D2] hover:bg-[#EBF5FF] hover:border-[#1976D2] font-semibold text-xs shadow-2xs transition-all cursor-pointer"
            >
              <ShieldLockIcon className="h-3.5 w-3.5" />
              <span>Permissions</span>
            </button>

            {/* View Assigned Users Button */}
            <button
              type="button"
              onClick={() => onViewEmployees && onViewEmployees(r)}
              title={`View ${assignedCount} assigned employee(s)`}
              className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-sky-50 hover:text-[#1976D2] cursor-pointer"
            >
              <UsersIcon className="h-4 w-4" />
            </button>

            {/* Edit Role Button */}
            {canManage && (
              <button
                type="button"
                onClick={() => onEditRole && onEditRole(r)}
                title="Edit Role"
                className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-yellow-50 hover:text-[#EAB308] cursor-pointer"
              >
                <EditIcon className="h-4 w-4" />
              </button>
            )}

            {/* Delete Role Button */}
            {canManage && (
              <button
                type="button"
                onClick={() => onDeleteRole && onDeleteRole(r)}
                disabled={cannotDelete}
                title={
                  isSystem
                    ? "System roles cannot be deleted"
                    : assignedCount > 0
                    ? `Cannot delete role with ${assignedCount} assigned employee(s)`
                    : "Delete Role"
                }
                className={`rounded-lg p-1.5 transition-colors ${
                  cannotDelete
                    ? "cursor-not-allowed text-slate-300 opacity-40"
                    : "text-[#64748B] hover:bg-rose-50 hover:text-[#EF4444] cursor-pointer"
                }`}
              >
                <TrashIcon className="h-4 w-4" />
              </button>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <DataTable
      data={roles}
      columns={columns}
      keyField="id"
      showSerialNo={true}
      serialNoHeader="#"
      loading={loading}
      pagination={roles.length > 10}
      pageSize={10}
      itemLabel="roles"
      emptyMessage="No roles found"
      emptySubtitle={
        searchQuery
          ? `No role records matched "${searchQuery}". Try a different keyword.`
          : "No roles registered in the database."
      }
    />
  );
}
