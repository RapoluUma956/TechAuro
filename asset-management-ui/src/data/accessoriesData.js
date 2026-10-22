export const accessoriesHeaderData = {
  title: "Accessories",
  subtitle: "Track inventory, stocks, warranty, and peripheral asset allocation.",
};

export const accessoriesTableColumns = [
  { field: "assetTag", headerName: "Asset Tag", width: 130 },
  { field: "name", headerName: "Item Name", width: 200 },
  { field: "category", headerName: "Category", width: 140 },
  { field: "totalStock", headerName: "Total Stock", width: 120 },
  { field: "availableStock", headerName: "Available", width: 110 },
  { field: "assignedStock", headerName: "Assigned", width: 110 },
  { field: "status", headerName: "Status", width: 120 },
];

export const accessoriesData = [
  {
    id: 1,
    assetTag: "ACC-MON-001",
    name: "Dell UltraSharp 27\" 4K Monitor",
    category: "Monitors",
    totalStock: 60,
    availableStock: 14,
    assignedStock: 46,
    status: "In Stock",
  },
  {
    id: 2,
    assetTag: "ACC-DCK-002",
    name: "CalDigit TS4 Thunderbolt Dock",
    category: "Docking Stations",
    totalStock: 45,
    availableStock: 8,
    assignedStock: 37,
    status: "Low Stock",
  },
  {
    id: 3,
    assetTag: "ACC-KEY-003",
    name: "Logitech MX Mechanical Keyboard",
    category: "Peripherals",
    totalStock: 75,
    availableStock: 22,
    assignedStock: 53,
    status: "In Stock",
  },
  {
    id: 4,
    assetTag: "ACC-MOU-004",
    name: "Logitech MX Master 3S Mouse",
    category: "Peripherals",
    totalStock: 80,
    availableStock: 25,
    assignedStock: 55,
    status: "In Stock",
  },
  {
    id: 5,
    assetTag: "ACC-HED-005",
    name: "Jabra Evolve2 75 Wireless Headset",
    category: "Audio",
    totalStock: 35,
    availableStock: 3,
    assignedStock: 32,
    status: "Low Stock",
  },
];
