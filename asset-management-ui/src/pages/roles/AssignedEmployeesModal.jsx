import React, { useState, useEffect } from "react";
import { roleService } from "../../services/roleService";
import Button from "../../components/common/Button";

function getInitials(name = "") {
  return name
    .split(" ")
    .map((part) => part[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

export default function AssignedEmployeesModal({ isOpen, onClose, role }) {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (!isOpen || !role?.id) {
      setEmployees([]);
      setSearchQuery("");
      setError("");
      return;
    }

    const loadUsers = async () => {
      setLoading(true);
      setError("");
      try {
        const data = await roleService.getAssignedEmployees(role.id);
        setEmployees(data || []);
      } catch (err) {
        setError(err.message || "Failed to load assigned employees");
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, [isOpen, role?.id]);

  if (!isOpen || !role) return null;

  const filteredEmployees = employees.filter((emp) => {
    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    return (
      (emp.fullName && emp.fullName.toLowerCase().includes(q)) ||
      (emp.employeeCode && emp.employeeCode.toLowerCase().includes(q)) ||
      (emp.email && emp.email.toLowerCase().includes(q)) ||
      (emp.designation && emp.designation.toLowerCase().includes(q))
    );
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs transition-opacity"
        onClick={onClose}
      />

      {/* Modal Dialog */}
      <div className="relative w-full max-w-2xl rounded-2xl bg-white shadow-2xl border border-[#DCE5E1] overflow-hidden flex flex-col max-h-[85vh] animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-start justify-between p-6 border-b border-[#DCE5E1] bg-[#F8FAF9]">
          <div className="space-y-1">
            <div className="flex items-center gap-2.5">
              <div className="h-8 w-8 rounded-xl bg-[#EBF5FF] text-[#1976D2] flex items-center justify-center">
                <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                  <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
              </div>
              <h2 className="text-lg font-bold text-[#03312E]">
                Assigned Employees
              </h2>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-[#EBF5FF] text-[#1976D2] border border-[#BFDBFE]">
                {role.roleName}
              </span>
            </div>
            <p className="text-xs text-[#64748B]">
              {role.description || `Employees configured with the ${role.roleName} role permissions.`}
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1 text-[#64748B] hover:bg-white hover:text-[#03312E] transition-colors cursor-pointer"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Search Bar & Count Pill */}
        <div className="p-4 border-b border-[#DCE5E1] bg-white flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div className="relative flex-1">
            <svg
              className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#64748B]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by name, code, email, designation..."
              className="w-full pl-9 pr-3 py-2 text-xs rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] text-[#03312E] placeholder-[#94A3B8] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 focus:border-[#1976D2] transition-all"
            />
          </div>
          <div className="text-xs font-semibold text-[#64748B] shrink-0">
            Total: <span className="text-[#03312E] font-bold">{filteredEmployees.length}</span> / {employees.length}
          </div>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-4">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-12 gap-3">
              <div className="h-7 w-7 animate-spin rounded-full border-3 border-[#1976D2] border-t-transparent" />
              <p className="text-xs text-[#64748B]">Loading assigned employees...</p>
            </div>
          ) : error ? (
            <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-xs text-red-700">
              {error}
            </div>
          ) : filteredEmployees.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
              <div className="h-10 w-10 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 mb-2">
                <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                </svg>
              </div>
              <p className="text-xs font-bold text-[#03312E]">No employees found</p>
              <p className="text-[11px] text-[#64748B] mt-0.5">
                {searchQuery
                  ? "No matching employees found for your search query."
                  : "No employees are currently assigned to this role."}
              </p>
            </div>
          ) : (
            <div className="divide-y divide-[#DCE5E1] border border-[#DCE5E1] rounded-xl overflow-hidden bg-white">
              {filteredEmployees.map((emp) => {
                const initials = getInitials(emp.fullName);
                return (
                  <div
                    key={emp.userId}
                    className="p-3.5 flex items-center justify-between gap-4 hover:bg-[#F8FAF9] transition-colors"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[#E3F2FD] font-bold text-xs text-[#1976D2] shadow-2xs">
                        {initials}
                      </div>
                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-xs text-[#03312E] truncate">
                            {emp.fullName}
                          </span>
                          <span className="font-mono text-[10px] font-semibold px-2 py-0.5 rounded-md bg-[#F1F5F9] text-[#1E293B]">
                            {emp.employeeCode}
                          </span>
                        </div>
                        <div className="text-[11px] text-[#64748B] truncate mt-0.5">
                          {emp.email}
                        </div>
                      </div>
                    </div>

                    <div className="text-right shrink-0">
                      <div className="text-xs font-semibold text-[#03312E]">
                        {emp.designation}
                      </div>
                      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold mt-1 ${
                        emp.status?.toLowerCase() === "active"
                          ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                          : "bg-amber-50 text-amber-700 border border-amber-200"
                      }`}>
                        {emp.status || "ACTIVE"}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-[#DCE5E1] bg-[#F8FAF9] flex justify-end">
          <Button variant="outline" size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </div>
  );
}

