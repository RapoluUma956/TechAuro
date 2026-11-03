import React from "react";

export default function Pagination({
  currentPage = 1,
  totalPages = 1,
  totalItems = 0,
  pageSize = 10,
  itemLabel = "records",
  onPageChange,
  className = "",
}) {
  if (totalPages <= 1 && totalItems <= pageSize) {
    return (
      <div
        className={`flex flex-col items-center justify-between gap-3 border-t border-[#EBF0EE] bg-[#F8FAF9] px-6 py-3.5 sm:flex-row ${className}`}
      >
        <p className="text-xs text-[#64748B]">
          Showing <span className="font-bold text-[#03312E]">{totalItems > 0 ? 1 : 0}</span> to{" "}
          <span className="font-bold text-[#03312E]">{totalItems}</span> of{" "}
          <span className="font-bold text-[#03312E]">{totalItems}</span> {itemLabel}
        </p>
        <span className="text-xs font-semibold text-[#8C9E9B]">Page 1 of 1</span>
      </div>
    );
  }

  const startIndex = (currentPage - 1) * pageSize + 1;
  const endIndex = Math.min(currentPage * pageSize, totalItems);

  // Generate pagination items with intelligent ellipsis
  const getPageNumbers = () => {
    const pages = [];

    if (totalPages <= 7) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= 4) {
        // Near the beginning: 1, 2, 3, 4, 5, "...", totalPages
        for (let i = 1; i <= 5; i++) {
          pages.push(i);
        }
        pages.push("...");
        pages.push(totalPages);
      } else if (currentPage >= totalPages - 3) {
        // Near the end: 1, "...", totalPages - 4, ..., totalPages
        pages.push(1);
        pages.push("...");
        for (let i = totalPages - 4; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        // In the middle: 1, "...", current - 1, current, current + 1, "...", totalPages
        pages.push(1);
        pages.push("...");
        pages.push(currentPage - 1);
        pages.push(currentPage);
        pages.push(currentPage + 1);
        pages.push("...");
        pages.push(totalPages);
      }
    }
    return pages;
  };

  const pages = getPageNumbers();

  return (
    <div
      className={`flex flex-col items-center justify-between gap-3 border-t border-[#EBF0EE] bg-[#F8FAF9] px-6 py-3.5 sm:flex-row ${className}`}
    >
      {/* Information summary text */}
      <p className="text-xs text-[#64748B]">
        Showing <span className="font-bold text-[#03312E]">{startIndex}</span> to{" "}
        <span className="font-bold text-[#03312E]">{endIndex}</span> of{" "}
        <span className="font-bold text-[#03312E]">{totalItems}</span> {itemLabel}
      </p>

      {/* Pagination Controls */}
      <nav aria-label="Pagination Navigation" className="flex items-center gap-1.5">
        {/* Previous Button */}
        <button
          type="button"
          onClick={() => onPageChange && onPageChange(currentPage - 1)}
          disabled={currentPage === 1}
          aria-label="Previous Page"
          className="inline-flex h-8.5 items-center justify-center gap-1 rounded-lg border border-[#DCE5E1] bg-white px-2.5 text-xs font-semibold text-[#03312E] shadow-2xs transition-all hover:bg-[#F1F5F9] disabled:cursor-not-allowed disabled:opacity-40 cursor-pointer select-none"
        >
          <svg className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
            <path
              fillRule="evenodd"
              d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z"
              clipRule="evenodd"
            />
          </svg>
          <span className="hidden sm:inline">Prev</span>
        </button>

        {/* Page Number Buttons */}
        {pages.map((p, idx) => {
          if (p === "...") {
            return (
              <span
                key={`ellipsis-${idx}`}
                className="flex h-8.5 w-8.5 items-center justify-center text-xs font-bold text-[#8C9E9B]"
              >
                ...
              </span>
            );
          }

          const isActive = p === currentPage;
          return (
            <button
              key={p}
              type="button"
              onClick={() => onPageChange && onPageChange(p)}
              aria-current={isActive ? "page" : undefined}
              className={`flex h-8.5 min-w-[34px] items-center justify-center rounded-lg px-2 text-xs font-bold transition-all cursor-pointer select-none ${
                isActive
                  ? "bg-[#1976D2] text-white shadow-xs"
                  : "border border-[#DCE5E1] bg-white text-[#03312E] hover:bg-[#F1F5F9]"
              }`}
            >
              {p}
            </button>
          );
        })}

        {/* Next Button */}
        <button
          type="button"
          onClick={() => onPageChange && onPageChange(currentPage + 1)}
          disabled={currentPage === totalPages}
          aria-label="Next Page"
          className="inline-flex h-8.5 items-center justify-center gap-1 rounded-lg border border-[#DCE5E1] bg-white px-2.5 text-xs font-semibold text-[#03312E] shadow-2xs transition-all hover:bg-[#F1F5F9] disabled:cursor-not-allowed disabled:opacity-40 cursor-pointer select-none"
        >
          <span className="hidden sm:inline">Next</span>
          <svg className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
            <path
              fillRule="evenodd"
              d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z"
              clipRule="evenodd"
            />
          </svg>
        </button>
      </nav>
    </div>
  );
}
