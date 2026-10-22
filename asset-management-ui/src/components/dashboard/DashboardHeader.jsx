export default function DashboardHeader({ data }) {
  return (
    <header className="flex flex-col justify-between gap-2 sm:flex-row sm:items-center">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-[#03312E]">
          {data.title}
        </h1>
        <p className="mt-1 text-sm text-[#64748B]">
          {data.subtitle}
        </p>
      </div>

      {/* Timestamp indicator */}
      <div className="flex items-center gap-1.5 self-start rounded-lg border border-[#DCE5E1] bg-white px-3 py-1.5 text-xs text-[#64748B] shadow-sm sm:self-auto">
        <span className="h-2 w-2 rounded-full bg-[#00C896]" />
        <span>Last updated: {data.lastUpdated}</span>
      </div>
    </header>
  );
}
