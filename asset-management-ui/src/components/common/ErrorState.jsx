import React from "react";
import Button from "./Button";

/**
 * Reusable ErrorState component for consistent, friendly error handling across the application.
 *
 * @param {string} title - Short heading (e.g. "We ran into an issue")
 * @param {string} message - User-friendly, casual explanation without technical jargon
 * @param {Function} onRetry - Optional callback triggered on retry
 * @param {string} retryLabel - Button label (defaults to "Try Again")
 * @param {React.ReactNode} action - Custom action button or link if different from retry
 * @param {"card" | "banner" | "page"} variant - Presentation style
 * @param {string} className - Additional CSS classes
 */
export default function ErrorState({
  title = "Something went wrong",
  message = "We couldn't load this information right now. Please check your connection and try again.",
  onRetry,
  retryLabel = "Try Again",
  action,
  variant = "card",
  className = "",
}) {
  if (variant === "banner") {
    return (
      <div
        role="alert"
        className={`flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 rounded-2xl border border-rose-200 bg-rose-50/80 p-4 text-xs text-rose-800 shadow-xs ${className}`}
      >
        <div className="flex items-center gap-3 min-w-0">
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-rose-100 text-rose-600">
            <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
          </div>
          <div className="min-w-0">
            {title && <p className="font-bold text-[#03312E]">{title}</p>}
            <p className="text-[#64748B]">{message}</p>
          </div>
        </div>

        <div className="shrink-0">
          {action ? (
            action
          ) : onRetry ? (
            <Button variant="outline" size="sm" onClick={onRetry}>
              {retryLabel}
            </Button>
          ) : null}
        </div>
      </div>
    );
  }

  // Card or Page variant (centered layout)
  const isPage = variant === "page";

  return (
    <div
      role="alert"
      className={`flex flex-col items-center justify-center text-center rounded-2xl border border-rose-200/80 bg-rose-50/50 p-8 sm:p-12 shadow-xs transition-all ${
        isPage ? "min-h-[400px]" : ""
      } ${className}`}
    >
      {/* Icon */}
      <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-rose-100/80 text-rose-600 shadow-2xs">
        <svg
          className="h-7 w-7"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
      </div>

      {/* Title */}
      <h3 className="text-base font-bold text-[#03312E]">{title}</h3>

      {/* Message */}
      {message && (
        <p className="mt-1 text-xs text-[#64748B] max-w-md mx-auto leading-relaxed">
          {message}
        </p>
      )}

      {/* Actions */}
      <div className="mt-5 flex items-center justify-center gap-3">
        {action ? (
          action
        ) : onRetry ? (
          <Button variant="primary" size="sm" onClick={onRetry}>
            <svg
              className="mr-1.5 h-3.5 w-3.5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
              <path d="M3 3v5h5" />
              <path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16" />
              <path d="M16 21h5v-5" />
            </svg>
            {retryLabel}
          </Button>
        ) : null}
      </div>
    </div>
  );
}

