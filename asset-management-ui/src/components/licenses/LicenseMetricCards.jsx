import React from "react";
import { MetricCards } from "../common";
import {
  StackIcon,
  UsersIcon,
  UserSingleIcon,
  CheckCircleIcon,
  ClockAlertIcon,
} from "../common/Icons";

export default function LicenseMetricCards({ metrics, loading = false }) {
  const cards = [
    {
      id: "totalLicenses",
      title: "Total Licenses",
      value: loading ? "—" : (metrics?.totalLicenses ?? 0),
      caption: "Active software contracts",
      icon: StackIcon,
      iconColor: "#1976D2",
      iconBg: "#E3F2FD",
    },
    {
      id: "totalSeats",
      title: "Total Seats",
      value: loading ? "—" : (metrics?.totalSeats ?? 0),
      caption: "Purchased seat capacity",
      icon: UsersIcon,
      iconColor: "#00C896",
      iconBg: "#E6F7F2",
    },
    {
      id: "assignedSeats",
      title: "Assigned Seats",
      value: loading ? "—" : (metrics?.assignedSeats ?? 0),
      caption: "Allocated to staff",
      icon: UserSingleIcon,
      iconColor: "#10B981",
      iconBg: "#E8F8F2",
    },
    {
      id: "availableSeats",
      title: "Available Seats",
      value: loading ? "—" : (metrics?.availableSeats ?? 0),
      caption: "Ready for deployment",
      icon: CheckCircleIcon,
      iconColor: "#F59E0B",
      iconBg: "#FEF3C7",
    },
    {
      id: "expiringSoon",
      title: "Expiring Soon",
      value: loading ? "—" : (metrics?.expiringSoonCount ?? 0),
      caption: "Within 30 days",
      icon: ClockAlertIcon,
      iconColor: "#EF4444",
      iconBg: "#FEE2E2",
    },
  ];

  return <MetricCards items={cards} columns={5} />;
}
