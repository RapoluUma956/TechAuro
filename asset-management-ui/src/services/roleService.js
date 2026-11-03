import { api } from "./api";

export const roleService = {
  // 1. Get all roles with optional search filter
  async getRoles(search = "") {
    const params = search ? { search } : {};
    const res = await api.get("/roles", { params });
    return res?.data || [];
  },

  // 2. Get role by ID
  async getRoleById(roleId) {
    const res = await api.get(`/roles/${roleId}`);
    return res?.data;
  },

  // 3. Create new role
  async createRole(payload) {
    const res = await api.post("/roles", payload);
    return res?.data;
  },

  // 4. Update existing role details
  async updateRole(roleId, payload) {
    const res = await api.put(`/roles/${roleId}`, payload);
    return res?.data;
  },

  // 5. Delete role
  async deleteRole(roleId) {
    return await api.delete(`/roles/${roleId}`);
  },

  // 6. Get role permission matrix
  async getRolePermissions(roleId) {
    const res = await api.get(`/roles/${roleId}/permissions`);
    return res?.data;
  },

  // 7. Update role permission matrix
  async updateRolePermissions(roleId, permissions) {
    const res = await api.put(`/roles/${roleId}/permissions`, { permissions });
    return res?.data;
  },

  // 8. Get all system permissions/modules
  async getAllPermissions() {
    const res = await api.get("/permissions");
    return res?.data || [];
  },

  // 9. Get user roles
  async getUserRoles(userId) {
    const res = await api.get(`/users/${userId}/roles`);
    return res?.data;
  },

  // 10. Assign user roles
  async assignUserRoles(userId, roleIds) {
    const res = await api.put(`/users/${userId}/roles`, { roleIds });
    return res?.data;
  },

  // 11. Get assigned employees for a role
  async getAssignedEmployees(roleId) {
    const res = await api.get(`/roles/${roleId}/users`);
    return res?.data || [];
  },
};

