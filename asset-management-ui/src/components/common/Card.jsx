import React from "react";

/**
 * Common Reusable Card Component
 * 
 * Consistent card container used across pages for dashboards, summaries, and widgets.
 */
export default function Card({
  children,
  title,
  subtitle,
  action,
  headerBorder = true,
  hoverable = false,
  className = "",
  bodyClassName = "",
  ...props
}) {
  return (
    <div
      className={`rounded-2xl border border-[#DCE5E1] bg-white shadow-sm transition-all duration-200 ${
        hoverable ? "hover:-translate-y-0.5 hover:shadow-md cursor-pointer" : ""
      } ${className}`}
      {...props}
    >
      {(title || subtitle || action) && (
        <div
          className={`flex items-center justify-between px-6 py-4.5 ${
            headerBorder ? "border-b border-[#EBF0EE]" : ""
          }`}
        >
          <div className="space-y-0.5">
            {title && (
              <h3 className="text-base font-bold text-[#03312E]">{title}</h3>
            )}
            {subtitle && (
              <p className="text-xs text-[#64748B]">{subtitle}</p>
            )}
          </div>
          {action && <div className="shrink-0">{action}</div>}
        </div>
      )}

      <div className={`p-6 ${bodyClassName}`}>{children}</div>
    </div>
  );
}
