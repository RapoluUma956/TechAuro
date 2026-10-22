import React, { useEffect } from "react";

/**
 * Common Reusable Modal Component
 * 
 * Features:
 * - Backdrop with smooth blur and enter animation
 * - Responsive sizing ("sm", "md", "lg", "xl", "full")
 * - Header with title, subtitle, and close 'X' button
 * - Optional footer slot for action buttons
 * - Escape key to close & backdrop click to close
 * - Body scroll locking when open
 */
export default function Modal({
  isOpen = false,
  onClose,
  title,
  subtitle,
  children,
  footer,
  size = "md", // "sm" | "md" | "lg" | "xl" | "full"
  closeOnBackdrop = true,
  closeOnEscape = true,
  showCloseButton = true,
  className = "",
}) {
  // Listen for Escape key to close modal
  useEffect(() => {
    if (!isOpen || !closeOnEscape) return;

    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        onClose && onClose();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, closeOnEscape, onClose]);

  // Lock body scroll when modal is open
  useEffect(() => {
    if (isOpen) {
      const originalStyle = window.getComputedStyle(document.body).overflow;
      document.body.style.overflow = "hidden";
      return () => {
        document.body.style.overflow = originalStyle;
      };
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const sizeClasses = {
    sm: "max-w-md",
    md: "max-w-lg",
    lg: "max-w-2xl",
    xl: "max-w-4xl",
    full: "max-w-6xl",
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget && closeOnBackdrop) {
      onClose && onClose();
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby={title ? "modal-title" : undefined}
      onClick={handleBackdropClick}
      className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-black/50 p-4 backdrop-blur-xs transition-opacity duration-200 animate-fadeIn"
    >
      <div
        className={`w-full overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white shadow-2xl transition-all duration-200 transform scale-100 ${
          sizeClasses[size] || sizeClasses.md
        } ${className}`}
      >
        {/* Modal Header */}
        {(title || showCloseButton) && (
          <div className="flex items-start justify-between border-b border-[#EBF0EE] px-6 py-4.5 bg-[#F8FAF9]">
            <div className="space-y-0.5">
              {title && (
                <h3
                  id="modal-title"
                  className="text-lg font-bold text-[#03312E]"
                >
                  {title}
                </h3>
              )}
              {subtitle && (
                <p className="text-xs text-[#64748B]">{subtitle}</p>
              )}
            </div>

            {showCloseButton && (
              <button
                type="button"
                onClick={onClose}
                aria-label="Close dialog"
                className="rounded-lg p-1.5 text-[#64748B] transition-colors hover:bg-slate-200 hover:text-[#03312E] cursor-pointer"
              >
                <svg
                  className="h-5 w-5"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            )}
          </div>
        )}

        {/* Modal Content Body */}
        <div className="max-h-[calc(85vh-120px)] overflow-y-auto px-6 py-5">
          {children}
        </div>

        {/* Optional Modal Footer */}
        {footer && (
          <div className="flex items-center justify-end gap-3 border-t border-[#EBF0EE] bg-[#F8FAF9] px-6 py-4">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}
