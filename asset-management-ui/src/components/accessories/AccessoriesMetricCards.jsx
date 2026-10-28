import React from "react";
import { MetricCards } from "../common";
import {
  AccessoryPackageIcon,
  UserCheckIcon,
  WarehouseBoxIcon,
  WrenchIcon,
} from "./AccessoryIcons";

export default function AccessoriesMetricCards({
  metrics,
  loading = false,
}) {
  const cards = [
    {
      id: "total",
      title: "Total Accessories",
      value: loading ? "—" : (metrics?.totalAccessories ?? 0),
      caption: "Catalog inventory & devices",
      icon: AccessoryPackageIcon,
      iconColor: "#1976D2",
      iconBg: "#E3F2FD",
    },
    {
      id: "assigned",
      title: "Assigned Accessories",
      value: loading ? "—" : (metrics?.assignedAccessories ?? 0),
      caption: "Allocated to employees",
      icon: UserCheckIcon,
      iconColor: "#00C896",
      iconBg: "#E8F8F2",
    },
    {
      id: "unassigned",
      title: "Unassigned Accessories",
      value: loading ? "—" : (metrics?.unassignedAccessories ?? 0),
      caption: "Available in warehouse",
      icon: WarehouseBoxIcon,
      iconColor: "#3B82F6",
      iconBg: "#EBF5FF",
    },
    {
      id: "repair",
      title: "Under Repair",
      value: loading ? "—" : (metrics?.repairAccessories ?? 0),
      caption: "Maintenance & service",
      icon: WrenchIcon,
      iconColor: "#F97316",
      iconBg: "#FFF4E5",
    },
  ];

  return <MetricCards items={cards} columns={4} />;
}
