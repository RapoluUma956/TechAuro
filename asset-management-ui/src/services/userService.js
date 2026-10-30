import { api } from "./api";

export const userService = {
  // Retrieve all users
  getAllUsers() {
    return api.get("/users");
  },

  // Retrieve a single user
  getUserById(id) {
    return api.get(`/users/${id}`);
  },

  // Retrieve full employee details including live device hardware & telemetry
  getEmployeeDetail(idOrCode) {
    if (typeof idOrCode === "string" && (idOrCode.startsWith("EMP-") || isNaN(Number(idOrCode)))) {
      return api.get(`/users/employee/${encodeURIComponent(idOrCode)}/detail`);
    }
    return api.get(`/users/${idOrCode}/detail`);
  },

  // Create a user and optionally link device by serialNumber or deviceId
  createUser(userData) {
    return api.post("/users", userData);
  },

  // Update a user profile
  updateUser(id, userData) {
    return api.put(`/users/${id}`, userData);
  },

  // Assign a device to an employee by serialNumber or deviceId
  assignDevice(userId, { serialNumber, deviceId } = {}) {
    return api.post(`/users/${userId}/assign-device`, { serialNumber, deviceId });
  },

  // Unassign device from an employee
  unassignDevice(userId) {
    return api.post(`/users/${userId}/unassign-device`);
  },

  // Retrieve list of all devices
  getDevices() {
    return api.get("/devices");
  },

  // Retrieve list of unassigned devices available for assignment
  getUnassignedDevices() {
    return api.get("/devices/unassigned");
  },

  // Look up a specific device by its serial number
  getDeviceBySerialNumber(serialNumber) {
    return api.get(`/devices/serial/${encodeURIComponent(serialNumber)}`);
  },
};
