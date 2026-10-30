import React from "react";
import { Link, useNavigate } from "react-router-dom";

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[#F4F7F6] flex flex-col justify-center items-center px-4 sm:px-6 lg:px-8 py-12 select-none">
      <div className="w-full max-w-lg text-center bg-white rounded-3xl border border-[#DCE5E1] p-8 sm:p-10 shadow-xl shadow-slate-900/5 transition-all">
        {/* Brand Icon / Illustration Container */}
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-2xl bg-[#EBF5FF] text-[#1976D2] mb-6 shadow-xs ring-8 ring-[#F0F7FF]">
          <svg
            className="h-10 w-10"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.75"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        </div>

        {/* 404 Badge */}
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-700 text-xs font-semibold mb-4 tracking-wide uppercase">
          <span className="h-2 w-2 rounded-full bg-[#1976D2]"></span>
          HTTP Error 404
        </div>

        {/* Heading */}
        <h1 className="text-2xl sm:text-3xl font-extrabold text-[#03312E] tracking-tight mb-2">
          Page Not Found
        </h1>

        {/* Professional Body Copy */}
        <p className="text-sm text-[#64748B] leading-relaxed mb-8 max-w-md mx-auto">
          The requested page could not be located. It may have been relocated, renamed, or is currently unavailable. Please verify the web address or navigate back to the dashboard.
        </p>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl border border-[#DCE5E1] bg-white px-5 py-2.5 text-xs font-semibold text-[#03312E] shadow-sm hover:bg-[#F8FAFC] hover:border-[#CBD5E1] transition-all cursor-pointer focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
          >
            <svg
              className="h-4 w-4 text-[#64748B]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <line x1="19" y1="12" x2="5" y2="12" />
              <polyline points="12 19 5 12 12 5" />
            </svg>
            Go Back
          </button>

          <Link
            to="/dashboard"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl bg-[#03312E] px-5 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-[#022422] transition-all cursor-pointer focus:outline-none focus:ring-2 focus:ring-[#03312E]/30"
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
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
            Return to Dashboard
          </Link>
        </div>

        {/* Footer Support Tagline */}
        <div className="mt-8 pt-6 border-t border-[#F1F5F9] text-center">
          <p className="text-[11px] text-[#94A3B8]">
            TechAura IT Asset Management &bull; Enterprise Fleet Operations
          </p>
        </div>
      </div>
    </div>
  );
}

