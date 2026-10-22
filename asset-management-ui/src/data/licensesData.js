export const licensesHeaderData = {
  title: "License Management",
  subtitle: "Track enterprise software subscriptions, seat allocations, and renewals.",
};

export const licensesTableColumns = [
  { field: "softwareName", headerName: "Software", width: 180 },
  { field: "vendor", headerName: "Vendor", width: 140 },
  { field: "licenseType", headerName: "License Type", width: 140 },
  { field: "totalSeats", headerName: "Total Seats", width: 110 },
  { field: "usedSeats", headerName: "Used Seats", width: 110 },
  { field: "expiryDate", headerName: "Expiry Date", width: 130 },
  { field: "status", headerName: "Status", width: 110 },
];

export const licensesData = [
  {
    id: 1,
    softwareName: "GitHub Enterprise",
    vendor: "Microsoft",
    licenseType: "Annual Subscription",
    totalSeats: 50,
    usedSeats: 44,
    expiryDate: "2027-01-15",
    status: "Active",
  },
  {
    id: 2,
    softwareName: "Figma Organization",
    vendor: "Figma",
    licenseType: "Per-User Monthly",
    totalSeats: 20,
    usedSeats: 18,
    expiryDate: "2026-12-31",
    status: "Active",
  },
  {
    id: 3,
    softwareName: "JetBrains All Products",
    vendor: "JetBrains",
    licenseType: "Annual Commercial",
    totalSeats: 30,
    usedSeats: 28,
    expiryDate: "2026-11-20",
    status: "Expiring Soon",
  },
  {
    id: 4,
    softwareName: "Slack Enterprise Grid",
    vendor: "Salesforce",
    licenseType: "Annual Subscription",
    totalSeats: 150,
    usedSeats: 124,
    expiryDate: "2027-06-30",
    status: "Active",
  },
  {
    id: 5,
    softwareName: "1Password Business",
    vendor: "AgileBits",
    licenseType: "Annual Subscription",
    totalSeats: 100,
    usedSeats: 92,
    expiryDate: "2027-03-15",
    status: "Active",
  },
];
