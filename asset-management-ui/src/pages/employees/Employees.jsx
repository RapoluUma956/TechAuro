import { useState, useMemo, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { employeeService } from "../../services/employeeService";
import useAutoRefresh from "../../hooks/useAutoRefresh";
import {
  EmployeeHeader,
  EmployeeMetricCards,
  EmployeeFilters,
  EmployeeTable,
  AddEmployeeModal,
} from "../../components/employees";

const DEFAULT_ROLES = [
  "Software Engineer",
  "Senior Software Engineer",
  "Lead Architect",
  "Product Designer",
  "Product Manager",
  "DevOps & Infrastructure",
  "QA Engineer",
  "Data Engineer",
  "Security Specialist",
];

export default function Employees() {
  const navigate = useNavigate();

  // Metrics state
  const [metrics, setMetrics] = useState({
    totalEmployees: 0,
    activeDevices: 0,
    inRepair: 0,
    inactiveDevices: 0,
  });

  // Primary data state
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Search & Filter states
  const [searchQuery, setSearchQuery] = useState("");
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [roleFilter, setRoleFilter] = useState("ALL");

  // Pagination state (10 rows per page)
  const PAGE_SIZE = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalCount, setTotalCount] = useState(0);

  // Sorting
  const [sortBy] = useState("createdAt");
  const [sortDir] = useState("desc");

  // Modal & Notification state
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [successToast, setSuccessToast] = useState("");

  // Debounce search query changes by 300ms
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
      setCurrentPage(1);
    }, 300);
    return () => clearTimeout(handler);
  }, [searchQuery]);

  // Fetch metrics from backend
  const fetchMetrics = useCallback(async () => {
    try {
      const data = await employeeService.getEmployeeMetrics();
      if (data) {
        setMetrics({
          totalEmployees: data.totalEmployees ?? 0,
          activeDevices: data.activeDevices ?? data.activeEmployees ?? 0,
          inRepair: data.inRepair ?? data.inRepairDevices ?? 0,
          inactiveDevices: data.inactiveDevices ?? data.inactiveEmployees ?? 0,
        });
      }
    } catch (err) {
      console.error("Failed to load employee metrics:", err);
    }
  }, []);

  // Fetch paginated employee list from backend
  const fetchEmployees = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const params = {
        page: currentPage - 1,
        size: PAGE_SIZE,
        search: debouncedSearchQuery.trim(),
        status: statusFilter,
        role: roleFilter,
        sortBy,
        sortDir,
      };

      const response = await employeeService.getEmployees(params);
      if (response && Array.isArray(response.content)) {
        const mappedEmployees = response.content.map((emp) => ({
          id: emp.employeeId,
          employeeId: emp.employeeCode || `EMP-${emp.employeeId}`,
          employeeName:
            emp.fullName ||
            [emp.firstName, emp.lastName].filter(Boolean).join(" "),
          email: emp.email,
          phone: emp.phone,
          role: emp.designation || emp.role || "—",
          accountStatus: emp.status || "ACTIVE",
          deviceStatus:
            emp.device?.connectionStatus ||
            (emp.device ? emp.device.deviceStatus || "Assigned" : "Unassigned"),
          deviceName: emp.device?.deviceName || "—",
          serialNumber: emp.device?.serialNumber || "Unassigned",
          deviceId: emp.device?.serialNumber || "",
          warrantyDate: emp.device?.warrantyExpiryDate || null,
        }));
        setEmployees(mappedEmployees);
        setTotalCount(response.totalElements ?? mappedEmployees.length);
        setTotalPages(response.totalPages ?? 1);
      } else {
        setEmployees([]);
        setTotalCount(0);
        setTotalPages(1);
      }
    } catch (err) {
      console.error("Failed to fetch employees:", err);
      setError(
        err.message || "Failed to load employees from the backend server."
      );
      setEmployees([]);
    } finally {
      setLoading(false);
    }
  }, [
    currentPage,
    PAGE_SIZE,
    debouncedSearchQuery,
    statusFilter,
    roleFilter,
    sortBy,
    sortDir,
  ]);

  // Initial load
  useEffect(() => {
    fetchMetrics();
  }, [fetchMetrics]);

  // 30-Second Active-Tab Auto-Refresh
  useAutoRefresh(
    useCallback(() => {
      fetchMetrics();
      fetchEmployees();
    }, [fetchMetrics, fetchEmployees]),
    30000
  );

  useEffect(() => {
    fetchEmployees();
  }, [fetchEmployees]);

  // Distinct roles for filter dropdown
  const roles = useMemo(() => {
    const fromEmployees = employees
      .map((e) => e.role)
      .filter((r) => r && r !== "—");
    return Array.from(new Set([...DEFAULT_ROLES, ...fromEmployees])).sort();
  }, [employees]);

  // Reset pagination when filters change
  const handleSearchChange = (value) => {
    setSearchQuery(value);
  };

  const handleStatusChange = (value) => {
    setStatusFilter(value);
    setCurrentPage(1);
  };

  const handleRoleChange = (value) => {
    setRoleFilter(value);
    setCurrentPage(1);
  };

  const handleClearFilters = () => {
    setSearchQuery("");
    setDebouncedSearchQuery("");
    setStatusFilter("ALL");
    setRoleFilter("ALL");
    setCurrentPage(1);
  };

  const handleRowClick = (employee) => {
    const targetId = employee.id || employee.employeeId;
    navigate(`/employees/${targetId}`);
  };

  const handleAddEmployee = async (payload) => {
    await employeeService.createEmployee(payload);
    setIsAddModalOpen(false);
    setSuccessToast(
      `Employee ${payload.firstName} ${payload.lastName || ""} added successfully!`
    );
    // Refresh both metrics and current listing
    await fetchMetrics();
    setCurrentPage(1);
    await fetchEmployees();

    setTimeout(() => {
      setSuccessToast("");
    }, 5000);
  };

  return (
    <div className="space-y-6 text-[#24211D]">
      {/* Success notification */}
      {successToast && (
        <div className="flex items-center justify-between rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-800 shadow-sm animate-fadeIn">
          <div className="flex items-center gap-2">
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-200 text-emerald-800">
              ✓
            </span>
            <span>{successToast}</span>
          </div>

          <button
            type="button"
            onClick={() => setSuccessToast("")}
            className="cursor-pointer text-emerald-700 hover:text-emerald-900"
          >
            ✕
          </button>
        </div>
      )}

      <EmployeeHeader onAddEmployee={() => setIsAddModalOpen(true)} />

      {/* Metric Cards connected to GET /api/v1/employees/metrics */}
      <EmployeeMetricCards
        totalEmployees={metrics.totalEmployees}
        activeCount={metrics.activeDevices}
        repairCount={metrics.inRepair}
        inactiveCount={metrics.inactiveDevices}
      />

      {/* Filter and Search Bar */}
      <EmployeeFilters
        searchQuery={searchQuery}
        onSearchChange={handleSearchChange}
        statusFilter={statusFilter}
        onStatusChange={handleStatusChange}
        roleFilter={roleFilter}
        onRoleChange={handleRoleChange}
        roles={roles}
        onClearFilters={handleClearFilters}
        totalCount={totalCount}
        filteredCount={employees.length}
      />

      {/* Loading state */}
      {loading && (
        <div className="rounded-xl border border-gray-200 bg-white p-12 text-center text-sm text-gray-600 space-y-3">
          <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-emerald-600 border-t-transparent" />
          <p>Loading employees from the server...</p>
        </div>
      )}

      {/* Error state */}
      {!loading && error && (
        <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center text-sm text-red-700 space-y-2">
          <p className="font-semibold">{error}</p>
          <button
            type="button"
            onClick={() => {
              fetchMetrics();
              fetchEmployees();
            }}
            className="mt-2 inline-flex items-center gap-1.5 rounded-lg bg-red-600 px-3.5 py-1.5 text-xs font-semibold text-white hover:bg-red-700 cursor-pointer shadow-xs"
          >
            Retry Request
          </button>
        </div>
      )}

      {/* Employee Table with server-side pagination */}
      {!loading && !error && (
        <EmployeeTable
          employees={employees}
          totalEmployeesCount={totalCount}
          currentPage={currentPage}
          pageSize={PAGE_SIZE}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
          onRowClick={handleRowClick}
          onClearFilters={handleClearFilters}
        />
      )}

      {/* Add Employee Modal */}
      <AddEmployeeModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSave={handleAddEmployee}
        roles={roles}
        nextId={totalCount + 1}
      />
    </div>
  );
}
