import React from "react";
import { PageHeader, Button } from "../common";
import { PlusIcon } from "../common/Icons";

export default function EmployeeHeader({ onAddEmployee }) {
  return (
    <PageHeader
      title="Employee & Devices"
      subtitle="Manage company personnel, assigned hardware assets, and device warranty status"
      actions={
        onAddEmployee ? (
          <Button
            variant="primary"
            icon={<PlusIcon className="h-4 w-4" />}
            onClick={onAddEmployee}
          >
            Add Employee
          </Button>
        ) : null
      }
    />
  );
}
