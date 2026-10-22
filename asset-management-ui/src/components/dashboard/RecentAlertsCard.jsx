import { Link } from "react-router-dom";
import { StatIcon } from "./DashboardIcons";

export default function RecentAlertsCard({ data }) {
  return (
    <article className="rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <h2 className="text-base font-bold text-[#03312E]">
          {data.title}
        </h2>
        <Link
          to={data.viewAllLink}
          className="text-xs font-semibold text-[#1976D2] hover:underline"
        >
          View All
        </Link>
      </div>

      <div className="mt-4 divide-y divide-[#F1F5F9]">
        {data.items.map((alert) => (
          <div key={alert.id} className="flex items-start justify-between gap-3 py-3.5 first:pt-0 last:pb-0">
            <div className="flex items-start gap-3 min-w-0">
              <div
                className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full"
                style={{ backgroundColor: alert.iconBg }}
              >
                <StatIcon type={alert.icon} color={alert.iconColor} />
              </div>
              <div className="min-w-0">
                <p className="truncate text-xs font-bold text-[#03312E]">
                  {alert.title}
                </p>
                <p className="truncate text-[11px] text-[#64748B]">
                  {alert.description}
                </p>
              </div>
            </div>

            <span className="shrink-0 text-[10px] font-medium text-[#94A3B8]">
              {alert.time}
            </span>
          </div>
        ))}
      </div>
    </article>
  );
}
