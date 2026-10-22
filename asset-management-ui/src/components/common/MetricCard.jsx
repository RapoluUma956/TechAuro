import React from "react";

export function MetricCard({
  title,
  value,
  caption,
  icon: IconComponent,
  iconColor = "#1976D2",
  iconBg = "#E3F2FD",
  trend,
  onClick,
  className = "",
}) {
  const isClickable = Boolean(onClick);

  return (
    <article
      onClick={onClick}
      className={`flex items-center gap-4 rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md ${
        isClickable ? "cursor-pointer" : ""
      } ${className}`}
    >
      {/* Icon Container */}
      {IconComponent && (
        <div
          className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl transition-transform hover:scale-105"
          style={{ backgroundColor: iconBg }}
        >
          {typeof IconComponent === "function" ? (
            <IconComponent color={iconColor} className="h-6 w-6" />
          ) : (
            IconComponent
          )}
        </div>
      )}

      {/* Content */}
      <div className="flex-1 min-w-0">
        <p className="text-xs font-semibold uppercase tracking-wider text-[#64748B] truncate">
          {title}
        </p>
        <div className="mt-1 flex items-baseline gap-2">
          <p className="text-3xl font-extrabold tracking-tight text-[#03312E]">
            {value}
          </p>
          {trend && (
            <span
              className={`text-xs font-bold ${
                trend.isPositive ? "text-emerald-600" : "text-rose-600"
              }`}
            >
              {trend.value}
            </span>
          )}
        </div>
        {caption && (
          <p className="mt-0.5 text-xs text-[#64748B] truncate">
            {caption}
          </p>
        )}
      </div>
    </article>
  );
}

export default function MetricCards({
  items = [],
  children,
  columns = 4,
  className = "",
}) {
  const colStyles = {
    2: "sm:grid-cols-2",
    3: "sm:grid-cols-2 lg:grid-cols-3",
    4: "sm:grid-cols-2 lg:grid-cols-4",
  };

  return (
    <section
      className={`grid grid-cols-1 gap-5 ${
        colStyles[columns] || colStyles[4]
      } ${className}`}
    >
      {items.length > 0
        ? items.map((item, index) => (
            <MetricCard key={item.id || item.title || index} {...item} />
          ))
        : children}
    </section>
  );
}
