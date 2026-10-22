import React from "react";

export const TABS = [
  {
    id: "device",
    label: "Device details",
    countKey: "device",
    icon: (
      <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="3" width="20" height="14" rx="2" />
        <line x1="8" y1="21" x2="16" y2="21" />
        <line x1="12" y1="17" x2="12" y2="21" />
      </svg>
    ),
  },
  {
    id: "accessories",
    label: "accessories",
    countKey: "accessories",
    icon: (
      <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
        <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
        <line x1="12" y1="22.08" x2="12" y2="12" />
      </svg>
    ),
  },
  {
    id: "software",
    label: "software",
    countKey: "softwares",
    icon: (
      <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="16 18 22 12 16 6" />
        <polyline points="8 6 2 12 8 18" />
      </svg>
    ),
  },
  {
    id: "vulnerabilities",
    label: "vulnerabilities",
    countKey: "vulnerabilities",
    icon: (
      <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
    ),
  },
  {
    id: "licences",
    label: "licences",
    countKey: "licenses",
    icon: (
      <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="m21 2-2 2m-1.5 1.5L14 9a5 5 0 1 0 4 4l3.5-3.5a1 1 0 0 0 0-1.41l-2-2a1 1 0 0 0-1.41 0l-2.09 2.09" />
        <circle cx="7.5" cy="16.5" r="1.5" />
      </svg>
    ),
  },
];

export default function EmployeeDetailTabs({
  activeTab = "device",
  onTabChange,
  counts = {},
}) {
  return (
    <div className="border-b border-[#DCE5E1] bg-white rounded-2xl p-1.5 shadow-2xs">
      <nav className="flex flex-wrap items-center gap-1.5" aria-label="Employee Tabs">
        {TABS.map((tab) => {
          const isActive = activeTab === tab.id;
          const count = counts[tab.countKey];

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onTabChange(tab.id)}
              className={`group flex items-center gap-2.5 rounded-xl px-4 py-2.5 text-sm font-semibold transition-all duration-150 cursor-pointer ${
                isActive
                  ? "bg-[#1976D2] text-white shadow-sm ring-1 ring-[#1976D2]"
                  : "text-[#5C7370] hover:bg-[#F1F5F9] hover:text-[#03312E]"
              }`}
            >
              <span
                className={`transition-colors ${
                  isActive ? "text-white" : "text-[#64748B] group-hover:text-[#03312E]"
                }`}
              >
                {tab.icon}
              </span>

              <span>{tab.label}</span>

              {count !== undefined && (
                <span
                  className={`inline-flex items-center justify-center rounded-full px-2 py-0.5 text-xs font-bold font-mono transition-colors ${
                    isActive
                      ? "bg-white/20 text-white"
                      : tab.id === "vulnerabilities" && count > 0
                      ? "bg-rose-100 text-rose-700"
                      : "bg-[#EBF0EE] text-[#5C7370] group-hover:bg-[#DCE5E1]"
                  }`}
                >
                  {count}
                </span>
              )}
            </button>
          );
        })}
      </nav>
    </div>
  );
}
