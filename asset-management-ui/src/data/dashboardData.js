/**
 * Dashboard Header & User Info Constants
 */
export const dashboardHeaderData = {
  title: "Dashboard",
  subtitle: "Overview of your IT infrastructure and security posture",
  lastUpdated: "Apr 26, 2025 10:24 AM",
  searchPlaceholder: "Search assets, employees, devices...",
  unreadNotificationCount: 3,
  userProfile: {
    name: "John Doe",
    role: "IT Administrator",
    initials: "JD",
  },
};

/**
 * Top 4 Metric Summary Cards
 */
export const dashboardStatistics = [
  {
    id: "total-assets",
    title: "Total Assets",
    value: 248,
    caption: "Managed devices",
    icon: "laptop",
    iconBg: "#EBF5FF",
    iconColor: "#1976D2",
    link: "/employees",
  },
  {
    id: "online-assets",
    title: "Online Assets",
    value: 231,
    caption: "Devices connected",
    icon: "wifi",
    iconBg: "#E6F4EA",
    iconColor: "#00C896",
    link: "/dashboard",
  },
  {
    id: "open-vulnerabilities",
    title: "Open Vulnerabilities",
    value: 18,
    caption: "Require review",
    icon: "shield-alert",
    iconBg: "#FEE2E2",
    iconColor: "#DC2626",
    link: "/notifications",
  },
  {
    id: "license-expiry",
    title: "License Expiry",
    value: 6,
    caption: "Expiring soon",
    icon: "key",
    iconBg: "#FEF3C7",
    iconColor: "#D97706",
    link: "/licenses",
  },
];

/**
 * Device Connectivity Section
 */
export const deviceConnectivityData = {
  title: "Device Connectivity",
  total: 248,
  online: {
    count: 231,
    percentage: 93,
    color: "var(--color-green)",
  },
  offline: {
    count: 17,
    percentage: 7,
    color: "var(--color-red)",
  },
};

/**
 * Asset Health Section
 */
export const assetHealthData = {
  title: "Asset Health",
  items: [
    {
      id: "healthy",
      label: "Healthy",
      count: 198,
      percentage: 80,
      color: "var(--color-green)",
      dotColor: "var(--color-green)",
    },
    {
      id: "warning",
      label: "Warning",
      count: 35,
      percentage: 14,
      color: "var(--color-yellow)",
      dotColor: "var(--color-yellow)",
    },
    {
      id: "critical",
      label: "Critical",
      count: 15,
      percentage: 6,
      color: "var(--color-red)",
      dotColor: "var(--color-red)",
    },
  ],
};

/**
 * Open Vulnerabilities by Severity (Donut Chart)
 */
export const vulnerabilitiesBySeverityData = {
  title: "Open Vulnerabilities by Severity",
  total: 18,
  centerLabel: "Total",
  segments: [
    {
      id: "critical",
      label: "Critical",
      count: 3,
      percentage: 17,
      color: "var(--color-red)",
    },
    {
      id: "high",
      label: "High",
      count: 7,
      percentage: 39,
      color: "var(--color-orange)",
    },
    {
      id: "medium",
      label: "Medium",
      count: 5,
      percentage: 28,
      color: "var(--color-yellow)",
    },
    {
      id: "low",
      label: "Low",
      count: 3,
      percentage: 17,
      color: "var(--color-blue)",
    },
  ],
};

/**
 * License Expiry Overview (Donut Chart)
 */
export const licenseExpiryOverviewData = {
  title: "License Expiry Overview",
  total: 6,
  centerLabel: "Expiring Soon",
  segments: [
    {
      id: "expired",
      label: "Expired",
      count: 2,
      percentage: 33,
      color: "var(--color-red)",
    },
    {
      id: "expiring-7-days",
      label: "Expiring (≤ 7 days)",
      count: 2,
      percentage: 33,
      color: "var(--color-orange)",
    },
    {
      id: "expiring-30-days",
      label: "Expiring (≤ 30 days)",
      count: 1,
      percentage: 17,
      color: "var(--color-yellow)",
    },
    {
      id: "active",
      label: "Active",
      count: 1,
      percentage: 17,
      color: "var(--color-green)",
    },
  ],
};

/**
 * Quick Actions Data
 */
export const quickActionsData = {
  title: "Quick Actions",
  items: [
    {
      id: "view-assets",
      label: "View Assets",
      icon: "laptop",
      iconBg: "#EBF5FF",
      iconColor: "#1976D2",
      link: "/employees",
    },
    {
      id: "review-vulnerabilities",
      label: "Review Vulnerabilities",
      icon: "shield-check",
      iconBg: "#EBF5FF",
      iconColor: "#1976D2",
      link: "/notifications",
    },
    {
      id: "manage-accessories",
      label: "Manage Accessories",
      icon: "box",
      iconBg: "#EBF5FF",
      iconColor: "#1976D2",
      link: "/accessories",
    },
    {
      id: "manage-licenses",
      label: "Manage Licenses",
      icon: "key",
      iconBg: "#EBF5FF",
      iconColor: "#1976D2",
      link: "/licenses",
    },
  ],
};

/**
 * Recent Alerts Data
 */
export const recentAlertsData = {
  title: "Recent Alerts",
  viewAllLink: "/notifications",
  items: [
    {
      id: 1,
      title: "Critical vulnerability detected",
      description: "3 devices require security review",
      time: "Now",
      icon: "shield-alert",
      iconBg: "#FEE2E2",
      iconColor: "#DC2626",
    },
    {
      id: 2,
      title: "High disk utilization",
      description: "2 devices have low free storage",
      time: "10m",
      icon: "database",
      iconBg: "#FEF3C7",
      iconColor: "#D97706",
    },
    {
      id: 3,
      title: "Software license expiring",
      description: "A license needs renewal",
      time: "1h",
      icon: "key",
      iconBg: "#FEF3C7",
      iconColor: "#D97706",
    },
    {
      id: 4,
      title: "Device offline",
      description: "LAP-032 has not reported for 2 hours",
      time: "2h",
      icon: "wifi",
      iconBg: "#DBEAFE",
      iconColor: "#2563EB",
    },
    {
      id: 5,
      title: "Warranty approaching expiry",
      description: "1 device expires in 30 days",
      time: "3h",
      icon: "shield-check",
      iconBg: "#D1FAE5",
      iconColor: "#059669",
    },
  ],
};

/**
 * Assets Requiring Attention (Interactive Table Data)
 */
export const assetsRequiringAttentionData = {
  title: "Assets Requiring Attention",
  searchPlaceholder: "Search assets...",
  filterTabs: [
    { key: "all", label: "All", count: 8 },
    { key: "critical", label: "Critical", count: 3 },
    { key: "warning", label: "Warning", count: 3 },
    { key: "offline", label: "Offline", count: 2 },
  ],
  columns: [
    { key: "tag", label: "Device / Asset Tag" },
    { key: "employee", label: "Employee" },
    { key: "organization", label: "Organization" },
    { key: "health", label: "Health" },
    { key: "connection", label: "Connection" },
    { key: "cpu", label: "CPU" },
    { key: "ram", label: "RAM" },
    { key: "disk", label: "Disk" },
    { key: "lastSeen", label: "Last Seen" },
    { key: "action", label: "Action" },
  ],
  assets: [
    {
      id: 1,
      tag: "LAP-001",
      employee: "Alice Johnson",
      organization: "Engineering",
      health: "Critical",
      connection: "Online",
      cpu: 92,
      ram: 78,
      disk: 65,
      lastSeen: "Now",
    },
    {
      id: 2,
      tag: "DESK-014",
      employee: "Bob Smith",
      organization: "Sales",
      health: "Warning",
      connection: "Offline",
      cpu: 45,
      ram: 62,
      disk: 88,
      lastSeen: "2h ago",
    },
    {
      id: 3,
      tag: "LAP-032",
      employee: "Carol White",
      organization: "HR",
      health: "Critical",
      connection: "Online",
      cpu: 88,
      ram: 81,
      disk: 77,
      lastSeen: "15m ago",
    },
    {
      id: 4,
      tag: "LAP-067",
      employee: "David Lee",
      organization: "Finance",
      health: "Warning",
      connection: "Online",
      cpu: 60,
      ram: 70,
      disk: 61,
      lastSeen: "30m ago",
    },
    {
      id: 5,
      tag: "DESK-089",
      employee: "Emma Davis",
      organization: "Marketing",
      health: "Healthy",
      connection: "Online",
      cpu: 32,
      ram: 48,
      disk: 42,
      lastSeen: "1h ago",
    },
    {
      id: 6,
      tag: "LAP-102",
      employee: "Frank Miller",
      organization: "Engineering",
      health: "Critical",
      connection: "Offline",
      cpu: 95,
      ram: 84,
      disk: 91,
      lastSeen: "3h ago",
    },
    {
      id: 7,
      tag: "DESK-115",
      employee: "Grace Hopper",
      organization: "DevOps",
      health: "Warning",
      connection: "Online",
      cpu: 58,
      ram: 72,
      disk: 69,
      lastSeen: "45m ago",
    },
    {
      id: 8,
      tag: "LAP-144",
      employee: "Hank Pym",
      organization: "Design",
      health: "Healthy",
      connection: "Online",
      cpu: 28,
      ram: 44,
      disk: 38,
      lastSeen: "10m ago",
    },
  ],
};
