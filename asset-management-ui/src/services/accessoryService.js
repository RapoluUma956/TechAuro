import { api } from "./api";

export const accessoryService = {
  // 1) Dashboard Summary
  getSummary: async () => {
    return await api.get("/accessories/summary");
  },

  // 2) Accessory Inventory Table
  getAccessories: async ({ page = 0, size = 10, search = "", category = "" } = {}) => {
    const params = new URLSearchParams();
    params.append("page", page);
    params.append("size", size);
    if (search) params.append("search", search);
    if (category && category !== "All" && category !== "ALL") {
      params.append("category", category);
    }
    return await api.get(`/accessories?${params.toString()}`);
  },

  // 3) Create / Merge Ordinary Accessory
  createAccessory: async (payload) => {
    return await api.post("/accessories", payload);
  },

  // 4) Get Accessory by ID
  getAccessoryById: async (id) => {
    return await api.get(`/accessories/${id}`);
  },

  // 5) Update Accessory
  updateAccessory: async (id, payload) => {
    return await api.put(`/accessories/${id}`, payload);
  },

  // 6) Delete Accessory
  deleteAccessory: async (id) => {
    return await api.delete(`/accessories/${id}`);
  },

  // 7) Laptop Summary
  getLaptopSummary: async () => {
    return await api.get("/accessories/laptops/summary");
  },

  // 8) Laptop Configurations Table
  getLaptopConfigurations: async ({
    brand = "",
    ram = "",
    ssd = "",
    search = "",
    page = 0,
    size = 10,
  } = {}) => {
    const params = new URLSearchParams();
    params.append("page", page);
    params.append("size", size);
    if (brand && brand !== "All" && brand !== "ALL") params.append("brand", brand);
    if (ram && ram !== "All" && ram !== "ALL") params.append("ram", ram);
    if (ssd && ssd !== "All" && ssd !== "ALL") params.append("ssd", ssd);
    if (search) params.append("search", search);
    return await api.get(`/accessories/laptops/configurations?${params.toString()}`);
  },

  // 9) Bulk Register Laptops
  bulkAddLaptops: async (payload) => {
    return await api.post("/accessories/laptops/bulk", payload);
  },

  // 10) Get Individual Laptop Details
  getLaptopById: async (deviceId) => {
    return await api.get(`/accessories/laptops/${deviceId}`);
  },

  // 11) Category Options Dropdown
  getCategories: async () => {
    return await api.get("/accessories/categories");
  },

  // 12) Assign Accessory to Employee
  assignAccessory: async (accessoryId, payload) => {
    return await api.post(`/accessories/${accessoryId}/assign`, payload);
  },

  // 13) Assign Laptop to Employee
  assignLaptop: async (deviceId, payload) => {
    return await api.post(`/accessories/laptops/${deviceId}/assign`, payload);
  },
};

