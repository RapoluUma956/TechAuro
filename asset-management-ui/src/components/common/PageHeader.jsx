import React from "react";

export default function PageHeader({
  title,
  subtitle,
  actions = null,
  breadcrumbs = null,
  badge = null,
  className = "",
}) {
  return (
    <header className={`space-y-2 ${className}`}>
      {/* Optional Breadcrumbs */}
      {breadcrumbs && breadcrumbs.length > 0 && (
        <nav aria-label="Breadcrumb" className="flex items-center gap-2 text-xs text-[#64748B]">
          {breadcrumbs.map((crumb, idx) => {
            const isLast = idx === breadcrumbs.length - 1;
            return (
              <React.Fragment key={crumb.label || idx}>
                {idx > 0 && <span>/</span>}
                {isLast ? (
                  <span className="font-semibold text-[#03312E]">{crumb.label}</span>
                ) : (
                  <button
                    type="button"
                    onClick={crumb.onClick}
                    className="hover:text-[#1976D2] cursor-pointer transition-colors"
                  >
                    {crumb.label}
                  </button>
                )}
              </React.Fragment>
            );
          })}
        </nav>
      )}

      {/* Title, Subtitle, and Actions */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-1">
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-extrabold tracking-tight text-[#03312E] sm:text-3xl">
              {title}
            </h1>
            {badge && <div>{badge}</div>}
          </div>
          {subtitle && (
            <p className="text-sm font-normal text-[#5C7370] max-w-3xl">
              {subtitle}
            </p>
          )}
        </div>

        {actions && <div className="flex shrink-0 items-center gap-3">{actions}</div>}
      </div>
    </header>
  );
}
