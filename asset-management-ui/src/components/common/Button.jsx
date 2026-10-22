import React from "react";

export default function Button({
  children,
  variant = "primary",
  size = "md",
  icon = null,
  iconPosition = "left",
  isLoading = false,
  disabled = false,
  className = "",
  type = "button",
  onClick,
  ...props
}) {
  const baseStyles =
    "inline-flex items-center justify-center font-semibold transition-all duration-150 focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer active:scale-95 select-none";

  const sizeStyles = {
    sm: "px-3 py-1.5 text-xs rounded-lg gap-1.5",
    md: "px-4 py-2.5 text-sm rounded-xl gap-2",
    lg: "px-5 py-3 text-base rounded-xl gap-2.5",
  };

  const variantStyles = {
    primary:
      "bg-[#1976D2] hover:bg-[#1565C0] text-white shadow-xs hover:shadow-md focus:ring-[#1976D2]/40",
    secondary:
      "bg-[#03312E] hover:bg-[#02211F] text-white shadow-xs hover:shadow-md focus:ring-[#03312E]/40",
    outline:
      "border border-[#DCE5E1] bg-white hover:bg-[#F8FAF9] text-[#03312E] shadow-2xs hover:border-[#1976D2] focus:ring-slate-200",
    ghost:
      "bg-transparent hover:bg-[#F1F5F9] text-[#5C7370] hover:text-[#03312E] focus:ring-slate-200",
    danger:
      "bg-[#EF4444] hover:bg-[#DC2626] text-white shadow-xs hover:shadow-md focus:ring-red-300",
    success:
      "bg-[#00C896] hover:bg-[#00B084] text-white shadow-xs hover:shadow-md focus:ring-emerald-300",
  };

  const computedClass = `${baseStyles} ${sizeStyles[size] || sizeStyles.md} ${
    variantStyles[variant] || variantStyles.primary
  } ${className}`;

  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      onClick={onClick}
      className={computedClass}
      {...props}
    >
      {isLoading ? (
        <svg
          className="h-4 w-4 animate-spin text-current"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8v8H4z"
          />
        </svg>
      ) : (
        icon && iconPosition === "left" && <span className="shrink-0">{icon}</span>
      )}

      <span>{children}</span>

      {!isLoading && icon && iconPosition === "right" && (
        <span className="shrink-0">{icon}</span>
      )}
    </button>
  );
}
