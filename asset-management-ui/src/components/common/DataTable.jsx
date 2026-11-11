import React, { useState, useMemo } from "react";
import Pagination from "./Pagination";
import { SortIndicator } from "./Icons";

/**
 * Helper to extract sortable value from row.
 * Can be a primitive property, or custom value from row[col.key].
 */
function getSortValue(row, col) {
  if (typeof col.sortValue === "function") {
    return col.sortValue(row);
  }
  const val = row[col.key];
  if (val === null || val === undefined) return "";
  if (typeof val === "string") return val.toLowerCase();
  return val;
}

/**
 * Common Reusable DataTable Component
 * 
 * Features:
 * - Columns configuration with custom renderers, widths, alignments, and sorting
 * - Integrated client-side multi-column sorting (ascending, descending, neutral)
 * - Controlled external sorting via `sortBy`, `sortDir`, `onSortChange`
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
 *   width: 'w-24',
 *   sortable: true | false (default: true if key exists and not 'actions'),
 *   sortValue: (row) => comparableValue,
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
  // Sorting props
  sortBy: controlledSortBy,
  sortDir: controlledSortDir,
  onSortChange: controlledOnSortChange,
  defaultSortBy = null,
  defaultSortDir = "asc",
  // Other props
  pagination = true,
  showSerialNo = false,
  serialNoHeader = "#",
  onRowClick,
  showRowArrow = false,
  emptyMessage = "No matching records found",
  emptySubtitle = "Try adjusting your search criteria.",
  emptyAction,
  loading = false,
  itemLabel = "records",
  className = "",
  renderExpandedRow,
  expandedRowKeys = {},
}) {
  // Internal pagination state for client-side pagination
  const [internalPage, setInternalPage] = useState(1);

  // Internal sort state for client-side sorting
  const [internalSortBy, setInternalSortBy] = useState(defaultSortBy);
  const [internalSortDir, setInternalSortDir] = useState(defaultSortDir);

  const isControlledSort = controlledSortBy !== undefined && controlledOnSortChange !== undefined;
  const currentSortBy = isControlledSort ? controlledSortBy : internalSortBy;
  const currentSortDir = isControlledSort ? controlledSortDir : internalSortDir;

  const handleHeaderClick = (col) => {
    // Check if column is sortable: default true unless col.sortable === false or key is 'actions'
    const isSortable = col.sortable !== false && col.key !== "actions";
    if (!isSortable) return;

    let nextDir = "asc";
    if (currentSortBy === col.key) {
      if (currentSortDir === "asc") {
        nextDir = "desc";
      } else if (currentSortDir === "desc") {
        nextDir = "asc";
      }
    }

    if (isControlledSort) {
      controlledOnSortChange(col.key, nextDir);
    } else {
      setInternalSortBy(col.key);
      setInternalSortDir(nextDir);
      // Reset to page 1 on client sort
      setInternalPage(1);
    }
  };

  // Determine whether pagination is controlled externally (e.g. server API)
  const isControlledPage = controlledPage !== undefined && controlledOnPageChange !== undefined;
  const currentPage = isControlledPage ? controlledPage : internalPage;

  // Client-side sort data if sorting internally
  const sortedData = useMemo(() => {
    if (isControlledSort || !currentSortBy) {
      return data;
    }
    const col = columns.find((c) => c.key === currentSortBy);
    if (!col) return data;

    const copy = [...data];
    copy.sort((a, b) => {
      const valA = getSortValue(a, col);
      const valB = getSortValue(b, col);

      if (valA === valB) return 0;
      if (valA === "" || valA === null || valA === undefined) return 1;
      if (valB === "" || valB === null || valB === undefined) return -1;

      if (typeof valA === "number" && typeof valB === "number") {
        return currentSortDir === "asc" ? valA - valB : valB - valA;
      }
      return currentSortDir === "asc"
        ? String(valA).localeCompare(String(valB), undefined, { numeric: true, sensitivity: "base" })
        : String(valB).localeCompare(String(valA), undefined, { numeric: true, sensitivity: "base" });
    });
    return copy;
  }, [data, currentSortBy, currentSortDir, columns, isControlledSort]);

  const totalItems = controlledTotalItems !== undefined ? controlledTotalItems : sortedData.length;
  const totalPages =
    controlledTotalPages !== undefined
      ? controlledTotalPages
      : Math.ceil(totalItems / pageSize) || 1;

  const handlePageChange = (newPage) => {
    if (isControlledPage) {
      controlledOnPageChange(newPage);
    } else {
      setInternalPage(newPage);
    }
  };

  // Slice data if uncontrolled client-side pagination
  const displayData = useMemo(() => {
    if (isControlledPage) {
      return sortedData;
    }
    const start = (currentPage - 1) * pageSize;
    return sortedData.slice(start, start + pageSize);
  }, [isControlledPage, sortedData, currentPage, pageSize]);

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
            <tr className="border-b border-[#DCE5E1] bg-[#F8FAF9] text-xs font-bold uppercase tracking-wider text-[#64748B]">
              {/* Optional S.No column */}
              {showSerialNo && (
                <th
                  scope="col"
                  className="w-14 py-3.5 pl-6 pr-2 text-center text-xs font-bold uppercase tracking-wider text-[#64748B]"
                >
                  {serialNoHeader}
                </th>
              )}

              {/* Data Columns */}
              {columns.map((col, idx) => {
                const isSortable = col.sortable !== false && col.key !== "actions";
                const isCurrentSort = currentSortBy === col.key;

                return (
                  <th
                    key={col.key || idx}
                    scope="col"
                    onClick={() => isSortable && handleHeaderClick(col)}
                    className={`py-3.5 px-4 ${getAlignClass(col.align)} ${
                      col.width || ""
                    } ${col.headerClassName || ""} ${
                      isSortable ? "cursor-pointer select-none hover:text-[#03312E] transition-colors" : ""
                    }`}
                  >
                    <span className={`inline-flex items-center gap-1.5 ${
                      col.align === "center" ? "justify-center" : col.align === "right" ? "justify-end" : "justify-start"
                    }`}>
                      <span>{col.header}</span>
                      {isSortable && (
                        <SortIndicator
                          active={isCurrentSort}
                          direction={isCurrentSort ? currentSortDir : "asc"}
                        />
                      )}
                    </span>
                  </th>
                );
              })}

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
                const isRowExpanded = Boolean(expandedRowKeys[rowKey]);

                return (
                  <React.Fragment key={rowKey}>
                    <tr
                      onClick={() => isClickable && onRowClick(row, index)}
                      className={`group transition-colors duration-150 ${
                        isClickable
                          ? "cursor-pointer hover:bg-[#F8FAF9]"
                          : "hover:bg-[#F8FAF9]"
                      }`}
                    >
                      {/* S.No */}
                      {showSerialNo && (
                        <td className="py-4 pl-6 pr-2 text-center text-xs font-semibold text-[#64748B]">
                          <span className="inline-block rounded-md bg-[#F1F5F9] px-2.5 py-1 font-mono text-xs font-bold text-[#1E293B]">
                            {serialNo}
                          </span>
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

                    {/* Expandable Row Content */}
                    {renderExpandedRow && isRowExpanded && (
                      <tr key={`${rowKey}-expanded`} className="bg-[#F8FAF9]">
                        <td
                          colSpan={
                            columns.length +
                            (showSerialNo ? 1 : 0) +
                            (showRowArrow || isClickable ? 1 : 0)
                          }
                          className="p-4 pl-10 pr-6"
                        >
                          {renderExpandedRow(row, index)}
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
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
