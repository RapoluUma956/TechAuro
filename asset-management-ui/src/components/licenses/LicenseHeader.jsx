import React from "react";
import { PageHeader, Button } from "../common";
import { PlusIcon } from "./LicenseIcons";

export default function LicenseHeader({ onAddLicense }) {
  return (
    <PageHeader
      title="License Management"
      subtitle="Manage software licenses, seat availability, and renewal dates."
      actions={
        <Button
          variant="primary"
          icon={<PlusIcon className="h-4 w-4" />}
          onClick={onAddLicense}
        >
          Add License
        </Button>
      }
    />
  );
}
