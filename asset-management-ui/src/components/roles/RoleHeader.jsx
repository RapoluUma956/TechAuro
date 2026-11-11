import React from "react";
import { PageHeader, Button } from "../common";
import { PlusIcon } from "../common/Icons";

export default function RoleHeader({
  totalRoles = 0,
  canManage = false,
  onCreateRole,
}) {
  return (
    <PageHeader
      title="Role Permissions"
      subtitle="Configure dynamic role-based access control, manage organizational roles, and govern page visibility."
      badge={
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-[#EBF5FF] text-[#1976D2] border border-[#BFDBFE]">
          {totalRoles} {totalRoles === 1 ? "Role" : "Roles"}
        </span>
      }
      actions={
        canManage && onCreateRole ? (
          <Button
            variant="primary"
            icon={<PlusIcon className="h-4 w-4" />}
            onClick={onCreateRole}
          >
            Create Role
          </Button>
        ) : null
      }
    />
  );
}

