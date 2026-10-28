import React from "react";
import {
  StackIcon,
  UsersIcon,
  UserSingleIcon,
  CheckCircleIcon,
  ClockAlertIcon,
} from "./LicenseIcons";

export default function LicenseMetricCards({ metrics, loading = false }) {
  const cards = [
    {
      id: "totalLicenses",
      title: "Total Licenses",
      value: loading ? "—" : (metrics?.totalLicenses ?? 0),
      caption: "Active software contracts",
      icon: StackIcon,
      iconColor: "#1976D2",
      iconBg: "#E3F2FD",
    },
    {
      id: "totalSeats",
      title: "Total Seats",
      value: loading ? "—" : (metrics?.totalSeats ?? 0),
      caption: "Purchased seat capacity",
      icon: UsersIcon,
      iconColor: "#00C896",
      iconBg: "#E6F7F2",
    },
    {
      id: "assignedSeats",
      title: "Assigned Seats",
      value: loading ? "—" : (metrics?.assignedSeats ?? 0),
      caption: "Allocated to staff",
      icon: UserSingleIcon,
      iconColor: "#10B981",
      iconBg: "#E8F8F2",
    },
    {
      id: "availableSeats",
      title: "Available Seats",
      value: loading ? "—" : (metrics?.availableSeats ?? 0),
      caption: "Ready for deployment",
      icon: CheckCircleIcon,
      iconColor: "#F59E0B",
      iconBg: "#FEF3C7",
    },
    {
      id: "expiringSoon",
      title: "Expiring Soon",
      value: loading ? "—" : (metrics?.expiringSoonCount ?? 0),
      caption: "(within 30 days)",
      icon: ClockAlertIcon,
      iconColor: "#EF4444",
      iconBg: "#FEE2E2",
      isAlert: true,
    },
  ];

  return (
    <section className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
      {cards.map((card) => {
        const IconComponent = card.icon;
        return (
          <article
            key={card.id}
            className="flex items-center gap-3.5 rounded-2xl border border-[#DCE5E1] bg-white p-4.5 shadow-2xs transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md"
          >
            {/* Icon Bubble */}
            <div
              className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl transition-transform hover:scale-105"
              style={{ backgroundColor: card.iconBg }}
            >
              <IconComponent color={card.iconColor} className="h-6 w-6" />
            </div>

            {/* Content */}
            <div className="min-w-0 flex-1">
              <p className="truncate text-xs font-semibold uppercase tracking-wider text-[#64748B]">
                {card.title}
              </p>
              <div className="mt-0.5 flex items-baseline gap-1.5">
                <span
                  className={`text-2xl font-extrabold tracking-tight sm:text-3xl ${
                    card.isAlert && (metrics?.expiringSoonCount ?? 0) > 0
                      ? "text-[#EF4444]"
                      : "text-[#03312E]"
                  }`}
                >
                  {card.value}
                </span>
                {card.caption && (
                  <span className="truncate text-xs font-normal text-[#64748B]">
                    {card.caption}
                  </span>
                )}
              </div>
            </div>
          </article>
        );
      })}
    </section>
  );
}
