import { api } from "./api";

export const notificationService = {
  /**
   * Fetch paginated list of notifications with optional filters.
   *
   * @param {Object} [params]
   * @param {number} [params.page=0]
   * @param {number} [params.size=20]
   * @param {string} [params.severity] - ALL, CRITICAL, HIGH, MEDIUM, LOW
   * @param {string} [params.type] - ALL, HARDWARE, SECURITY, LICENSE, DEVICE, SYSTEM
   * @param {boolean} [params.unreadOnly]
   * @param {string} [params.search]
   * @returns {Promise<Object>}
   */
  async getNotifications(params = {}) {
    return api.get("/notifications", { params });
  },

  /**
   * Fetch notification summary counts (total, unread, critical, high, etc.)
   *
   * @returns {Promise<Object>}
   */
  async getSummary() {
    return api.get("/notifications/summary");
  },

  /**
   * Mark a single notification as read.
   *
   * @param {number|string} notificationId
   * @returns {Promise<Object>}
   */
  async markAsRead(notificationId) {
    return api.patch(`/notifications/${notificationId}/read`);
  },

  /**
   * Mark all unread notifications as read.
   *
   * @returns {Promise<void>}
   */
  async markAllAsRead() {
    return api.post("/notifications/read-all");
  },

  /**
   * Dismiss/Delete a notification.
   *
   * @param {number|string} notificationId
   * @returns {Promise<void>}
   */
  async deleteNotification(notificationId) {
    return api.delete(`/notifications/${notificationId}`);
  },

  /**
   * Trigger on-demand sync of rule-based notifications.
   *
   * @returns {Promise<void>}
   */
  async syncNotifications() {
    return api.post("/notifications/sync");
  },
};

export default notificationService;

