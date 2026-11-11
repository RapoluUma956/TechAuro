import React, { useState, useEffect } from "react";
import { Modal, Button } from "../common";
import { roleService } from "../../services/roleService";
import { UsersIcon, AlertTriangleIcon } from "../common/Icons";

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

  if (!role) return null;

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
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Assigned Employees"
      subtitle={role.description || `Employees configured with the ${role.roleName} role permissions.`}
      size="lg"
      footer={
        <div className="flex items-center justify-end w-full">
          <Button variant="outline" size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      }
    >
      <div className="space-y-4">
        {/* Search Bar & Total Counter */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
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
              className="w-full pl-9 pr-3 py-2 text-sm rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] text-[#03312E] placeholder-[#94A3B8] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 focus:border-[#1976D2] transition-colors"
            />
          </div>
          <div className="text-xs font-medium text-[#64748B] shrink-0">
            Total: <span className="text-[#03312E] font-bold">{filteredEmployees.length}</span> / {employees.length}
          </div>
        </div>

        {/* Content Body */}
        <div>
          {loading ? (
            <div className="flex flex-col items-center justify-center py-12 gap-3">
              <div className="h-8 w-8 animate-spin rounded-full border-3 border-[#1976D2] border-t-transparent" />
              <p className="text-xs text-[#64748B]">Loading assigned employees...</p>
            </div>
          ) : error ? (
            <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-xs text-red-700 flex items-center gap-2">
              <AlertTriangleIcon className="h-4 w-4 shrink-0 text-red-500" />
              <span>{error}</span>
            </div>
          ) : filteredEmployees.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
              <div className="h-12 w-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 mb-2">
                <UsersIcon className="h-6 w-6" />
              </div>
              <p className="text-sm font-bold text-[#03312E]">No employees found</p>
              <p className="text-xs text-[#64748B] mt-0.5">
                {searchQuery
                  ? "No matching employees found for your search query."
                  : "No employees are currently assigned to this role."}
              </p>
            </div>
          ) : (
            <div className="max-h-[50vh] overflow-y-auto divide-y divide-[#DCE5E1] border border-[#DCE5E1] rounded-xl overflow-hidden bg-white">
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
                          <span className="font-bold text-sm text-[#03312E] truncate">
                            {emp.fullName}
                          </span>
                          <span className="font-mono text-xs font-semibold px-2 py-0.5 rounded-md bg-[#F1F5F9] text-[#1E293B]">
                            {emp.employeeCode}
                          </span>
                        </div>
                        <div className="text-xs text-[#64748B] truncate mt-0.5">
                          {emp.email}
                        </div>
                      </div>
                    </div>

                    <div className="text-right shrink-0">
                      <div className="text-xs font-semibold text-[#03312E]">
                        {emp.designation}
                      </div>
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold mt-1 ${
                          emp.status?.toLowerCase() === "active"
                            ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                            : "bg-amber-50 text-amber-700 border border-amber-200"
                        }`}
                      >
                        {emp.status || "Active"}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
}

