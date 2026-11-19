import React from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  UsersIcon,
  LaptopIcon,
  KeyIcon,
  BellIcon,
  ArrowLeftIcon,
} from "../../components/common/Icons";

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="app-content-background min-h-screen flex flex-col justify-center items-center p-6 select-none font-['Manrope']">
      <div className="w-full max-w-2xl text-center bg-white rounded-2xl border border-[#DCE5E1] p-8 sm:p-12 shadow-sm transition-all">
        {/* Top 404 Visual Pill */}
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-2xl bg-[#EBF7F2] text-[#03312E] mb-5 shadow-xs border border-[#DCE5E1]">
          <span className="font-mono text-3xl font-extrabold tracking-tight text-[#03312E]">
            404
          </span>
        </div>

        {/* Status Badge matching app StatusBadges */}
        <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-[#EBF7F2] border border-[#A9D4C0] text-[#03312E] text-xs font-bold mb-3 tracking-wide">
          <span className="h-2 w-2 rounded-full bg-[#00C896] animate-pulse"></span>
          Page Not Found
        </div>

        {/* Heading */}
        <h1 className="text-2xl sm:text-3xl font-extrabold text-[#03312E] tracking-tight mb-2">
          We couldn't find that page
        </h1>

        {/* Body Text in app colors */}
        <p className="text-sm text-[#5C7370] leading-relaxed mb-8 max-w-md mx-auto">
          The link you followed might be broken, or the page may have moved. You can head back to the previous screen or jump directly to any section below.
        </p>

        {/* Action Buttons styled with app buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl border border-[#DCE5E1] bg-white px-5 py-2.5 text-xs font-bold text-[#03312E] shadow-xs hover:bg-[#F8FAF9] transition-all cursor-pointer"
          >
            <ArrowLeftIcon className="h-4 w-4 text-[#5C7370]" />
            Go Back
          </button>

          <Link
            to="/dashboard"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl bg-[#03312E] px-6 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-[#022422] transition-all cursor-pointer"
          >
            <svg
              className="h-4 w-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <rect x="3" y="3" width="7" height="7" rx="1" />
              <rect x="14" y="3" width="7" height="7" rx="1" />
              <rect x="14" y="14" width="7" height="7" rx="1" />
              <rect x="3" y="14" width="7" height="7" rx="1" />
            </svg>
            Return to Dashboard
          </Link>
        </div>

        {/* Quick Navigation Cards matching app styling */}
        <div className="mt-8 pt-6 border-t border-[#DCE5E1]">
          <p className="text-xs font-bold uppercase tracking-wider text-[#64748B] mb-4">
            Quick Navigation
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-left">
            <Link
              to="/employees"
              className="flex items-center gap-3 p-3.5 rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] hover:bg-white hover:border-[#1976D2] hover:shadow-xs transition-all group"
            >
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2] group-hover:scale-105 transition-transform">
                <UsersIcon className="h-5 w-5" />
              </div>
              <div className="min-w-0">
                <p className="text-xs font-bold text-[#03312E] truncate">Employees</p>
                <p className="text-[11px] text-[#5C7370] truncate">Personnel directory</p>
              </div>
            </Link>

            <Link
              to="/accessories"
              className="flex items-center gap-3 p-3.5 rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] hover:bg-white hover:border-[#1976D2] hover:shadow-xs transition-all group"
            >
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#E8F8F2] text-[#00C896] group-hover:scale-105 transition-transform">
                <LaptopIcon className="h-5 w-5" />
              </div>
              <div className="min-w-0">
                <p className="text-xs font-bold text-[#03312E] truncate">Hardware</p>
                <p className="text-[11px] text-[#5C7370] truncate">Laptops & items</p>
              </div>
            </Link>

            <Link
              to="/licenses"
              className="flex items-center gap-3 p-3.5 rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] hover:bg-white hover:border-[#1976D2] hover:shadow-xs transition-all group"
            >
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#FFF4E5] text-[#F97316] group-hover:scale-105 transition-transform">
                <KeyIcon className="h-5 w-5" />
              </div>
              <div className="min-w-0">
                <p className="text-xs font-bold text-[#03312E] truncate">Licenses</p>
                <p className="text-[11px] text-[#5C7370] truncate">Software & seats</p>
              </div>
            </Link>

            <Link
              to="/notifications"
              className="flex items-center gap-3 p-3.5 rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] hover:bg-white hover:border-[#1976D2] hover:shadow-xs transition-all group"
            >
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#FBEAEF] text-[#750D37] group-hover:scale-105 transition-transform">
                <BellIcon className="h-5 w-5" />
              </div>
              <div className="min-w-0">
                <p className="text-xs font-bold text-[#03312E] truncate">Security Alerts</p>
                <p className="text-[11px] text-[#5C7370] truncate">Audits & events</p>
              </div>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
