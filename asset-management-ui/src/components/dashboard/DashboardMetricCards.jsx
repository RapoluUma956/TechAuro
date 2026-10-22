import { StatIcon } from "./DashboardIcons";

export default function DashboardMetricCards({ statistics }) {
  return (
    <section className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {statistics.map((stat) => (
        <article
          key={stat.id}
          className="flex items-center gap-4 rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-sm transition-all hover:shadow-md"
        >
          <div
            className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl"
            style={{ backgroundColor: stat.iconBg }}
          >
            <StatIcon type={stat.icon} color={stat.iconColor} />
          </div>

          <div>
            <p className="text-xs font-semibold text-[#64748B]">
              {stat.title}
            </p>
            <p className="mt-1 text-3xl font-extrabold tracking-tight text-[#03312E]">
              {stat.value}
            </p>
            <p className="mt-0.5 text-xs text-[#64748B]">
              {stat.caption}
            </p>
          </div>
        </article>
      ))}
    </section>
  );
}
