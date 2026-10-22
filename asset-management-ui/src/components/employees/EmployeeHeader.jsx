import React from "react";
import { PageHeader, Button } from "../common";
import { PlusIcon } from "./EmployeeIcons";

export default function EmployeeHeader({ onAddEmployee }) {
  return (
    <PageHeader
      title="Employee & Devices"
      subtitle="Manage company personnel, assigned hardware assets, and device warranty status"
      actions={
        <Button
          variant="primary"
          icon={<PlusIcon className="h-4 w-4" />}
          onClick={onAddEmployee}
        >
          Add Employee
        </Button>
      }
    />
  );
}
