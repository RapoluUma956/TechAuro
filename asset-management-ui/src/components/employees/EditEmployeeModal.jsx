import { useState, useEffect, useRef } from "react";
import { Modal, Button } from "../common";
import { employeeService } from "../../services/employeeService";
import { roleService } from "../../services/roleService";

export default function EditEmployeeModal({
  isOpen,
  onClose,
  employee,
  onSave,
  roles = [],
}) {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    employeeCode: "",
    email: "",
    phone: "",
    designation: "",
    roleId: "",
    dateOfJoining: "",
    accountStatus: "ACTIVE",
    deviceMode: "keep", // "keep" | "select" | "manual" | "none"
    selectedSerialNumber: "",
    manualSerialNumber: "",
  });

  const [availableRoles, setAvailableRoles] = useState([]);
  const [availableDevices, setAvailableDevices] = useState([]);
  const [loadingDevices, setLoadingDevices] = useState(false);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState("");
  const [errors, setErrors] = useState({});

  // Manual serial lookup state
  const [lookupState, setLookupState] = useState({
    loading: false,
    checked: false,
    assignable: false,
    device: null,
    message: "",
  });

  const prevIsOpenRef = useRef(false);

  useEffect(() => {
    // Only populate on open transition
    if (isOpen && !prevIsOpenRef.current && employee) {
      const empId = employee.id || employee.employeeId;

      // Initial populate from row data
      const parts = (employee.employeeName || "").trim().split(" ");
      const fName = parts[0] || "";
      const lName = parts.slice(1).join(" ") || "";

      const currentHasDevice =
        employee.serialNumber &&
        employee.serialNumber !== "Unassigned" &&
        employee.serialNumber !== "—";

      setFormData({
        firstName: employee.firstName || fName,
        lastName: employee.lastName || lName,
        employeeCode: employee.employeeId || employee.employeeCode || "",
        email: employee.email || "",
        phone: employee.phone || "",
        designation: employee.role || employee.designation || "Software Engineer",
        roleId: "",
        dateOfJoining: employee.dateOfJoining || new Date().toISOString().split("T")[0],
        accountStatus: employee.accountStatus || "ACTIVE",
        deviceMode: currentHasDevice ? "keep" : "none",
        selectedSerialNumber: "",
        manualSerialNumber: "",
      });

      setErrors({});
      setServerError("");
      setLookupState({
        loading: false,
        checked: false,
        assignable: false,
        device: null,
        message: "",
      });

      // Load full employee profile from backend to get full details (roleId, dateOfJoining, etc.)
      if (empId) {
        setLoadingDetails(true);
        employeeService
          .getEmployeeById(empId)
          .then((profile) => {
            if (profile) {
              setFormData((prev) => ({
                ...prev,
                firstName: profile.firstName || prev.firstName,
                lastName: profile.lastName || prev.lastName,
                employeeCode: profile.employeeCode || prev.employeeCode,
                email: profile.email || prev.email,
                phone: profile.phone || prev.phone,
                designation: profile.designation || profile.role || prev.designation,
                dateOfJoining: profile.dateOfJoining || prev.dateOfJoining,
                accountStatus: profile.accountStatus || profile.status || prev.accountStatus,
              }));
            }
          })
          .catch((err) => {
            console.warn("Could not fetch detailed profile for edit:", err);
          })
          .finally(() => {
            setLoadingDetails(false);
          });
      }

      // Fetch dynamic roles from backend
      roleService
        .getRoles()
        .then((rolesList) => {
          const validRoles = Array.isArray(rolesList) ? rolesList : [];
          setAvailableRoles(validRoles);
          const currentRoleName = (employee.role || employee.designation || "").toUpperCase();
          const matchedRole = validRoles.find(
            (r) => (r.roleName || "").toUpperCase() === currentRoleName
          );
          if (matchedRole) {
            setFormData((prev) => ({
              ...prev,
              roleId: String(matchedRole.id),
            }));
          } else if (validRoles.length > 0) {
            setFormData((prev) => ({
              ...prev,
              roleId: prev.roleId || String(validRoles[0].id),
            }));
          }
        })
        .catch(() => {
          setAvailableRoles([]);
        });

      // Fetch available devices from backend
      setLoadingDevices(true);
      employeeService
        .getAvailableDevices()
        .then((res) => {
          const list = res?.devices || (Array.isArray(res) ? res : []);
          setAvailableDevices(list);
        })
        .catch(() => {
          setAvailableDevices([]);
        })
        .finally(() => {
          setLoadingDevices(false);
        });
    }

    prevIsOpenRef.current = isOpen;
  }, [isOpen, employee]);

  // Debounced lookup for manual serial number
  useEffect(() => {
    const sn = formData.manualSerialNumber.trim();
    if (formData.deviceMode !== "manual" || !sn) {
      setLookupState({
        loading: false,
        checked: false,
        assignable: false,
        device: null,
        message: "",
      });
      return;
    }

    const timer = setTimeout(async () => {
      setLookupState((p) => ({ ...p, loading: true }));
      try {
        const resp = await employeeService.getDeviceBySerial(sn);
        if (resp && resp.serialNumber) {
          setLookupState({
            loading: false,
            checked: true,
            assignable: resp.assignable ?? true,
            device: resp,
            message:
              resp.message ||
              (resp.assignable
                ? "Device is available for assignment."
                : "Device is already assigned."),
          });
        } else {
          setLookupState({
            loading: false,
            checked: true,
            assignable: true,
            device: null,
            message:
              "Serial number not yet detected by agent. Telemetry will sync automatically once device reports.",
          });
        }
      } catch (err) {
        setLookupState({
          loading: false,
          checked: true,
          assignable: true,
          device: null,
          message:
            "Device not found in registry. Telemetry will link when the agent starts.",
        });
      }
    }, 400);

    return () => clearTimeout(timer);
  }, [formData.manualSerialNumber, formData.deviceMode]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
    if (serverError) {
      setServerError("");
    }
  };

  const selectedDeviceObj = availableDevices.find(
    (d) => d.serialNumber === formData.selectedSerialNumber
  );

  const currentDeviceSerial = employee?.serialNumber;
  const hasCurrentDevice =
    currentDeviceSerial &&
    currentDeviceSerial !== "Unassigned" &&
    currentDeviceSerial !== "—";

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    const newErrors = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = "First name is required";
    }
    if (!formData.employeeCode.trim()) {
      newErrors.employeeCode = "Employee ID / Code is required";
    }
    if (!formData.email.trim()) {
      newErrors.email = "Email address is required";
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = "Please enter a valid email address";
    }

    if (
      formData.deviceMode === "manual" &&
      !formData.manualSerialNumber.trim()
    ) {
      newErrors.manualSerialNumber = "Please enter the hardware serial number";
    }

    if (
      formData.deviceMode === "select" &&
      !formData.selectedSerialNumber
    ) {
      newErrors.selectedSerialNumber = "Please select a device from the list";
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    let deviceAssignment = { mode: "KEEP" };
    if (formData.deviceMode === "none") {
      deviceAssignment = { mode: "NONE" };
    } else if (formData.deviceMode === "select" && formData.selectedSerialNumber) {
      deviceAssignment = {
        mode: "DEVICE_ID",
        deviceId: selectedDeviceObj?.deviceId || null,
        serialNumber: selectedDeviceObj?.serialNumber || formData.selectedSerialNumber,
      };
    } else if (
      formData.deviceMode === "manual" &&
      formData.manualSerialNumber.trim()
    ) {
      deviceAssignment = {
        mode: "SERIAL_NUMBER",
        serialNumber: formData.manualSerialNumber.trim(),
      };
    }

    const payload = {
      employeeCode: formData.employeeCode.trim(),
      firstName: formData.firstName.trim(),
      lastName: (formData.lastName || "").trim(),
      email: formData.email.trim(),
      phone: (formData.phone || "").trim(),
      designation: (formData.designation || "").trim(),
      roleId: formData.roleId ? Number(formData.roleId) : null,
      dateOfJoining: formData.dateOfJoining || null,
      accountStatus: formData.accountStatus || "ACTIVE",
      deviceAssignment,
    };

    try {
      setSubmitting(true);
      setServerError("");
      const targetId = employee?.id || employee?.employeeId;
      await onSave(targetId, payload);
    } catch (err) {
      console.error("Employee update failed:", err);
      setServerError(
        err.message ||
          "Failed to update employee details. Please check the values entered."
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen || !employee) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      closeOnBackdrop={false}
      title="Edit Employee Details"
      subtitle={`Update personal profile, employment role, and assigned hardware for ${employee.employeeName || employee.employeeId}`}
      size="lg"
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleSubmit} disabled={submitting}>
            {submitting ? "Saving Changes..." : "Save Changes"}
          </Button>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {loadingDetails && (
          <div className="text-xs text-blue-600 bg-blue-50 p-2.5 rounded-xl border border-blue-100 flex items-center gap-2">
            <span className="h-2 w-2 rounded-full bg-blue-500 animate-pulse" />
            Loading profile information...
          </div>
        )}

        {serverError && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3.5 text-xs text-red-700 font-medium">
            {serverError}
          </div>
        )}

        {/* Row 1: Name fields */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              First Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              placeholder="e.g. Alex"
              className={`mt-1.5 w-full rounded-xl border py-2.5 px-3.5 text-sm transition-colors focus:outline-none focus:ring-2 ${
                errors.firstName
                  ? "border-red-500 focus:ring-red-200"
                  : "border-[#DCE5E1] focus:border-[#1976D2] focus:ring-[#1976D2]/20"
              }`}
            />
            {errors.firstName && (
              <p className="mt-1 text-xs text-red-500">{errors.firstName}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Last Name
            </label>
            <input
              type="text"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              placeholder="e.g. Morgan"
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>
        </div>

        {/* Row 2: Employee Code & Email */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Employee ID / Code <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="employeeCode"
              value={formData.employeeCode}
              onChange={handleChange}
              placeholder="e.g. EMP-1001"
              className={`mt-1.5 w-full rounded-xl border py-2.5 px-3.5 text-sm font-mono transition-colors focus:outline-none focus:ring-2 ${
                errors.employeeCode
                  ? "border-red-500 focus:ring-red-200"
                  : "border-[#DCE5E1] focus:border-[#1976D2] focus:ring-[#1976D2]/20"
              }`}
            />
            {errors.employeeCode && (
              <p className="mt-1 text-xs text-red-500">{errors.employeeCode}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Work Email <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="alex.morgan@company.com"
              className={`mt-1.5 w-full rounded-xl border py-2.5 px-3.5 text-sm transition-colors focus:outline-none focus:ring-2 ${
                errors.email
                  ? "border-red-500 focus:ring-red-200"
                  : "border-[#DCE5E1] focus:border-[#1976D2] focus:ring-[#1976D2]/20"
              }`}
            />
            {errors.email && (
              <p className="mt-1 text-xs text-red-500">{errors.email}</p>
            )}
          </div>
        </div>

        {/* Row 3: Designation & Assigned Role */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Designation / Job Title
            </label>
            <input
              type="text"
              name="designation"
              value={formData.designation}
              onChange={handleChange}
              placeholder="e.g. Senior Software Engineer"
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Security Role <span className="text-red-500">*</span>
            </label>
            <select
              name="roleId"
              value={formData.roleId || ""}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 font-medium"
            >
              {availableRoles.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.roleName} {r.isSystemRole ? "(System)" : ""}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Row 4: Phone, Joining Date & Account Status */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Phone Number
            </label>
            <input
              type="text"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              placeholder="+1 (555) 000-0000"
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Date of Joining
            </label>
            <input
              type="date"
              name="dateOfJoining"
              value={formData.dateOfJoining}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Account Status
            </label>
            <select
              name="accountStatus"
              value={formData.accountStatus}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 font-medium"
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="SUSPENDED">SUSPENDED</option>
            </select>
          </div>
        </div>

        {/* Hardware Device Assignment Section */}
        <div className="border-t border-[#EBF0EE] pt-4 mt-2">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 mb-3">
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#03312E]">
                Assigned Hardware Device
              </h3>
              <p className="text-[11px] text-[#64748B]">
                {hasCurrentDevice ? (
                  <>
                    Currently linked: <strong className="font-mono text-[#1976D2]">{currentDeviceSerial}</strong> ({employee.deviceName || "Laptop"})
                  </>
                ) : (
                  "Currently no device assigned to this employee"
                )}
              </p>
            </div>

            <div className="flex items-center gap-1.5 flex-wrap">
              {hasCurrentDevice && (
                <button
                  type="button"
                  onClick={() =>
                    setFormData((p) => ({ ...p, deviceMode: "keep" }))
                  }
                  className={`px-2.5 py-1 text-xs rounded-lg font-medium cursor-pointer transition-colors ${
                    formData.deviceMode === "keep"
                      ? "bg-[#1976D2] text-white"
                      : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                  }`}
                >
                  Keep Current
                </button>
              )}
              <button
                type="button"
                onClick={() =>
                  setFormData((p) => ({ ...p, deviceMode: "select" }))
                }
                className={`px-2.5 py-1 text-xs rounded-lg font-medium cursor-pointer transition-colors ${
                  formData.deviceMode === "select"
                    ? "bg-[#1976D2] text-white"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
              >
                Available Devices
              </button>
              <button
                type="button"
                onClick={() =>
                  setFormData((p) => ({ ...p, deviceMode: "manual" }))
                }
                className={`px-2.5 py-1 text-xs rounded-lg font-medium cursor-pointer transition-colors ${
                  formData.deviceMode === "manual"
                    ? "bg-[#1976D2] text-white"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
              >
                Manual Serial #
              </button>
              <button
                type="button"
                onClick={() =>
                  setFormData((p) => ({ ...p, deviceMode: "none" }))
                }
                className={`px-2.5 py-1 text-xs rounded-lg font-medium cursor-pointer transition-colors ${
                  formData.deviceMode === "none"
                    ? "bg-[#EF4444] text-white"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
              >
                Unassign / None
              </button>
            </div>
          </div>

          {formData.deviceMode === "keep" && hasCurrentDevice && (
            <div className="rounded-xl border border-blue-200 bg-blue-50/70 p-3 text-xs space-y-1">
              <div className="flex items-center justify-between">
                <span className="font-bold text-blue-900">
                  {employee.deviceName || "Laptop"}
                </span>
                <span className="inline-flex items-center gap-1 font-bold text-blue-700">
                  ● Assigned
                </span>
              </div>
              <div className="font-mono text-blue-800">
                Serial Number: <span className="font-bold">{currentDeviceSerial}</span>
              </div>
              <div className="text-gray-600">
                This device will remain assigned to the employee.
              </div>
            </div>
          )}

          {formData.deviceMode === "select" && (
            <div className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Select Monitored Device (by Serial Number)
                </label>
                {loadingDevices ? (
                  <div className="mt-1.5 p-3 text-xs text-gray-500 bg-gray-50 rounded-xl">
                    Loading monitored devices...
                  </div>
                ) : availableDevices.length > 0 ? (
                  <select
                    name="selectedSerialNumber"
                    value={formData.selectedSerialNumber}
                    onChange={handleChange}
                    className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm font-medium transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
                  >
                    <option value="">-- Choose Monitored Device --</option>
                    {availableDevices.map((dev) => (
                      <option
                        key={dev.serialNumber || dev.deviceId}
                        value={dev.serialNumber}
                      >
                        {dev.serialNumber} — {dev.deviceName || `${dev.manufacturer || ""} ${dev.model || ""}`} ({dev.connectionStatus || "OFFLINE"})
                      </option>
                    ))}
                  </select>
                ) : (
                  <div className="mt-1.5 p-3 text-xs text-amber-700 bg-amber-50 rounded-xl border border-amber-200">
                    No unassigned devices currently detected. Switch to "Manual Serial #" to enter a serial number directly.
                  </div>
                )}
                {errors.selectedSerialNumber && (
                  <p className="mt-1 text-xs text-red-500">
                    {errors.selectedSerialNumber}
                  </p>
                )}
              </div>

              {selectedDeviceObj && (
                <div className="rounded-xl border border-emerald-200 bg-emerald-50/70 p-3 text-xs space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-emerald-900">
                      {selectedDeviceObj.deviceName || `${selectedDeviceObj.manufacturer || ""} ${selectedDeviceObj.model || ""}`}
                    </span>
                    <span className="inline-flex items-center gap-1 font-bold text-emerald-700">
                      <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
                      {selectedDeviceObj.connectionStatus || "ONLINE"}
                    </span>
                  </div>
                  <div className="font-mono text-emerald-800">
                    Serial:{" "}
                    <span className="font-bold">
                      {selectedDeviceObj.serialNumber}
                    </span>
                  </div>
                  <div className="text-gray-600">
                    OS: {selectedDeviceObj.operatingSystem || "Windows"} • CPU:{" "}
                    {selectedDeviceObj.processor || "Detecting..."}
                  </div>
                </div>
              )}
            </div>
          )}

          {formData.deviceMode === "manual" && (
            <div className="space-y-2">
              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Device Serial Number <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  name="manualSerialNumber"
                  value={formData.manualSerialNumber}
                  onChange={handleChange}
                  placeholder="e.g. PF5N8G2C"
                  className={`mt-1.5 w-full rounded-xl border py-2.5 px-3.5 text-sm font-mono uppercase transition-colors focus:outline-none focus:ring-2 ${
                    errors.manualSerialNumber
                      ? "border-red-500 focus:ring-red-200"
                      : "border-[#DCE5E1] focus:border-[#1976D2] focus:ring-[#1976D2]/20"
                  }`}
                />
                {errors.manualSerialNumber && (
                  <p className="mt-1 text-xs text-red-500">
                    {errors.manualSerialNumber}
                  </p>
                )}
              </div>

              {lookupState.loading && (
                <div className="text-[11px] text-gray-500 flex items-center gap-1.5">
                  <span className="h-2 w-2 rounded-full bg-blue-500 animate-pulse" />
                  Verifying serial number with backend registry...
                </div>
              )}

              {!lookupState.loading && lookupState.checked && (
                <div
                  className={`rounded-xl border p-2.5 text-xs ${
                    lookupState.assignable
                      ? "border-emerald-200 bg-emerald-50 text-emerald-800"
                      : "border-rose-200 bg-rose-50 text-rose-800"
                  }`}
                >
                  <p className="font-medium">{lookupState.message}</p>
                  {lookupState.device && (
                    <p className="mt-0.5 text-[11px] opacity-90 font-mono">
                      {lookupState.device.deviceName} (Status: {lookupState.device.connectionStatus})
                    </p>
                  )}
                </div>
              )}
            </div>
          )}

          {formData.deviceMode === "none" && (
            <div className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
              {hasCurrentDevice ? (
                <>
                  Saving will <strong>unassign</strong> device <span className="font-mono font-bold">{currentDeviceSerial}</span> from this employee. The device will be released back into the unassigned inventory.
                </>
              ) : (
                "This employee has no hardware device assigned."
              )}
            </div>
          )}
        </div>
      </form>
    </Modal>
  );
}

