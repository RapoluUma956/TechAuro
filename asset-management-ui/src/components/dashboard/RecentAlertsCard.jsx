import { Link } from "react-router-dom";
import { StatIcon } from "./DashboardIcons";

function formatRelativeTime(dateString) {
  if (!dateString) return "Now";
  try {
    const diffMs = Date.now() - new Date(dateString).getTime();
    if (diffMs < 0 || diffMs < 60000) return "Now";
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 60) return `${minutes}m`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h`;
    const days = Math.floor(hours / 24);
    return `${days}d`;
  } catch {
    return "Now";
  }
}

function resolveAlertStyling(alert) {
  const type = (alert.type || "").toUpperCase();
  const severity = (alert.severity || "").toUpperCase();

  let icon = "shield-alert";
  if (type === "VULNERABILITY") icon = "shield-alert";
  else if (type === "DISK") icon = "database";
  else if (type === "DEVICE") icon = "wifi";
  else if (type === "LICENSE") icon = "key";
  else if (type === "WARRANTY") icon = "shield-check";
  else if (alert.icon) icon = alert.icon;

  let iconBg = "#EBF5FF";
  let iconColor = "#1976D2";

  if (severity === "CRITICAL") {
    iconBg = "#FEE2E2";
    iconColor = "#DC2626";
  } else if (severity === "WARNING") {
    iconBg = "#FEF3C7";
    iconColor = "#D97706";
  } else if (type === "WARRANTY") {
    iconBg = "#D1FAE5";
    iconColor = "#059669";
  } else if (type === "DEVICE") {
    iconBg = "#DBEAFE";
    iconColor = "#2563EB";
  }

  return {
    icon: alert.icon || icon,
    iconBg: alert.iconBg || iconBg,
    iconColor: alert.iconColor || iconColor,
  };
}

export default function RecentAlertsCard({ data, loading = false }) {
  const title = data?.title || "Recent Alerts";
  const viewAllLink = data?.viewAllLink || "/notifications";
  const rawItems = data?.items || [];

  return (
    <article className="rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <h2 className="text-base font-bold text-[#03312E]">
          {title}
        </h2>
        <Link
          to={viewAllLink}
          className="text-xs font-semibold text-[#1976D2] hover:underline"
        >
          View All
        </Link>
      </div>

      <div className="mt-4 divide-y divide-[#F1F5F9]">
        {loading ? (
          <div className="space-y-3 py-2">
            {[1, 2, 3].map((i) => (
              <div key={i} className="flex items-center gap-3 animate-pulse">
                <div className="h-8 w-8 rounded-full bg-[#E2E8F0]" />
                <div className="flex-1 space-y-1.5">
                  <div className="h-3 w-3/4 rounded bg-[#E2E8F0]" />
                  <div className="h-2.5 w-1/2 rounded bg-[#F1F5F9]" />
                </div>
              </div>
            ))}
          </div>
        ) : rawItems.length > 0 ? (
          rawItems.map((alert) => {
            const styling = resolveAlertStyling(alert);
            const displayTime = alert.time || formatRelativeTime(alert.createdAt);
            const description = alert.message || alert.description || "";

            return (
              <div
                key={alert.id}
                className="flex items-start justify-between gap-3 py-3.5 first:pt-0 last:pb-0"
              >
                <div className="flex items-start gap-3 min-w-0">
                  <div
                    className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full"
                    style={{ backgroundColor: styling.iconBg }}
                  >
                    <StatIcon type={styling.icon} color={styling.iconColor} />
                  </div>
                  <div className="min-w-0">
                    <p className="truncate text-xs font-bold text-[#03312E]">
                      {alert.title}
                    </p>
                    <p className="truncate text-[11px] text-[#64748B]">
                      {description}
                    </p>
                    {alert.assetTag && (
                      <span className="inline-block mt-0.5 text-[10px] font-semibold text-[#1976D2]">
                        {alert.assetTag}
                      </span>
                    )}
                  </div>
                </div>

                <span className="shrink-0 text-[10px] font-medium text-[#94A3B8]">
                  {displayTime}
                </span>
              </div>
            );
          })
        ) : (
          <div className="py-6 text-center text-xs text-[#94A3B8]">
            No recent alerts. All systems are operational.
          </div>
        )}
      </div>
    </article>
  );
}
