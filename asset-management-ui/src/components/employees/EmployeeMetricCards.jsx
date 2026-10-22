import React from "react";
import { MetricCards } from "../common";
import {
  UsersIcon,
  LaptopCheckIcon,
  WrenchIcon,
  InactiveIcon,
} from "./EmployeeIcons";

export default function EmployeeMetricCards({
  totalEmployees = 0,
  activeCount = 0,
  repairCount = 0,
  inactiveCount = 0,
}) {
  const metrics = [
    {
      id: "total",
      title: "Total Employees",
      value: totalEmployees,
      caption: "Registered personnel",
      icon: UsersIcon,
      iconColor: "#1976D2",
      iconBg: "#E3F2FD",
    },
    {
      id: "active",
      title: "Active Devices",
      value: activeCount,
      caption: "Assigned & operational",
      icon: LaptopCheckIcon,
      iconColor: "#00C896",
      iconBg: "#E8F8F2",
    },
    {
      id: "repair",
      title: "In Repair",
      value: repairCount,
      caption: "Under maintenance",
      icon: WrenchIcon,
      iconColor: "#F97316",
      iconBg: "#FFF4E5",
    },
    {
      id: "inactive",
      title: "Inactive Devices",
      value: inactiveCount,
      caption: "Decommissioned / unassigned",
      icon: InactiveIcon,
      iconColor: "#EF4444",
      iconBg: "#FEECEB",
    },
  ];

  return <MetricCards items={metrics} columns={4} />;
}
