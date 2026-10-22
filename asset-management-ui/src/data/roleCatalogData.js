export const roleCatalogHeaderData = {
  title: "Role Catalog",
  subtitle: "Define roles, default asset packages, and permission templates.",
};

export const roleCatalogTableColumns = [
  { field: "roleName", headerName: "Role Name", width: 200 },
  { field: "department", headerName: "Department", width: 150 },
  { field: "standardLaptop", headerName: "Standard Laptop", width: 180 },
  { field: "standardAccessories", headerName: "Default Accessories", width: 220 },
  { field: "assignedUserCount", headerName: "Assigned Users", width: 140 },
  { field: "status", headerName: "Status", width: 110 },
];

export const roleCatalogData = [
  {
    id: 1,
    roleName: "Software Engineer",
    department: "Engineering",
    standardLaptop: "MacBook Pro 16\" / ThinkPad X1",
    standardAccessories: "Dual Monitor, USB-C Dock, Magic Mouse",
    assignedUserCount: 42,
    status: "Active",
  },
  {
    id: 2,
    roleName: "Product Designer",
    department: "Design",
    standardLaptop: "MacBook Pro 16\" (M3 Pro)",
    standardAccessories: "4K Monitor, Graphic Tablet, Magic Trackpad",
    assignedUserCount: 12,
    status: "Active",
  },
  {
    id: 3,
    roleName: "Sales Representative",
    department: "Sales",
    standardLaptop: "Dell Latitude 5430",
    standardAccessories: "Wireless Headset, Travel Dock, Laptop Bag",
    assignedUserCount: 28,
    status: "Active",
  },
  {
    id: 4,
    roleName: "DevOps & Infrastructure",
    department: "Engineering",
    standardLaptop: "ThinkPad P16 Gen 2",
    standardAccessories: "Ultra-wide Monitor, Mechanical Keyboard",
    assignedUserCount: 8,
    status: "Active",
  },
];
