import { useState, useMemo, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { userService } from "../../services/userService";
import {
  EmployeeHeader,
  EmployeeMetricCards,
  EmployeeFilters,
  EmployeeTable,
  AddEmployeeModal,
} from "../../components/employees";
import { initialEmployeesData } from "../../data/employeesData";

export default function Employees() {
  const navigate = useNavigate();

  // Primary data state initialized with raw static data (ready for API integration)
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Search & Filter states
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [roleFilter, setRoleFilter] = useState("ALL");

  // Pagination state (10 rows per page)
  const PAGE_SIZE = 10;
  const [currentPage, setCurrentPage] = useState(1);

  // Modal & Notification state
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [successToast, setSuccessToast] = useState("");

  useEffect(() => {
    let isMounted = true;
    const fetchEmployees = async () => {
      try {
        setLoading(true);
        setError("");
        const response = await userService.getAllUsers();
        if (!Array.isArray(response)) {
          throw new Error("Unexpected response from server");
        }
        const mappedEmployees = response.map((user) => ({
          id: user.userId,
          employeeId: user.employeeCode,
          employeeName: [user.firstName, user.lastName]
            .filter(Boolean)
            .join(" "),
          email: user.email,
          phone: user.phone,
          role: user.designation || "Not specified",
          accountStatus: user.accountStatus || "Unknown",
          deviceStatus: user.accountStatus || "Unknown",
          deviceName: "",
          deviceId: "",
        }));
        if (isMounted) {
          setEmployees(mappedEmployees);
          setCurrentPage(1);
        }
      } catch (err) {
        if (isMounted) {
          // Graceful fallback to initial data so UI functions smoothly even when backend is offline
          setEmployees(initialEmployeesData);
          setCurrentPage(1);
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };
    fetchEmployees();
    return () => {
      isMounted = false;
    };
  }, []);

  // Distinct roles for filter dropdown
  const roles = useMemo(() => {
    return Array.from(
      new Set(employees.map((e) => e.role).filter(Boolean)),
    ).sort();
  }, [employees]);

  // Dynamic metric calculations based on total data
  const metrics = useMemo(() => {
    return {
      totalEmployees: employees.length,
      activeCount: employees.filter(
        (employee) => (employee.accountStatus || "").toLowerCase() === "active",
      ).length,
      repairCount: 0,
      inactiveCount: employees.filter(
        (employee) =>
          (employee.accountStatus || "").toLowerCase() === "inactive",
      ).length,
    };
  }, [employees]);

  // Filtered dataset matching search and dropdown filters
  const filteredEmployees = useMemo(() => {
return employees.filter((employee) => {
if (
statusFilter !== "ALL" &&
(employee.accountStatus || "").toLowerCase() !==
statusFilter.toLowerCase()
) {
return false;
}

  if (
    roleFilter !== "ALL" &&
    employee.role !== roleFilter
  ) {
    return false;
  }

  if (searchQuery.trim()) {
    const query = searchQuery.toLowerCase().trim();

    const searchableFields = [
      employee.employeeName,
      employee.employeeId,
      employee.role,
      employee.email,
      employee.deviceName,
      employee.deviceId,
    ];

    const matchesSearch = searchableFields.some((field) =>
      (field || "").toLowerCase().includes(query)
    );

    if (!matchesSearch) {
      return false;
    }
  }

  return true;
});

}, [employees, searchQuery, statusFilter, roleFilter]);

// Pagination calculations
const totalPages =
Math.ceil(filteredEmployees.length / PAGE_SIZE) || 1;

const validCurrentPage = Math.min(currentPage, totalPages);

const paginatedEmployees = useMemo(() => {
const startIndex = (validCurrentPage - 1) * PAGE_SIZE;

return filteredEmployees.slice(
  startIndex,
  startIndex + PAGE_SIZE
);

}, [filteredEmployees, validCurrentPage]);

// Reset pagination when filters change
const handleSearchChange = (value) => {
setSearchQuery(value);
setCurrentPage(1);
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
setStatusFilter("ALL");
setRoleFilter("ALL");
setCurrentPage(1);
};

  const handleRowClick = (employee) => {
    const targetId = employee.employeeId || employee.id;
    navigate(`/employees/${targetId}`);
  };

// Local-only for now; backend creation will be connected next
const handleAddEmployee = (newEmployee) => {
setEmployees((previous) => [newEmployee, ...previous]);
setCurrentPage(1);

setSuccessToast(
  `Employee ${newEmployee.employeeName} added successfully!`
);

setTimeout(() => {
  setSuccessToast("");
}, 4000);

};

return (
<div className="space-y-6 text-[#24211D]">
{/* Success notification */}
{successToast && (
<div className="flex items-center justify-between rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-800 shadow-sm">
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

  <EmployeeHeader
    onAddEmployee={() => setIsAddModalOpen(true)}
  />

  {/* Loading state */}
  {loading && (
    <div className="rounded-xl border border-gray-200 bg-white p-6 text-center text-sm text-gray-600">
      Loading employees from the server...
    </div>
  )}

  {/* Error state */}
  {!loading && error && (
    <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
      <p>{error}</p>

      <button
        type="button"
        onClick={() => window.location.reload()}
        className="mt-2 cursor-pointer font-semibold underline"
      >
        Retry
      </button>
    </div>
  )}

  {/* Show employee data only after successful loading */}
  {!loading && !error && (
    <>
      <EmployeeMetricCards
        totalEmployees={metrics.totalEmployees}
        activeCount={metrics.activeCount}
        repairCount={metrics.repairCount}
        inactiveCount={metrics.inactiveCount}
      />

      <EmployeeFilters
        searchQuery={searchQuery}
        onSearchChange={handleSearchChange}
        statusFilter={statusFilter}
        onStatusChange={handleStatusChange}
        roleFilter={roleFilter}
        onRoleChange={handleRoleChange}
        roles={roles}
        onClearFilters={handleClearFilters}
        totalCount={employees.length}
        filteredCount={filteredEmployees.length}
      />

      <EmployeeTable
        employees={paginatedEmployees}
        totalEmployeesCount={filteredEmployees.length}
        currentPage={validCurrentPage}
        pageSize={PAGE_SIZE}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
        onRowClick={handleRowClick}
        onClearFilters={handleClearFilters}
      />
    </>
  )}

  {/* Add Employee Modal */}
  <AddEmployeeModal
    isOpen={isAddModalOpen}
    onClose={() => setIsAddModalOpen(false)}
    onSave={handleAddEmployee}
    roles={roles}
    nextId={employees.length + 1}
  />
</div>

);
}