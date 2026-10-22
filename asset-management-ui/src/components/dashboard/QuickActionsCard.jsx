import { Link } from "react-router-dom";
import { StatIcon } from "./DashboardIcons";

export default function QuickActionsCard({ data }) {
  return (
    <article className="rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
      <h2 className="text-base font-bold text-[#03312E]">
        {data.title}
      </h2>

      <div className="mt-4 space-y-2.5">
        {data.items.map((action) => (
          <Link
            key={action.id}
            to={action.link}
            className="group flex items-center justify-between rounded-xl border border-[#F1F5F9] bg-[#F8FAFC] p-3 text-xs font-semibold text-[#03312E] transition-all hover:border-[#1976D2] hover:bg-white hover:shadow-sm"
          >
            <div className="flex items-center gap-3">
              <div
                className="flex h-8 w-8 items-center justify-center rounded-lg"
                style={{ backgroundColor: action.iconBg }}
              >
                <StatIcon type={action.icon} color={action.iconColor} />
              </div>
              <span>{action.label}</span>
            </div>

            <svg
              className="h-4 w-4 text-[#94A3B8] transition-transform group-hover:translate-x-1 group-hover:text-[#1976D2]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <polyline points="9 18 15 12 9 6" />
            </svg>
          </Link>
        ))}
      </div>
    </article>
  );
}
