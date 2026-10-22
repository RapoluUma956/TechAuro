export const notificationsHeaderData = {
  title: "Notifications",
  subtitle: "System alerts, device security events, and compliance triggers.",
};

export const allNotificationsData = [
  {
    id: 1,
    title: "High CPU usage detected",
    description: "Laptop EMP001 exceeding 95% threshold for 30 minutes",
    time: "2 hours ago",
    category: "Performance",
    severity: "warning",
    read: false,
  },
  {
    id: 2,
    title: "Critical vulnerability CVE-2026-1402 found",
    description: "Laptop LAP-IT-024 requires urgent OS security update",
    time: "4 hours ago",
    category: "Security",
    severity: "critical",
    read: false,
  },
  {
    id: 3,
    title: "Device disconnected",
    description: "Laptop EMP032 offline for more than 7 consecutive days",
    time: "6 hours ago",
    category: "Connectivity",
    severity: "info",
    read: true,
  },
  {
    id: 4,
    title: "License renewal reminder",
    description: "JetBrains All Products license expiring in 30 days",
    time: "1 day ago",
    category: "License",
    severity: "warning",
    read: true,
  },
  {
    id: 5,
    title: "New accessory requested",
    description: "Alex Smith requested CalDigit TS4 Dock",
    time: "2 days ago",
    category: "Inventory",
    severity: "info",
    read: true,
  },
];
