import { api } from "./api";
import { userService } from "./userService";

export const licenseService = {
  // 1) Dashboard Summary Metrics
  getSummary: async () => {
    return await api.get("/licenses/summary");
  },

  // 2) Dropdown filter options (Distinct software and publishers)
  getFilterOptions: async () => {
    return await api.get("/licenses/filter-options");
  },

  // 3) Paginated, Filtered, and Searched Licenses
  getLicenses: async ({
    page = 0,
    size = 10,
    search = "",
    software = "",
    publisher = "",
    status = "",
    expiry = "",
  } = {}) => {
    const params = new URLSearchParams();
    params.append("page", page);
    params.append("size", size);

    if (search && search.trim()) {
      params.append("search", search.trim());
    }
    if (software && software !== "All" && software !== "ALL") {
      params.append("software", software);
    }
    if (publisher && publisher !== "All" && publisher !== "ALL") {
      params.append("publisher", publisher);
    }
    if (status && status !== "All" && status !== "ALL") {
      params.append("status", status);
    }
    if (expiry && expiry !== "All" && expiry !== "ALL") {
      params.append("expiry", expiry);
    }

    return await api.get(`/licenses?${params.toString()}`);
  },

  // 4) Get License by ID with assignment history
  getLicenseById: async (licenseId) => {
    return await api.get(`/licenses/${licenseId}`);
  },

  // 5) Create new License
  createLicense: async (payload) => {
    return await api.post("/licenses", payload);
  },

  // 6) Update existing License
  updateLicense: async (licenseId, payload) => {
    return await api.put(`/licenses/${licenseId}`, payload);
  },

  // 7) Delete License
  deleteLicense: async (licenseId) => {
    return await api.delete(`/licenses/${licenseId}`);
  },

  // 8) Assign License to Employee
  assignLicense: async (licenseId, payload) => {
    return await api.post(`/licenses/${licenseId}/assign`, payload);
  },

  // 9) Release or Return License seat
  releaseLicense: async (licenseId, payload) => {
    return await api.post(`/licenses/${licenseId}/release`, payload);
  },

  // 10) Fetch employees directory for assignment
  getEmployeesForAssignment: async () => {
    try {
      const res = await userService.getAllUsers();
      if (Array.isArray(res)) return res;
      if (res && Array.isArray(res.data)) return res.data;
      return [];
    } catch {
      return [];
    }
  },
};
