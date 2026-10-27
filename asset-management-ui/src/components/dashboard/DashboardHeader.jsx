function formatLastUpdated(dateString) {
  if (!dateString) return "No agent reports yet";
  try {
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return dateString;
    return d.toLocaleString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
    });
  } catch {
    return dateString;
  }
}

export default function DashboardHeader({
  data = {},
  lastUpdatedAt,
  onRefresh,
  onOpenThresholds,
  refreshing = false,
}) {
  const title = data.title || "Dashboard";
  const subtitle = data.subtitle || "Overview of your IT infrastructure and security posture";
  const formattedTimestamp = formatLastUpdated(lastUpdatedAt || data.lastUpdated);

  return (
    <header className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-[#03312E]">
          {title}
        </h1>
        <p className="mt-1 text-sm text-[#64748B]">
          {subtitle}
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-2 self-start sm:self-auto">
        {/* Timestamp indicator */}
        <div className="flex items-center gap-1.5 rounded-lg border border-[#DCE5E1] bg-white px-3 py-1.5 text-xs text-[#64748B] shadow-sm">
          <span className={`h-2 w-2 rounded-full ${lastUpdatedAt ? "bg-[#00C896]" : "bg-[#94A3B8]"}`} />
          <span>Last updated: {formattedTimestamp}</span>
        </div>

        {/* Refresh Button */}
        {onRefresh && (
          <button
            type="button"
            onClick={onRefresh}
            disabled={refreshing}
            className="flex items-center gap-1.5 rounded-lg border border-[#DCE5E1] bg-white px-3 py-1.5 text-xs font-semibold text-[#64748B] shadow-sm transition-all hover:bg-[#F8FAFC] hover:text-[#03312E] disabled:opacity-50"
            title="Refresh dashboard metrics"
          >
            <svg
              className={`h-3.5 w-3.5 ${refreshing ? "animate-spin text-[#1976D2]" : ""}`}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67" />
            </svg>
            <span className="hidden sm:inline">Refresh</span>
          </button>
        )}

        {/* Threshold Settings Trigger */}
        {onOpenThresholds && (
          <button
            type="button"
            onClick={onOpenThresholds}
            className="flex items-center gap-1.5 rounded-lg border border-[#DCE5E1] bg-white px-3 py-1.5 text-xs font-semibold text-[#1976D2] shadow-sm transition-all hover:bg-[#EBF5FF]"
            title="Configure health and alert thresholds"
          >
            <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="4" y1="21" x2="4" y2="14" />
              <line x1="4" y1="10" x2="4" y2="3" />
              <line x1="12" y1="21" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12" y2="3" />
              <line x1="20" y1="21" x2="20" y2="16" />
              <line x1="20" y1="12" x2="20" y2="3" />
              <line x1="1" y1="14" x2="7" y2="14" />
              <line x1="9" y1="8" x2="15" y2="8" />
              <line x1="17" y1="16" x2="23" y2="16" />
            </svg>
            <span>Thresholds</span>
          </button>
        )}
      </div>
    </header>
  );
}
