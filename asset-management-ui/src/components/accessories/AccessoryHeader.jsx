import React from "react";
import { PageHeader, Button } from "../common";
import { PlusIcon } from "./AccessoryIcons";

export default function AccessoryHeader({ onAddAccessory }) {
  return (
    <PageHeader
      title="Accessories & Hardware"
      subtitle="Track peripheral inventory, stocks, warranty details, and hardware asset allocation"
      actions={
        <Button
          variant="primary"
          icon={<PlusIcon className="h-4 w-4" />}
          onClick={onAddAccessory}
        >
          Add Accessory
        </Button>
      }
    />
  );
}
