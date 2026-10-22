export default function DeviceConnectivityCard({ data }) {
  return (
    <article className="rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
      <h2 className="text-base font-bold text-[#03312E]">
        {data.title}
      </h2>

      <div className="mt-5">
        {/* Segmented Progress Bar */}
        <div className="flex h-4 w-full overflow-hidden rounded-full bg-[#E2E8F0]">
          <div
            className="h-full transition-all duration-500"
            style={{
              width: `${data.online.percentage}%`,
              backgroundColor: data.online.color,
            }}
          />
          <div
            className="h-full transition-all duration-500"
            style={{
              width: `${data.offline.percentage}%`,
              backgroundColor: data.offline.color,
            }}
          />
        </div>

        {/* Legend */}
        <div className="mt-4 flex flex-wrap items-center justify-between gap-2 text-xs font-semibold">
          <div className="flex items-center gap-2">
            <span className="h-2.5 w-2.5 rounded-full bg-[#00C896]" />
            <span className="text-[#03312E]">
              Online: {data.online.count} ({data.online.percentage}%)
            </span>
          </div>
          <div className="flex items-center gap-2">
            <span className="h-2.5 w-2.5 rounded-full bg-[#EF4444]" />
            <span className="text-[#03312E]">
              Offline: {data.offline.count} ({data.offline.percentage}%)
            </span>
          </div>
        </div>
      </div>
    </article>
  );
}
