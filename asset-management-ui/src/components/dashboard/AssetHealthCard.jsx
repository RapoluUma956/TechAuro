export default function AssetHealthCard({ data }) {
  return (
    <article className="rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-sm">
      <h2 className="text-base font-bold text-[#03312E]">
        {data.title}
      </h2>

      <div className="mt-4 space-y-3">
        {data.items.map((item) => (
          <div key={item.id} className="flex items-center gap-3 text-xs">
            <div className="flex w-24 items-center gap-2">
              <span className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ backgroundColor: item.dotColor }} />
              <span className="font-medium text-[#64748B]">{item.label}</span>
            </div>

            <span className="w-16 font-bold text-[#03312E]">
              {item.count} ({item.percentage}%)
            </span>

            <div className="h-2.5 flex-1 overflow-hidden rounded-full bg-[#F1F5F9]">
              <div
                className="h-full rounded-full transition-all duration-500"
                style={{
                  width: `${item.percentage}%`,
                  backgroundColor: item.color,
                }}
              />
            </div>
          </div>
        ))}
      </div>
    </article>
  );
}
