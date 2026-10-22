import { useState, useEffect } from "react";
import { Modal, Button, Dropdown } from "../common";

export default function AddEmployeeModal({
  isOpen,
  onClose,
  onSave,
  roles = [],
  nextId = 13,
}) {
  const [formData, setFormData] = useState({
    employeeName: "",
    employeeId: `EMP-${1000 + nextId}`,
    email: "",
    role: roles[0] || "Lead Full Stack Engineer",
    deviceId: `DEV-${Math.floor(1000 + Math.random() * 9000)}`,
    deviceName: 'MacBook Pro 16" (M3)',
    deviceStatus: "Active",
    warrantyDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split("T")[0],
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isOpen) {
      setFormData({
        employeeName: "",
        employeeId: `EMP-${1000 + nextId}`,
        email: "",
        role: roles[0] || "Lead Full Stack Engineer",
        deviceId: `DEV-${Math.floor(1000 + Math.random() * 9000)}`,
        deviceName: 'MacBook Pro 16" (M3)',
        deviceStatus: "Active",
        warrantyDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000)
          .toISOString()
          .split("T")[0],
      });
      setErrors({});
    }
  }, [isOpen, nextId, roles]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const handleSubmit = (e) => {
    if (e) e.preventDefault();
    const newErrors = {};

    if (!formData.employeeName.trim()) {
      newErrors.employeeName = "Employee name is required";
    }
    if (!formData.employeeId.trim()) {
      newErrors.employeeId = "Employee ID is required";
    }
    if (!formData.deviceName.trim()) {
      newErrors.deviceName = "Device name is required";
    }
    if (!formData.deviceId.trim()) {
      newErrors.deviceId = "Device ID is required";
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    const newRecord = {
      id: Date.now(),
      ...formData,
      email:
        formData.email ||
        `${formData.employeeName.toLowerCase().replace(/\s+/g, ".")}@techaura.io`,
    };

    onSave(newRecord);
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Add New Employee"
      subtitle="Register staff member and link their assigned primary hardware"
      size="lg"
      footer={
        <>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleSubmit}>
            Save Employee
          </Button>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Employee Info Row */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Employee Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="employeeName"
              value={formData.employeeName}
              onChange={handleChange}
              placeholder="e.g. John Doe"
              className={`mt-1.5 w-full rounded-xl border py-2.5 px-3.5 text-sm transition-colors focus:outline-none focus:ring-2 ${
                errors.employeeName
                  ? "border-red-500 focus:ring-red-200"
                  : "border-[#DCE5E1] focus:border-[#1976D2] focus:ring-[#1976D2]/20"
              }`}
            />
            {errors.employeeName && (
              <p className="mt-1 text-xs text-red-500">{errors.employeeName}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Employee ID <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="employeeId"
              value={formData.employeeId}
              onChange={handleChange}
              placeholder="e.g. EMP-1013"
              className={`mt-1.5 w-full rounded-xl border py-2.5 px-3.5 text-sm font-mono transition-colors focus:outline-none focus:ring-2 ${
                errors.employeeId
                  ? "border-red-500 focus:ring-red-200"
                  : "border-[#DCE5E1] focus:border-[#1976D2] focus:ring-[#1976D2]/20"
              }`}
            />
            {errors.employeeId && (
              <p className="mt-1 text-xs text-red-500">{errors.employeeId}</p>
            )}
          </div>
        </div>

        {/* Email & Role */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Work Email
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="john.doe@techaura.io"
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Role
            </label>
            {roles && roles.length > 0 ? (
              <select
                name="role"
                value={formData.role}
                onChange={handleChange}
                className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm font-medium transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
              >
                {roles.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            ) : (
              <input
                type="text"
                name="role"
                value={formData.role}
                onChange={handleChange}
                placeholder="e.g. Lead Full Stack Engineer"
                className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
              />
            )}
          </div>
        </div>

        {/* Assigned Hardware Section */}
        <div className="border-t border-[#EBF0EE] pt-3">
          <h3 className="text-xs font-bold uppercase tracking-wider text-[#5C7370]">
            Assigned Hardware Device
          </h3>
        </div>

        {/* Device ID & Device Name */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Device ID <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="deviceId"
              value={formData.deviceId}
              onChange={handleChange}
              placeholder="e.g. DEV-4019"
              className={`mt-1.5 w-full rounded-xl border py-2.5 px-3.5 text-sm font-mono transition-colors focus:outline-none focus:ring-2 ${
                errors.deviceId
                  ? "border-red-500 focus:ring-red-200"
                  : "border-[#DCE5E1] focus:border-[#1976D2] focus:ring-[#1976D2]/20"
              }`}
            />
            {errors.deviceId && (
              <p className="mt-1 text-xs text-red-500">{errors.deviceId}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Device Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="deviceName"
              value={formData.deviceName}
              onChange={handleChange}
              placeholder="e.g. MacBook Pro 16"
              className={`mt-1.5 w-full rounded-xl border py-2.5 px-3.5 text-sm transition-colors focus:outline-none focus:ring-2 ${
                errors.deviceName
                  ? "border-red-500 focus:ring-red-200"
                  : "border-[#DCE5E1] focus:border-[#1976D2] focus:ring-[#1976D2]/20"
              }`}
            />
            {errors.deviceName && (
              <p className="mt-1 text-xs text-red-500">{errors.deviceName}</p>
            )}
          </div>
        </div>

        {/* Device Status & Warranty Date */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Device Status
            </label>
            <select
              name="deviceStatus"
              value={formData.deviceStatus}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm font-medium transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
            >
              <option value="Active">Active (Operational)</option>
              <option value="Repair">Repair (In Maintenance)</option>
              <option value="Inactive">Inactive (Decommissioned)</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Warranty Expiry Date
            </label>
            <input
              type="date"
              name="warrantyDate"
              value={formData.warrantyDate}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>
        </div>
      </form>
    </Modal>
  );
}
