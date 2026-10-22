import React, { useState, useMemo } from "react";
import Pagination from "./Pagination";

/**
 * Common Reusable DataTable Component
 * 
 * Features:
 * - Columns configuration with custom renderers, widths, and alignments
 * - Integrated Pagination with configurable `pageSize` (default: 10)
 * - Automatic client-side pagination or controlled external pagination (for API requests)
 * - Clickable rows with hover styling and optional chevron arrow
 * - Empty state with customizable icon, message, subtitle, and action button
 * - Optional Serial Number (S.No) column
 * - Optional loading state with skeleton rows
 * 
 * Column definition:
 * {
 *   key: 'field_name',
 *   header: 'Column Title',
 *   render: (row, index, serialNo) => <JSX />,
 *   align: 'left' | 'center' | 'right',
 *   width: 'w-24', // or percentage / class
 *   headerClassName: '',
 *   cellClassName: ''
 * }
 */
export default function DataTable({
  columns = [],
  data = [],
  keyField = "id",
  pageSize = 10,
  currentPage: controlledPage,
  totalPages: controlledTotalPages,
  totalItems: controlledTotalItems,
  onPageChange: controlledOnPageChange,
  pagination = true,
  showSerialNo = false,
  serialNoHeader = "S.No",
  onRowClick,
  showRowArrow = false,
  emptyMessage = "No matching records found",
  emptySubtitle = "Try adjusting your search criteria or clearing selected filters.",
  emptyAction,
  loading = false,
  itemLabel = "records",
  className = "",
}) {
  // Internal pagination state for client-side pagination
  const [internalPage, setInternalPage] = useState(1);

  // Determine whether pagination is controlled externally (e.g. server API)
  const isControlled = controlledPage !== undefined && controlledOnPageChange !== undefined;
  const currentPage = isControlled ? controlledPage : internalPage;

  const totalItems = controlledTotalItems !== undefined ? controlledTotalItems : data.length;
  const totalPages =
    controlledTotalPages !== undefined
      ? controlledTotalPages
      : Math.ceil(totalItems / pageSize) || 1;

  const handlePageChange = (newPage) => {
    if (isControlled) {
      controlledOnPageChange(newPage);
    } else {
      setInternalPage(newPage);
    }
  };

  // Slice data if uncontrolled client-side pagination
  const displayData = useMemo(() => {
    if (isControlled) {
      return data;
    }
    const start = (currentPage - 1) * pageSize;
    return data.slice(start, start + pageSize);
  }, [isControlled, data, currentPage, pageSize]);

  const pageOffset = (currentPage - 1) * pageSize;
  const isClickable = Boolean(onRowClick);

  // Alignment classes helper
  const getAlignClass = (align) => {
    if (align === "center") return "text-center";
    if (align === "right") return "text-right";
    return "text-left";
  };

  return (
    <div
      className={`overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white shadow-sm ${className}`}
    >
      <div className="overflow-x-auto">
        <table className="w-full border-collapse text-left">
          {/* Table Header */}
          <thead>
            <tr className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-xs font-bold uppercase tracking-wider text-[#5C7370]">
              {/* Optional S.No column */}
              {showSerialNo && (
                <th
                  scope="col"
                  className="w-14 py-3.5 pl-6 pr-2 text-center text-xs font-bold uppercase tracking-wider"
                >
                  {serialNoHeader}
                </th>
              )}

              {/* Data Columns */}
              {columns.map((col, idx) => (
                <th
                  key={col.key || idx}
                  scope="col"
                  className={`py-3.5 px-4 ${getAlignClass(col.align)} ${
                    col.width || ""
                  } ${col.headerClassName || ""}`}
                >
                  {col.header}
                </th>
              ))}

              {/* Optional Arrow indicator column */}
              {(showRowArrow || isClickable) && (
                <th scope="col" className="w-12 py-3.5 pr-6 pl-2 text-right">
                  <span className="sr-only">Row action</span>
                </th>
              )}
            </tr>
          </thead>

          {/* Table Body */}
          <tbody className="divide-y divide-[#EBF0EE]">
            {loading ? (
              // Loading Skeleton Rows
              Array.from({ length: pageSize }).map((_, rIdx) => (
                <tr key={`skeleton-${rIdx}`} className="animate-pulse">
                  {showSerialNo && (
                    <td className="py-4 pl-6 pr-2 text-center">
                      <div className="mx-auto h-4 w-6 rounded bg-slate-100" />
                    </td>
                  )}
                  {columns.map((col, cIdx) => (
                    <td key={`skeleton-col-${cIdx}`} className="py-4 px-4">
                      <div className="h-4 w-3/4 rounded bg-slate-100" />
                    </td>
                  ))}
                  {(showRowArrow || isClickable) && (
                    <td className="py-4 pr-6 pl-2">
                      <div className="ml-auto h-4 w-4 rounded bg-slate-100" />
                    </td>
                  )}
                </tr>
              ))
            ) : displayData.length === 0 ? (
              // Empty State inside table or full card
              <tr>
                <td
                  colSpan={
                    columns.length +
                    (showSerialNo ? 1 : 0) +
                    (showRowArrow || isClickable ? 1 : 0)
                  }
                  className="py-16 px-4 text-center"
                >
                  <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-[#EBF7F2] text-[#03312E]">
                    <svg
                      className="h-8 w-8"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="1.5"
                    >
                      <circle cx="11" cy="11" r="8" />
                      <line x1="21" y1="21" x2="16.65" y2="16.65" />
                    </svg>
                  </div>
                  <h3 className="mt-4 text-base font-bold text-[#03312E]">
                    {emptyMessage}
                  </h3>
                  {emptySubtitle && (
                    <p className="mt-1 mx-auto max-w-sm text-sm text-[#64748B]">
                      {emptySubtitle}
                    </p>
                  )}
                  {emptyAction && <div className="mt-4">{emptyAction}</div>}
                </td>
              </tr>
            ) : (
              // Real Data Rows
              displayData.map((row, index) => {
                const serialNo = pageOffset + index + 1;
                const rowKey = row[keyField] ?? row.id ?? row._id ?? index;

                return (
                  <tr
                    key={rowKey}
                    onClick={() => isClickable && onRowClick(row, index)}
                    className={`group transition-colors duration-150 ${
                      isClickable
                        ? "cursor-pointer hover:bg-[#EBF7F2]/60"
                        : "hover:bg-slate-50/60"
                    }`}
                  >
                    {/* S.No */}
                    {showSerialNo && (
                      <td className="py-4 pl-6 pr-2 text-center text-xs font-semibold text-[#64748B]">
                        {serialNo}
                      </td>
                    )}

                    {/* Columns */}
                    {columns.map((col, cIdx) => {
                      const cellValue = row[col.key];
                      const renderedContent = col.render
                        ? col.render(row, index, serialNo)
                        : cellValue;

                      return (
                        <td
                          key={col.key || cIdx}
                          className={`py-4 px-4 ${getAlignClass(col.align)} ${
                            col.cellClassName || ""
                          }`}
                        >
                          {renderedContent}
                        </td>
                      );
                    })}

                    {/* Row Arrow */}
                    {(showRowArrow || isClickable) && (
                      <td className="py-4 pr-6 pl-2 text-right">
                        <div className="inline-flex items-center justify-center text-[#94A3B8] transition-transform duration-150 group-hover:translate-x-1 group-hover:text-[#1976D2]">
                          <svg
                            className="h-4 w-4"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </div>
                      </td>
                    )}
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer */}
      {pagination && totalItems > 0 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={totalItems}
          pageSize={pageSize}
          itemLabel={itemLabel}
          onPageChange={handlePageChange}
        />
      )}
    </div>
  );
}
