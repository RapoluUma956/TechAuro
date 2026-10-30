import React, { useState, useEffect, useCallback, useMemo } from "react";
import { Link } from "react-router-dom";
import notificationService from "../../services/notificationService";
import useAutoRefresh from "../../hooks/useAutoRefresh";

function getSeverityBadge(severity) {
  const s = (severity || "").toUpperCase();
  switch (s) {
    case "CRITICAL":
      return {
        bg: "bg-red-50 text-red-700 border-red-200",
        dot: "bg-red-600",
        label: "Critical",
      };
    case "HIGH":
      return {
        bg: "bg-amber-50 text-amber-800 border-amber-200",
        dot: "bg-amber-600",
        label: "High",
      };
    case "MEDIUM":
      return {
        bg: "bg-blue-50 text-blue-700 border-blue-200",
        dot: "bg-blue-600",
        label: "Medium",
      };
    case "LOW":
    default:
      return {
        bg: "bg-slate-50 text-slate-700 border-slate-200",
        dot: "bg-slate-500",
        label: "Low",
      };
  }
}

function getTypeIcon(type) {
  const t = (type || "").toUpperCase();
  switch (t) {
    case "SECURITY":
      return (
        <svg className="h-5 w-5 text-red-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
      );
    case "HARDWARE":
      return (
        <svg className="h-5 w-5 text-amber-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <rect x="4" y="4" width="16" height="16" rx="2" />
          <rect x="9" y="9" width="6" height="6" />
          <line x1="9" y1="1" x2="9" y2="4" />
          <line x1="15" y1="1" x2="15" y2="4" />
          <line x1="9" y1="20" x2="9" y2="23" />
          <line x1="15" y1="20" x2="15" y2="23" />
          <line x1="20" y1="9" x2="23" y2="9" />
          <line x1="20" y1="14" x2="23" y2="14" />
          <line x1="1" y1="9" x2="4" y2="9" />
          <line x1="1" y1="14" x2="4" y2="14" />
        </svg>
      );
    case "LICENSE":
      return (
        <svg className="h-5 w-5 text-indigo-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
      );
    case "DEVICE":
    default:
      return (
        <svg className="h-5 w-5 text-[#1976D2]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <rect x="2" y="3" width="20" height="14" rx="2" />
          <line x1="8" y1="21" x2="16" y2="21" />
          <line x1="12" y1="17" x2="12" y2="21" />
        </svg>
      );
  }
}

function formatRelativeTime(dateString) {
  if (!dateString) return "Recently";
  const date = new Date(dateString);
  const now = new Date();
  const diffSec = Math.floor((now - date) / 1000);

  if (diffSec < 60) return "Just now";
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHours = Math.floor(diffMin / 60);
  if (diffHours < 24) return `${diffHours}h ago`;
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `${diffDays}d ago`;

  return date.toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [summary, setSummary] = useState({
    total: 0,
    unreadCount: 0,
    criticalCount: 0,
    highCount: 0,
    mediumCount: 0,
    lowCount: 0,
  });
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  // Filters
  const [activeTab, setActiveTab] = useState("ALL"); // ALL, UNREAD, CRITICAL, HIGH, SECURITY, HARDWARE, LICENSE
  const [searchQuery, setSearchQuery] = useState("");
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [feedbackToast, setFeedbackToast] = useState("");

  const showToast = (msg) => {
    setFeedbackToast(msg);
    setTimeout(() => setFeedbackToast(""), 3500);
  };

  // Fetch summary and notification items
  const loadData = useCallback(async (isBackground = false) => {
    if (!isBackground) {
      setLoading(true);
      setError(null);
    } else {
      setRefreshing(true);
    }

    try {
      const severityFilter =
        activeTab === "CRITICAL" || activeTab === "HIGH" ? activeTab : undefined;
      const typeFilter =
        activeTab === "SECURITY" || activeTab === "HARDWARE" || activeTab === "LICENSE"
          ? activeTab
          : undefined;
      const unreadOnly = activeTab === "UNREAD" ? true : undefined;

      const [summaryRes, notifRes] = await Promise.all([
        notificationService.getSummary(),
        notificationService.getNotifications({
          page: 0,
          size: 50,
          severity: severityFilter,
          type: typeFilter,
          unreadOnly,
          search: searchQuery || undefined,
        }),
      ]);

      if (summaryRes) setSummary(summaryRes);
      if (notifRes?.content) {
        setNotifications(notifRes.content);
      }
    } catch (err) {
      console.error("Failed to fetch notifications:", err);
      if (!isBackground) {
        setError(err.message || "Unable to load security notifications. Please try again.");
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [activeTab, searchQuery]);

  useEffect(() => {
    loadData(false);
  }, [loadData]);

  // 30-Second Active-Tab Auto-Refresh
  useAutoRefresh(
    useCallback(
      (isBackground) => {
        loadData(isBackground);
      },
      [loadData]
    ),
    30000
  );

  // Mark single notification as read
  const handleMarkAsRead = async (id, e) => {
    e?.stopPropagation();
    setActionLoadingId(id);
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.notificationId === id ? { ...n, isRead: true, status: "READ" } : n))
      );
      setSummary((prev) => ({
        ...prev,
        unreadCount: Math.max(0, prev.unreadCount - 1),
      }));
      showToast("Notification marked as read.");
    } catch (err) {
      showToast(err.message || "Failed to update notification status.");
    } finally {
      setActionLoadingId(null);
    }
  };

  // Mark all as read
  const handleMarkAllAsRead = async () => {
    setRefreshing(true);
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true, status: "READ" })));
      setSummary((prev) => ({ ...prev, unreadCount: 0 }));
      showToast("All notifications marked as read.");
    } catch (err) {
      showToast(err.message || "Failed to mark all as read.");
    } finally {
      setRefreshing(false);
    }
  };

  // Dismiss / delete notification
  const handleDeleteNotification = async (id, e) => {
    e?.stopPropagation();
    setActionLoadingId(id);
    try {
      await notificationService.deleteNotification(id);
      setNotifications((prev) => prev.filter((n) => n.notificationId !== id));
      setSummary((prev) => ({
        ...prev,
        total: Math.max(0, prev.total - 1),
      }));
      showToast("Notification dismissed.");
    } catch (err) {
      showToast(err.message || "Failed to dismiss notification.");
    } finally {
      setActionLoadingId(null);
    }
  };

  // Sync now
  const handleSync = async () => {
    setRefreshing(true);
    try {
      await notificationService.syncNotifications();
      await loadData(false);
      showToast("System alerts synchronized with latest telemetry.");
    } catch (err) {
      showToast(err.message || "Synchronization failed.");
      setRefreshing(false);
    }
  };

  return (
    <div className="space-y-6 text-[#03312E] pb-10">
      {/* Toast Notification */}
      {feedbackToast && (
        <div className="fixed bottom-6 right-6 z-50 flex items-center gap-2 rounded-xl bg-[#03312E] text-white px-4 py-3 shadow-xl border border-white/10 text-xs font-semibold animate-in fade-in slide-in-from-bottom-3 duration-200">
          <svg className="h-4 w-4 text-[#00C896]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <polyline points="20 6 9 17 4 12" />
          </svg>
          <span>{feedbackToast}</span>
        </div>
      )}

      {/* Page Header */}
      <header className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#03312E] tracking-tight">
              Notifications & Security
            </h1>
            {refreshing && (
              <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                <svg className="h-3 w-3 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M21 12a9 9 0 1 1-6.219-8.56" />
                </svg>
                Syncing...
              </span>
            )}
          </div>
          <p className="mt-1 text-xs sm:text-sm text-[#64748B]">
            Automated security detection, hardware metrics alerts, and license compliance monitoring.
          </p>
        </div>

        {/* Global Action Buttons */}
        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={handleSync}
            disabled={refreshing}
            className="inline-flex items-center gap-1.5 rounded-xl border border-[#DCE5E1] bg-white px-3.5 py-2 text-xs font-semibold text-[#03312E] shadow-sm hover:bg-[#F8FAFC] hover:border-[#CBD5E1] transition-all cursor-pointer disabled:opacity-50"
            title="Scan telemetry and update notifications"
          >
            <svg
              className={`h-4 w-4 text-[#1976D2] ${refreshing ? "animate-spin" : ""}`}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <polyline points="23 4 23 10 17 10" />
              <polyline points="1 20 1 14 7 14" />
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
            </svg>
            Sync Alerts
          </button>

          {summary.unreadCount > 0 && (
            <button
              type="button"
              onClick={handleMarkAllAsRead}
              disabled={refreshing}
              className="inline-flex items-center gap-1.5 rounded-xl bg-[#03312E] px-3.5 py-2 text-xs font-semibold text-white shadow-sm hover:bg-[#022422] transition-all cursor-pointer disabled:opacity-50"
            >
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="20 6 9 17 4 12" />
              </svg>
              Mark All as Read
            </button>
          )}
        </div>
      </header>

      {/* Metric Summary Cards */}
      <section className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 sm:p-5 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-[#64748B]">Total Alerts</span>
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-slate-100 text-slate-700">
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
            </div>
          </div>
          <p className="mt-2 text-2xl font-black text-[#03312E]">{summary.total}</p>
          <span className="text-[11px] text-[#94A3B8]">Logged system events</span>
        </div>

        <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 sm:p-5 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-[#64748B]">Unread Notices</span>
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-blue-50 text-[#1976D2]">
              <span className="h-2.5 w-2.5 rounded-full bg-[#1976D2]"></span>
            </div>
          </div>
          <p className="mt-2 text-2xl font-black text-[#1976D2]">{summary.unreadCount}</p>
          <span className="text-[11px] text-[#94A3B8]">Awaiting acknowledgement</span>
        </div>

        <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 sm:p-5 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-[#64748B]">Critical Risks</span>
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-red-50 text-red-600">
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>
          </div>
          <p className="mt-2 text-2xl font-black text-red-600">{summary.criticalCount}</p>
          <span className="text-[11px] text-[#94A3B8]">Immediate action required</span>
        </div>

        <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 sm:p-5 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-[#64748B]">High Priority</span>
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-amber-50 text-amber-700">
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
              </svg>
            </div>
          </div>
          <p className="mt-2 text-2xl font-black text-amber-700">{summary.highCount}</p>
          <span className="text-[11px] text-[#94A3B8]">Hardware & expiry alerts</span>
        </div>
      </section>

      {/* Filter Tabs & Search Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 bg-white p-3 rounded-2xl border border-[#DCE5E1] shadow-xs">
        {/* Navigation Tabs */}
        <div className="flex items-center gap-1 overflow-x-auto pb-1 md:pb-0 scrollbar-none">
          {[
            { id: "ALL", label: "All Alerts" },
            { id: "UNREAD", label: "Unread", count: summary.unreadCount },
            { id: "CRITICAL", label: "Critical", count: summary.criticalCount },
            { id: "HIGH", label: "High", count: summary.highCount },
            { id: "SECURITY", label: "Security" },
            { id: "HARDWARE", label: "Hardware" },
            { id: "LICENSE", label: "Licenses" },
          ].map((tab) => {
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-1.5 whitespace-nowrap rounded-xl px-3 py-1.5 text-xs font-semibold transition-all cursor-pointer ${
                  isActive
                    ? "bg-[#03312E] text-white shadow-xs"
                    : "text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#03312E]"
                }`}
              >
                <span>{tab.label}</span>
                {tab.count !== undefined && tab.count > 0 && (
                  <span
                    className={`ml-1 rounded-full px-1.5 py-0.2 text-[10px] font-bold ${
                      isActive ? "bg-white/20 text-white" : "bg-slate-200 text-slate-800"
                    }`}
                  >
                    {tab.count}
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* Search input */}
        <div className="relative w-full md:w-72 shrink-0">
          <svg
            className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#94A3B8]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            placeholder="Search notifications..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full rounded-xl border border-[#DCE5E1] bg-[#F8FAFC] py-1.5 pl-8 pr-8 text-xs text-[#03312E] placeholder:text-[#94A3B8] focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 transition-all"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => setSearchQuery("")}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#03312E]"
            >
              <svg className="h-3 w-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          )}
        </div>
      </div>

      {/* Notifications List Card */}
      <div className="rounded-2xl border border-[#DCE5E1] bg-white shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex flex-col items-center justify-center p-12 text-center">
            <svg className="h-8 w-8 animate-spin text-[#1976D2] mb-3" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M21 12a9 9 0 1 1-6.219-8.56" />
            </svg>
            <p className="text-xs font-semibold text-[#64748B]">Loading notifications and system events...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center">
            <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-red-50 text-red-600">
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>
            <h3 className="text-sm font-bold text-[#03312E]">{error}</h3>
            <button
              type="button"
              onClick={() => loadData(false)}
              className="mt-3 inline-flex items-center gap-1.5 rounded-xl border border-[#DCE5E1] bg-white px-4 py-2 text-xs font-semibold text-[#03312E] hover:bg-[#F8FAFC]"
            >
              Try Again
            </button>
          </div>
        ) : notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-12 text-center">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#EBF5FF] text-[#1976D2] mb-3">
              <svg className="h-7 w-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
            </div>
            <h3 className="text-sm font-bold text-[#03312E]">No Notifications Found</h3>
            <p className="text-xs text-[#64748B] mt-1 max-w-sm">
              {activeTab !== "ALL"
                ? `No notifications currently match the "${activeTab}" filter.`
                : "All systems are operating normally within configured threshold limits."}
            </p>
          </div>
        ) : (
          <div className="divide-y divide-[#F1F5F9]">
            {notifications.map((item) => {
              const badge = getSeverityBadge(item.severity);
              const isUnread = !item.isRead;
              const isOperating = actionLoadingId === item.notificationId;

              return (
                <div
                  key={item.notificationId}
                  className={`flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-4 sm:p-5 transition-colors ${
                    isUnread
                      ? "bg-[#F8FAFC] border-l-4 border-l-[#1976D2]"
                      : "bg-white hover:bg-[#FAFCFB]"
                  }`}
                >
                  {/* Left: Icon, Details, Badges */}
                  <div className="flex items-start gap-3.5 min-w-0 flex-1">
                    {/* Category Type Icon Box */}
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white border border-[#E2E8F0] shadow-xs">
                      {getTypeIcon(item.notificationType)}
                    </div>

                    <div className="min-w-0 flex-1">
                      {/* Top Bar: Title & Badges */}
                      <div className="flex flex-wrap items-center gap-2 mb-1">
                        <span
                          className={`inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-[10px] font-bold border ${badge.bg}`}
                        >
                          <span className={`h-1.5 w-1.5 rounded-full ${badge.dot}`}></span>
                          {badge.label}
                        </span>

                        <span className="rounded-md bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-700">
                          {item.notificationType || "SYSTEM"}
                        </span>

                        {item.deviceAssetTag && (
                          <span className="rounded-md bg-emerald-50 text-emerald-800 border border-emerald-200 px-2 py-0.5 text-[10px] font-bold">
                            {item.deviceAssetTag}
                          </span>
                        )}

                        <span className="text-[11px] text-[#94A3B8] ml-auto">
                          {formatRelativeTime(item.createdAt)}
                        </span>
                      </div>

                      {/* Notification Title */}
                      <h4
                        className={`text-xs sm:text-sm leading-snug ${
                          isUnread ? "font-bold text-[#03312E]" : "font-medium text-[#334155]"
                        }`}
                      >
                        {item.title}
                      </h4>

                      {/* Notification Message */}
                      <p className="mt-1 text-xs text-[#64748B] leading-relaxed">
                        {item.message}
                      </p>

                      {/* Metadata footer */}
                      {(item.deviceName || item.userName) && (
                        <div className="mt-2 flex items-center gap-3 text-[11px] text-[#94A3B8]">
                          {item.deviceName && (
                            <span className="truncate">
                              Asset: <strong className="text-[#475569]">{item.deviceName}</strong>
                            </span>
                          )}
                          {item.userName && (
                            <span className="truncate">
                              Assigned to: <strong className="text-[#475569]">{item.userName}</strong>
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Right: Actions */}
                  <div className="flex items-center gap-2 shrink-0 self-end sm:self-center">
                    {isUnread && (
                      <button
                        type="button"
                        onClick={(e) => handleMarkAsRead(item.notificationId, e)}
                        disabled={isOperating}
                        className="inline-flex items-center gap-1 rounded-lg border border-[#DCE5E1] bg-white px-2.5 py-1 text-xs font-semibold text-[#1976D2] hover:bg-[#F0F7FF] transition-colors cursor-pointer disabled:opacity-50"
                        title="Mark as read"
                      >
                        <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                          <polyline points="20 6 9 17 4 12" />
                        </svg>
                        <span>Mark read</span>
                      </button>
                    )}

                    <button
                      type="button"
                      onClick={(e) => handleDeleteNotification(item.notificationId, e)}
                      disabled={isOperating}
                      className="inline-flex items-center justify-center h-7 w-7 rounded-lg text-[#94A3B8] hover:text-[#DC2626] hover:bg-red-50 transition-colors cursor-pointer disabled:opacity-50"
                      title="Dismiss notification"
                    >
                      <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <line x1="18" y1="6" x2="6" y2="18" />
                        <line x1="6" y1="6" x2="18" y2="18" />
                      </svg>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

