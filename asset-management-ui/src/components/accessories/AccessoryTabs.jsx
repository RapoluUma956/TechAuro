import React from "react";
import { AccessoryPackageIcon, LaptopIcon, LayersIcon } from "./AccessoryIcons";

export default function AccessoryTabs({
  activeTab = "accessories",
  onTabChange,
  accessoryCount = 0,
  laptopCount = 0,
}) {
  const tabs = [
    {
      id: "accessories",
      label: "Accessory Inventory",
      count: accessoryCount,
      icon: <AccessoryPackageIcon className="h-4 w-4" />,
    },
    {
      id: "laptops",
      label: "Laptop Configurations",
      count: laptopCount,
      icon: <LaptopIcon className="h-4 w-4" />,
    },
    {
      id: "both",
      label: "Show Both Tables",
      count: accessoryCount + laptopCount,
      icon: <LayersIcon className="h-4 w-4" />,
    },
  ];

  return (
    <div className="flex flex-col gap-3 rounded-2xl border border-[#DCE5E1] bg-white p-1.5 shadow-2xs sm:flex-row sm:items-center sm:justify-between">
      <nav className="flex flex-wrap items-center gap-1.5" aria-label="Accessory Views">
        {tabs.map((tab) => {
          const isActive = activeTab === tab.id;

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onTabChange(tab.id)}
              className={`group flex items-center gap-2.5 rounded-xl px-4 py-2.5 text-xs font-bold transition-all duration-150 cursor-pointer ${
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

              {tab.count !== undefined && (
                <span
                  className={`inline-flex items-center justify-center rounded-full px-2 py-0.5 text-xs font-bold font-mono transition-colors ${
                    isActive
                      ? "bg-white/20 text-white"
                      : "bg-[#EBF0EE] text-[#5C7370] group-hover:bg-[#DCE5E1]"
                  }`}
                >
                  {tab.count}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      <div className="hidden items-center gap-2 pr-4 text-xs font-semibold text-[#5C7370] lg:flex">
        <span className="relative flex h-2 w-2">
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-[#00C896] opacity-75" />
          <span className="relative inline-flex h-2 w-2 rounded-full bg-[#00C896]" />
        </span>
        <span>Real-time Asset Inventory</span>
      </div>
    </div>
  );
}
