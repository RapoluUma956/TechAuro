import React from "react";
import { Link, useNavigate } from "react-router-dom";

export default function Unauthorized({ moduleName }) {
  const navigate = useNavigate();

  return (
    <div className="min-h-[70vh] flex flex-col justify-center items-center px-4 sm:px-6 lg:px-8 py-12 select-none">
      <div className="w-full max-w-lg text-center bg-white rounded-3xl border border-[#DCE5E1] p-8 sm:p-10 shadow-xl shadow-slate-900/5 transition-all">
        {/* Brand Icon Container */}
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-2xl bg-amber-50 text-amber-600 mb-6 ring-8 ring-amber-50/50">
          <svg
            className="h-10 w-10"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.75"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <rect width="18" height="11" x="3" y="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
        </div>

        {/* 403 Badge */}
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-50 border border-amber-200 text-amber-800 text-xs font-semibold mb-4 tracking-wide uppercase">
          <span className="h-2 w-2 rounded-full bg-amber-500"></span>
          Access Restricted &bull; 403 Forbidden
        </div>

        {/* Heading */}
        <h1 className="text-2xl sm:text-3xl font-extrabold text-[#03312E] tracking-tight mb-2">
          Permission Required
        </h1>

        {/* Body Copy */}
        <p className="text-sm text-[#64748B] leading-relaxed mb-8 max-w-md mx-auto">
          You do not have administrative permission to access {moduleName ? `the ${moduleName} module` : "this page"}. Please contact your IT administrator if you believe your assigned role requires this privilege.
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
            TechAura Role-Based Access Control &bull; Enterprise Fleet Operations
          </p>
        </div>
      </div>
    </div>
  );
}

