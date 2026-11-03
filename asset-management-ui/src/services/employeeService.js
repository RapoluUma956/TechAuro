import { api } from "./api";

export const employeeService = {
  // 1. Employee Dashboard Metrics
  getEmployeeMetrics() {
    return api.get("/employees/metrics");
  },

  // 2. Employee List with pagination, search, and filtering
  getEmployees(params = {}) {
    return api.get("/employees", { params });
  },

  // 3. Available Laptops/Devices for assignment
  getAvailableDevices() {
    return api.get("/employees/available-devices");
  },

  // 4. Device lookup and validation by serial number
  getDeviceBySerial(serialNumber) {
    return api.get(`/devices/serial/${encodeURIComponent(serialNumber)}`);
  },

  // 5. Create new employee
  createEmployee(payload) {
    return api.post("/employees", payload);
  },

  // 6. Get Employee Profile & summary counts
  getEmployeeById(employeeId) {
    return api.get(`/employees/${employeeId}`);
  },

  // 7. Get Device Details Tab info
  getEmployeeDevice(employeeId) {
    return api.get(`/employees/${employeeId}/device`);
  },

  // 8. Get Device Assignment History
  getDeviceHistory(deviceId, params = {}) {
    return api.get(`/devices/${deviceId}/history`, { params });
  },

  // 9. Get Employee Accessories
  getEmployeeAccessories(employeeId, params = {}) {
    return api.get(`/employees/${employeeId}/accessories`, { params });
  },

  // 10. Get Employee Software Inventory
  getEmployeeSoftware(employeeId, params = {}) {
    return api.get(`/employees/${employeeId}/software`, { params });
  },

  // 11. Get Employee Vulnerabilities
  getEmployeeVulnerabilities(employeeId, params = {}) {
    return api.get(`/employees/${employeeId}/vulnerabilities`, { params });
  },

  // 12. Get Employee Licenses
  getEmployeeLicenses(employeeId, params = {}) {
    return api.get(`/employees/${employeeId}/licenses`, { params });
  },

  // 13. Reassign / Change Device
  reassignEmployeeDevice(employeeId, payload) {
    return api.put(`/employees/${employeeId}/device`, payload);
  },

  // 14. Unassign Device
  unassignEmployeeDevice(employeeId) {
    return api.delete(`/employees/${employeeId}/device`);
  },
};

