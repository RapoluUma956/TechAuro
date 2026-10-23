import { api, setAccessToken, setCsrfToken } from "./api";

export const authService = {
  // 1) GET /api/auth/csrf
  async getCsrf() {
    const res = await api.get("/auth/csrf");
    if (res?.data?.csrfToken) {
      setCsrfToken(res.data.csrfToken);
    }
    return res?.data?.csrfToken;
  },

  // 2) POST /api/auth/login
  async login({ email, password }) {
    const res = await api.post("/auth/login", { email, password });
    if (res?.data?.accessToken) {
      setAccessToken(res.data.accessToken);
    }
    return res;
  },

  // 3) POST /api/auth/change-password
  async changePassword({ currentPassword, newPassword, confirmPassword }) {
    return await api.post("/auth/change-password", {
      currentPassword,
      newPassword,
      confirmPassword,
    });
  },

  // 4) POST /api/auth/refresh
  async refresh() {
    const res = await api.post("/auth/refresh");
    if (res?.data?.accessToken) {
      setAccessToken(res.data.accessToken);
    }
    return res;
  },

  // 5) POST /api/auth/logout
  async logout() {
    try {
      await api.post("/auth/logout");
    } finally {
      setAccessToken(null);
    }
  },

  // 6) GET /api/auth/me
  async getMe() {
    const res = await api.get("/auth/me");
    return res?.data;
  },
};
