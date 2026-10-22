import React from "react";
import { resolveColor } from "../../utils/rangeColorUtils";

/**
 * Common Reusable Donut Chart Component
 * 
 * Can be rendered either as a full dashboard card or as a standalone SVG chart.
 * 
 * Props:
 * - `data`: Object containing { title, total, centerLabel, segments }
 * - `title`: Optional title for card mode
 * - `total`: Center number (auto-calculated from segment counts/values if omitted)
 * - `centerLabel`: Label beneath the center number (default: "Total")
 * - `segments`: Array of { id, label, value, count, percentage, color }
 * - `asCard`: Boolean. If true (or if title is present and asCard not explicitly false), renders in card container
 * - `size`: Chart diameter size: "sm" (110px), "md" (144px), "lg" (180px), or custom number in px (default: "md")
 * - `thickness`: Donut ring stroke thickness (default: 12)
 * - `showLegend`: Whether to display the segments legend (default: true)
 * - `legendLayout`: "vertical" | "horizontal" | "grid" (default: "vertical")
 * - `className`: Extra CSS classes
 */
export default function DonutChart({
  data,
  title: propTitle,
  total: propTotal,
  centerLabel: propCenterLabel,
  segments: propSegments,
  asCard,
  size = "md",
  thickness = 12,
  showLegend = true,
  legendLayout = "vertical",
  className = "",
}) {
  const title = propTitle ?? data?.title;
  const rawSegments = propSegments ?? data?.segments ?? [];
  const rawTotal = propTotal ?? data?.total;
  const centerLabel = propCenterLabel ?? data?.centerLabel ?? "Total";
  const shouldRenderCard = asCard ?? Boolean(title);

  // Calculate sum of counts/values if total is not specified
  const calculatedTotal = rawSegments.reduce((sum, s) => {
    const val = Number(s.value !== undefined ? s.value : s.count) || 0;
    return sum + val;
  }, 0);
  const total = rawTotal !== undefined ? rawTotal : calculatedTotal;

  // Geometry
  const radius = 38;
  const circumference = 2 * Math.PI * radius; // ≈ 238.76

  let accumulatedDash = 0;
  const computedSegments = rawSegments.map((segment) => {
    const count = Number(segment.value !== undefined ? segment.value : segment.count) || 0;
    const percentage =
      segment.percentage !== undefined
        ? Number(segment.percentage)
        : total > 0
        ? Math.round((count / total) * 100)
        : 0;

    const strokeLength = (percentage / 100) * circumference;
    const strokeDasharray = `${strokeLength.toFixed(2)} ${circumference.toFixed(2)}`;
    const strokeDashoffset = (-accumulatedDash).toFixed(2);
    accumulatedDash += strokeLength;

    return {
      ...segment,
      count,
      percentage,
      resolvedColor: resolveColor(segment.color),
      strokeDasharray,
      strokeDashoffset,
    };
  });

  const sizeDimensions = {
    sm: "h-28 w-28",
    md: "h-36 w-36",
    lg: "h-44 w-44",
  };
  const sizeClass = typeof size === "number" ? "" : sizeDimensions[size] || sizeDimensions.md;
  const sizeStyle = typeof size === "number" ? { width: `${size}px`, height: `${size}px` } : {};

  const chartContent = (
    <div className="flex flex-col items-center justify-between gap-4 w-full">
      {/* SVG Donut Ring */}
      <div
        className={`relative flex shrink-0 items-center justify-center ${sizeClass}`}
        style={sizeStyle}
      >
        <svg
          className="h-full w-full -rotate-90 transform"
          viewBox="0 0 100 100"
          aria-label={title ? `${title} donut chart` : "Donut chart"}
        >
          {/* Background Track */}
          <circle
            cx="50"
            cy="50"
            r={radius}
            fill="none"
            stroke="#F1F5F9"
            strokeWidth={thickness}
          />

          {/* Dynamic Segments */}
          {computedSegments.map((seg) => (
            <circle
              key={seg.id || seg.label}
              cx="50"
              cy="50"
              r={radius}
              fill="none"
              stroke={seg.resolvedColor}
              strokeWidth={thickness}
              strokeDasharray={seg.strokeDasharray}
              strokeDashoffset={seg.strokeDashoffset}
              className="transition-all duration-500"
            />
          ))}
        </svg>

        {/* Center Information */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center px-2">
          <span className="text-2xl font-black text-[#03312E] leading-tight">
            {total}
          </span>
          <span className="text-[10px] font-semibold text-[#64748B] uppercase tracking-wider">
            {centerLabel}
          </span>
        </div>
      </div>

      {/* Legend / Information List */}
      {showLegend && computedSegments.length > 0 && (
        <div
          className={`w-full text-xs ${
            legendLayout === "horizontal"
              ? "flex flex-wrap items-center justify-center gap-4"
              : legendLayout === "grid"
              ? "grid grid-cols-2 gap-2"
              : "space-y-2"
          }`}
        >
          {computedSegments.map((seg) => (
            <div
              key={seg.id || seg.label}
              className="flex items-center justify-between gap-2"
            >
              <div className="flex items-center gap-2 min-w-0">
                <span
                  className="h-2.5 w-2.5 shrink-0 rounded-full"
                  style={{ backgroundColor: seg.resolvedColor }}
                />
                <span className="truncate font-medium text-[#64748B]">
                  {seg.label}
                </span>
              </div>
              <span className="shrink-0 font-bold text-[#03312E]">
                {seg.count} ({seg.percentage}%)
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );

  if (shouldRenderCard) {
    return (
      <article
        className={`rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm transition-all hover:shadow-md ${className}`}
      >
        {title && (
          <h2 className="mb-4 text-base font-bold text-[#03312E]">{title}</h2>
        )}
        {chartContent}
      </article>
    );
  }

  return <div className={`inline-block ${className}`}>{chartContent}</div>;
}
