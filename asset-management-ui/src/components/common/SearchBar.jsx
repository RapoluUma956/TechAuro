import React from "react";

export default function SearchBar({
  value = "",
  onChange,
  onClear,
  placeholder = "Search...",
  variant = "responsive", // "responsive" | "long" | "short"
  size = "md", // "sm" | "md" | "lg"
  className = "",
  autoFocus = false,
  disabled = false,
}) {
  const sizeStyles = {
    sm: "py-1.5 pl-8 pr-7 text-xs",
    md: "py-2.5 pl-9.5 pr-8 text-sm",
    lg: "py-3 pl-11 pr-10 text-base",
  };

  const iconSizes = {
    sm: "h-3.5 w-3.5 left-2.5",
    md: "h-4 w-4 left-3",
    lg: "h-5 w-5 left-3.5",
  };

  const clearSizes = {
    sm: "h-3.5 w-3.5 right-2",
    md: "h-4 w-4 right-2.5",
    lg: "h-5 w-5 right-3",
  };

  const variantWidthStyles = {
    long: "w-full",
    short: "w-48 sm:w-60 focus-within:w-72 sm:focus-within:w-80 transition-all duration-200",
    responsive: "w-full md:w-80 lg:w-96",
  };

  const handleClear = () => {
    if (onClear) {
      onClear();
    } else if (onChange) {
      onChange("");
    }
  };

  return (
    <div
      className={`relative inline-flex items-center ${
        variantWidthStyles[variant] || variantWidthStyles.responsive
      } ${className}`}
    >
      {/* Search Magnifying Glass Icon */}
      <div
        className={`pointer-events-none absolute flex items-center text-[#64748B] ${
          iconSizes[size] || iconSizes.md
        }`}
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="h-full w-full"
        >
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
      </div>

      {/* Input */}
      <input
        type="text"
        value={value}
        disabled={disabled}
        autoFocus={autoFocus}
        onChange={(e) => onChange && onChange(e.target.value, e)}
        onKeyDown={(e) => {
          if (e.key === "Escape" && value) {
            handleClear();
          }
        }}
        placeholder={placeholder}
        className={`w-full rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] text-[#03312E] placeholder-[#8C9E9B] shadow-2xs transition-all duration-150 focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 disabled:cursor-not-allowed disabled:bg-slate-100 ${
          sizeStyles[size] || sizeStyles.md
        }`}
      />

      {/* Clear 'X' Button */}
      {value && !disabled && (
        <button
          type="button"
          onClick={handleClear}
          aria-label="Clear search"
          className={`absolute flex items-center justify-center text-[#64748B] hover:text-[#03312E] cursor-pointer transition-colors ${
            clearSizes[size] || clearSizes.md
          }`}
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="h-full w-full"
          >
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      )}
    </div>
  );
}
