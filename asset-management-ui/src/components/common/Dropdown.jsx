import React from "react";

export default function Dropdown({
  label,
  value,
  onChange,
  options = [],
  placeholder = "Select...",
  placeholderValue = "ALL",
  size = "md",
  disabled = false,
  error,
  className = "",
  id,
  name,
  ...props
}) {
  const sizeStyles = {
    sm: "py-1.5 pl-3 pr-8 text-xs rounded-lg",
    md: "py-2.5 pl-3.5 pr-8 text-sm rounded-xl",
    lg: "py-3 pl-4 pr-9 text-base rounded-xl",
  };

  const formattedOptions = options.map((opt) => {
    if (typeof opt === "object" && opt !== null) {
      return {
        value: opt.value ?? opt.id,
        label: opt.label ?? opt.name ?? opt.value,
      };
    }
    return { value: opt, label: String(opt) };
  });

  return (
    <div className={`flex flex-col gap-1.5 ${className}`}>
      {label && (
        <label
          htmlFor={id || name}
          className="text-xs font-semibold text-[#03312E]"
        >
          {label}
        </label>
      )}

      <div className="relative w-full">
        <select
          id={id || name}
          name={name}
          value={value}
          disabled={disabled}
          onChange={(e) => onChange && onChange(e.target.value, e)}
          className={`w-full appearance-none border bg-[#F8FAF9] font-medium text-[#03312E] shadow-2xs transition-colors focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 disabled:cursor-not-allowed disabled:bg-slate-100 cursor-pointer ${
            error
              ? "border-red-500 focus:ring-red-200"
              : "border-[#DCE5E1]"
          } ${sizeStyles[size] || sizeStyles.md}`}
          {...props}
        >
          {placeholder && <option value={placeholderValue}>{placeholder}</option>}
          {formattedOptions.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>

        {/* Custom Chevron Icon */}
        <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3 text-[#64748B]">
          <svg className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
            <path
              fillRule="evenodd"
              d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z"
              clipRule="evenodd"
            />
          </svg>
        </div>
      </div>

      {error && typeof error === "string" && (
        <p className="text-xs text-red-500">{error}</p>
      )}
    </div>
  );
}
