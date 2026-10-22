import React from "react";

export default function StatusBadge({
  status = "Unknown",
  variant = "dot", // "dot" | "pill"
  className = "",
  customColors,
}) {
  const normalized = String(status || "").toLowerCase().trim();

  // Color mappings
  const colorMap = {
    // Green / Positive
    active: {
      bg: "bg-emerald-50",
      text: "text-emerald-700",
      border: "border-emerald-200",
      dot: "bg-emerald-500",
      pulse: true,
    },
    healthy: {
      bg: "bg-emerald-50",
      text: "text-emerald-700",
      border: "border-emerald-200",
      dot: "bg-emerald-500",
      pulse: false,
    },
    online: {
      bg: "bg-emerald-50",
      text: "text-emerald-700",
      border: "border-emerald-200",
      dot: "bg-emerald-500",
      pulse: true,
    },
    // Orange / Warning
    repair: {
      bg: "bg-amber-50",
      text: "text-amber-700",
      border: "border-amber-200",
      dot: "bg-amber-500",
      pulse: false,
    },
    warning: {
      bg: "bg-amber-50",
      text: "text-amber-700",
      border: "border-amber-200",
      dot: "bg-amber-500",
      pulse: false,
    },
    pending: {
      bg: "bg-amber-50",
      text: "text-amber-700",
      border: "border-amber-200",
      dot: "bg-amber-500",
      pulse: false,
    },
    // Red / Danger
    inactive: {
      bg: "bg-rose-50",
      text: "text-rose-700",
      border: "border-rose-200",
      dot: "bg-rose-500",
      pulse: false,
    },
    critical: {
      bg: "bg-rose-50",
      text: "text-rose-700",
      border: "border-rose-200",
      dot: "bg-rose-500",
      pulse: true,
    },
    offline: {
      bg: "bg-rose-50",
      text: "text-rose-700",
      border: "border-rose-200",
      dot: "bg-rose-500",
      pulse: false,
    },
    expired: {
      bg: "bg-rose-50",
      text: "text-rose-700",
      border: "border-rose-200",
      dot: "bg-rose-500",
      pulse: false,
    },
    // Blue / Info
    info: {
      bg: "bg-blue-50",
      text: "text-blue-700",
      border: "border-blue-200",
      dot: "bg-blue-500",
      pulse: false,
    },
    assigned: {
      bg: "bg-blue-50",
      text: "text-blue-700",
      border: "border-blue-200",
      dot: "bg-blue-500",
      pulse: false,
    },
  };

  const scheme = customColors || colorMap[normalized] || {
    bg: "bg-slate-50",
    text: "text-slate-700",
    border: "border-slate-200",
    dot: "bg-slate-400",
    pulse: false,
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-semibold shadow-2xs select-none ${
        scheme.bg
      } ${scheme.text} ${scheme.border} ${className}`}
    >
      {variant === "dot" && (
        <span
          className={`h-1.5 w-1.5 rounded-full ${scheme.dot} ${
            scheme.pulse ? "animate-pulse" : ""
          }`}
        />
      )}
      <span>{status}</span>
    </span>
  );
}
