import { useState, useEffect } from "react";
import { Modal, Button } from "../common";
import { employeeService } from "../../services/employeeService";

export default function AddEmployeeModal({
  isOpen,
  onClose,
  onSave,
  roles = [],
  nextId = 1,
}) {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    employeeCode: `EMP-${1000 + nextId}`,
    email: "",
    phone: "",
    designation: roles[0] || "Software Engineer",
    dateOfJoining: new Date().toISOString().split("T")[0],
    deviceMode: "select", // "select" | "manual" | "none"
    selectedSerialNumber: "",
    manualSerialNumber: "",
  });

  const [availableDevices, setAvailableDevices] = useState([]);
  const [loadingDevices, setLoadingDevices] = useState(false);
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

  useEffect(() => {
    if (isOpen) {
      setFormData({
        firstName: "",
        lastName: "",
        employeeCode: `EMP-${1000 + nextId}`,
        email: "",
        phone: "",
        designation: roles[0] || "Software Engineer",
        dateOfJoining: new Date().toISOString().split("T")[0],
        deviceMode: "select",
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

      // Fetch available devices from backend
      setLoadingDevices(true);
      employeeService
        .getAvailableDevices()
        .then((res) => {
          const list = res?.devices || (Array.isArray(res) ? res : []);
          setAvailableDevices(list);
          if (list.length > 0) {
            setFormData((prev) => ({
              ...prev,
              selectedSerialNumber: list[0].serialNumber || "",
            }));
          }
        })
        .catch(() => {
          setAvailableDevices([]);
        })
        .finally(() => {
          setLoadingDevices(false);
        });
    }
  }, [isOpen, nextId, roles]);

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
            "Device not found in registry. You can still assign it; telemetry will link when the agent starts.",
        });
      }
    }, 400);

    return () => clearTimeout(timer);
  }, [formData.manualSerialNumber, formData.deviceMode]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const updated = { ...prev, [name]: value };
      // Auto-suggest email when first & last name change
      if (
        (name === "firstName" || name === "lastName") &&
        (!prev.email || prev.email.includes("@company.com"))
      ) {
        const f = name === "firstName" ? value : prev.firstName;
        const l = name === "lastName" ? value : prev.lastName;
        const cleanF = (f || "").trim().toLowerCase();
        const cleanL = (l || "").trim().toLowerCase();
        if (cleanF || cleanL) {
          updated.email = `${[cleanF, cleanL].filter(Boolean).join(".")}@company.com`;
        }
      }
      return updated;
    });

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

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    let deviceAssignment = { mode: "NONE" };
    if (formData.deviceMode === "select" && formData.selectedSerialNumber) {
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
      dateOfJoining: formData.dateOfJoining || null,
      deviceAssignment,
    };

    try {
      setSubmitting(true);
      setServerError("");
      await onSave(payload);
    } catch (err) {
      console.error("Employee registration failed:", err);
      setServerError(
        err.message ||
          "Failed to register employee. Check if employee code or email already exists."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Add New Employee"
      subtitle="Register staff member and link their assigned hardware device by unique Serial Number"
      size="lg"
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleSubmit} disabled={submitting}>
            {submitting ? "Saving..." : "Save Employee"}
          </Button>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
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
              Employee ID <span className="text-red-500">*</span>
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

        {/* Row 3: Role & Phone */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Designation / Role
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
        </div>

        {/* Row 4: Date of Joining */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
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
        </div>

        {/* Hardware Device Assignment Section */}
        <div className="border-t border-[#EBF0EE] pt-4 mt-2">
          <div className="flex items-center justify-between mb-2">
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#03312E]">
                Assign Hardware Device
              </h3>
              <p className="text-[11px] text-[#64748B]">
                Link the live agent telemetry metrics to this employee
              </p>
            </div>
            <div className="flex items-center gap-2">
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
                    ? "bg-[#1976D2] text-white"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
              >
                No Device
              </button>
            </div>
          </div>

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

              {/* Live lookup state indicator */}
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

              <p className="text-[11px] text-[#64748B]">
                The agent device telemetry will automatically link to this employee based on the hardware serial number.
              </p>
            </div>
          )}

          {formData.deviceMode === "none" && (
            <div className="rounded-xl border border-gray-200 bg-gray-50 p-3 text-xs text-gray-600">
              This employee will be created without an assigned device. You can assign a device by serial number at any time later.
            </div>
          )}
        </div>
      </form>
    </Modal>
  );
}
