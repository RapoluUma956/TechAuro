import React, { useState, useEffect, useCallback, useMemo } from "react";
import notificationService from "../../services/notificationService";
import useAutoRefresh from "../../hooks/useAutoRefresh";
import { MetricCards, SearchBar, Button } from "../../components/common";
import {
  BellIcon,
  CheckCircleIcon,
  AlertTriangleIcon,
  ClockAlertIcon,
  RotateCcwIcon,
  TrashIcon,
} from "../../components/common/Icons";

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

  return date.toLocaleDateString("en-GB", {
    day: "2-digit",
    month: "short",
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

  // Search state
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
      const [summaryRes, notifRes] = await Promise.all([
        notificationService.getSummary(),
        notificationService.getNotifications({
          page: 0,
          size: 50,
          search: searchQuery.trim() || undefined,
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
  }, [searchQuery]);

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

  // Standard metric cards matching LicenseMetricCards / AccessoriesMetricCards / DashboardMetricCards
  const metricCards = useMemo(() => {
    return [
      {
        id: "total",
        title: "Total Alerts",
        value: loading ? "—" : (summary?.total ?? 0),
        caption: "Logged system events",
        icon: BellIcon,
        iconColor: "#1976D2",
        iconBg: "#E3F2FD",
      },
      {
        id: "unread",
        title: "Unread Notices",
        value: loading ? "—" : (summary?.unreadCount ?? 0),
        caption: "Awaiting acknowledgement",
        icon: CheckCircleIcon,
        iconColor: "#00C896",
        iconBg: "#E8F8F2",
      },
      {
        id: "critical",
        title: "Critical Risks",
        value: loading ? "—" : (summary?.criticalCount ?? 0),
        caption: "Immediate action required",
        icon: AlertTriangleIcon,
        iconColor: "#EF4444",
        iconBg: "#FEE2E2",
      },
      {
        id: "high",
        title: "High Priority",
        value: loading ? "—" : (summary?.highCount ?? 0),
        caption: "Hardware & expiry alerts",
        icon: ClockAlertIcon,
        iconColor: "#F59E0B",
        iconBg: "#FEF3C7",
      },
    ];
  }, [summary, loading]);

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

      {/* Page Header matching other pages */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold tracking-tight text-[#03312E] sm:text-3xl">
              Notifications & Security
            </h1>
            {refreshing && (
              <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                <span className="h-2 w-2 rounded-full bg-emerald-600 animate-pulse" />
                Syncing...
              </span>
            )}
          </div>
          <p className="mt-1 text-xs font-medium text-[#64748B] sm:text-sm">
            Automated security detection, hardware metrics alerts, and license compliance monitoring.
          </p>
        </div>

        {/* Global Action Buttons */}
        <div className="flex items-center gap-2.5">
          <Button
            variant="outline"
            size="md"
            onClick={handleSync}
            disabled={refreshing}
            icon={<RotateCcwIcon className={`h-4 w-4 ${refreshing ? "animate-spin" : ""}`} />}
          >
            Sync Alerts
          </Button>

          {summary.unreadCount > 0 && (
            <Button
              variant="primary"
              size="md"
              onClick={handleMarkAllAsRead}
              disabled={refreshing}
              icon={
                <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              }
            >
              Mark All as Read
            </Button>
          )}
        </div>
      </div>

      {/* Reusable MetricCards component matching other pages */}
      <MetricCards items={metricCards} columns={4} />

      {/* Standard Search Bar Card matching AccessoryTable / EmployeeFilters / LicenseFilters */}
      <div className="rounded-2xl border border-[#DCE5E1] bg-white p-4 shadow-sm">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="w-full sm:max-w-xl">
            <SearchBar
              value={searchQuery}
              onChange={setSearchQuery}
              placeholder="Search notifications by title, asset tag, employee, or message..."
              variant="long"
            />
          </div>

          <div className="text-xs font-semibold text-[#64748B]">
            Total: <span className="font-bold text-[#03312E]">{notifications.length}</span> notices
          </div>
        </div>
      </div>

      {/* Notifications List Card */}
      <div className="overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white shadow-sm">
        {loading ? (
          <div className="flex flex-col items-center justify-center p-16 text-center">
            <div className="h-8 w-8 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent mb-3" />
            <p className="text-xs font-semibold text-[#64748B]">Loading notifications and system events...</p>
          </div>
        ) : error ? (
          <div className="p-12 text-center">
            <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-red-50 text-red-600">
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>
            <h3 className="text-sm font-bold text-[#03312E]">{error}</h3>
            <div className="mt-4">
              <Button variant="outline" size="sm" onClick={() => loadData(false)}>
                Try Again
              </Button>
            </div>
          </div>
        ) : notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-16 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-[#EBF7F2] text-[#03312E] mb-3">
              <svg className="h-8 w-8" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
            </div>
            <h3 className="text-base font-bold text-[#03312E]">No Notifications Found</h3>
            <p className="text-sm text-[#64748B] mt-1 max-w-sm">
              {searchQuery
                ? `No notifications matched "${searchQuery}". Try a different keyword.`
                : "All systems are operating normally within configured threshold limits."}
            </p>
          </div>
        ) : (
          <div className="divide-y divide-[#EBF0EE]">
            {notifications.map((item) => {
              const badge = getSeverityBadge(item.severity);
              const isUnread = !item.isRead;
              const isOperating = actionLoadingId === item.notificationId;

              return (
                <div
                  key={item.notificationId}
                  className={`flex flex-col sm:flex-row sm:items-start justify-between gap-4 p-5 transition-colors ${
                    isUnread
                      ? "bg-[#F8FAFC] border-l-4 border-l-[#1976D2]"
                      : "bg-white hover:bg-[#F8FAF9]"
                  }`}
                >
                  {/* Left: Icon, Details, Badges */}
                  <div className="flex items-start gap-3.5 min-w-0 flex-1">
                    {/* Category Type Icon Box */}
                    <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-white border border-[#DCE5E1] shadow-2xs">
                      {getTypeIcon(item.notificationType)}
                    </div>

                    <div className="min-w-0 flex-1">
                      {/* Top Bar: Title & Badges */}
                      <div className="flex flex-wrap items-center gap-2 mb-1.5">
                        <span
                          className={`inline-flex items-center gap-1 rounded-md px-2.5 py-0.5 text-[10px] font-bold border ${badge.bg}`}
                        >
                          <span className={`h-1.5 w-1.5 rounded-full ${badge.dot}`}></span>
                          {badge.label}
                        </span>

                        <span className="rounded-md bg-[#F1F5F9] px-2.5 py-0.5 text-[10px] font-bold text-[#1E293B]">
                          {item.notificationType || "SYSTEM"}
                        </span>

                        {item.deviceAssetTag && (
                          <span className="rounded-md bg-[#E3F2FD] text-[#1976D2] border border-[#BBDEFB] px-2.5 py-0.5 font-mono text-[10px] font-bold">
                            {item.deviceAssetTag}
                          </span>
                        )}

                        <span className="text-xs font-medium text-[#64748B] ml-auto">
                          {formatRelativeTime(item.createdAt)}
                        </span>
                      </div>

                      {/* Notification Title */}
                      <h4
                        className={`text-sm leading-snug ${
                          isUnread ? "font-bold text-[#03312E]" : "font-semibold text-[#334155]"
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
                        <div className="mt-2.5 flex items-center gap-3 text-xs text-[#64748B]">
                          {item.deviceName && (
                            <span className="truncate">
                              Asset: <strong className="font-semibold text-[#03312E]">{item.deviceName}</strong>
                            </span>
                          )}
                          {item.userName && (
                            <span className="truncate">
                              Assigned to: <strong className="font-semibold text-[#03312E]">{item.userName}</strong>
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Right: Actions */}
                  <div className="flex items-center gap-1.5 shrink-0 self-end sm:self-center">
                    {isUnread && (
                      <button
                        type="button"
                        onClick={(e) => handleMarkAsRead(item.notificationId, e)}
                        disabled={isOperating}
                        className="inline-flex items-center gap-1.5 rounded-lg border border-[#DCE5E1] bg-white px-3 py-1.5 text-xs font-bold text-[#1976D2] hover:bg-[#E3F2FD] hover:border-[#BBDEFB] transition-colors cursor-pointer disabled:opacity-50 shadow-2xs"
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
                      className="inline-flex items-center justify-center h-8 w-8 rounded-lg text-[#64748B] hover:text-[#DC2626] hover:bg-rose-50 transition-colors cursor-pointer disabled:opacity-50"
                      title="Dismiss notification"
                    >
                      <TrashIcon className="h-4 w-4" />
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
