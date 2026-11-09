import React, { useState, useEffect } from "react";
import { Modal, Button } from "../common";

export default function EditLaptopModal({
  isOpen,
  onClose,
  laptop,
  onSave,
}) {
  const [formData, setFormData] = useState({
    serialNumber: "",
    assetTag: "",
    brand: "",
    model: "",
    warrantyExpiryDate: "",
    deviceStatus: "ACTIVE",
  });
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (laptop) {
      setFormData({
        serialNumber: laptop.serialNumber || "",
        assetTag: laptop.assetTag || "",
        brand: laptop.brand || "",
        model: laptop.model || "",
        warrantyExpiryDate: laptop.warrantyExpiryDate
          ? String(laptop.warrantyExpiryDate).split("T")[0]
          : "",
        deviceStatus: laptop.deviceStatus || "ACTIVE",
      });
      setErrorMessage("");
    }
  }, [laptop]);

  if (!isOpen || !laptop) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setErrorMessage("");

    if (!formData.serialNumber.trim()) {
      setErrorMessage("Serial number is required.");
      return;
    }

    try {
      setSubmitting(true);
      await onSave(laptop.deviceId, {
        ...laptop,
        serialNumber: formData.serialNumber.trim(),
        assetTag: formData.assetTag.trim() || null,
        brand: formData.brand.trim() || laptop.brand,
        model: formData.model.trim() || laptop.model,
        warrantyExpiryDate: formData.warrantyExpiryDate || null,
        deviceStatus: formData.deviceStatus || "ACTIVE",
      });
      onClose();
    } catch (err) {
      setErrorMessage(err.message || "Failed to update laptop device.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      closeOnBackdrop={false}
      title="Edit Laptop Device"
      subtitle={`Update hardware identification and status for ${laptop.serialNumber}`}
      size="md"
      footer={
        <div className="flex w-full items-center justify-end gap-3">
          <Button variant="outline" size="md" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button
            variant="primary"
            size="md"
            isLoading={submitting}
            onClick={handleSubmit}
          >
            Save Changes
          </Button>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {errorMessage && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs font-medium text-red-700">
            {errorMessage}
          </div>
        )}

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Serial Number <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="serialNumber"
              required
              value={formData.serialNumber}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 font-mono text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Asset Tag / ID
            </label>
            <input
              type="text"
              name="assetTag"
              value={formData.assetTag}
              onChange={handleChange}
              placeholder="e.g. AST-101"
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 font-mono text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Brand
            </label>
            <input
              type="text"
              name="brand"
              value={formData.brand}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Model
            </label>
            <input
              type="text"
              name="model"
              value={formData.model}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Warranty Expiry Date
            </label>
            <input
              type="date"
              name="warrantyExpiryDate"
              value={formData.warrantyExpiryDate}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Hardware Status
            </label>
            <select
              name="deviceStatus"
              value={formData.deviceStatus}
              onChange={handleChange}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 font-medium"
            >
              <option value="ACTIVE">ACTIVE (Operational)</option>
              <option value="REPAIR">REPAIR (In Maintenance)</option>
              <option value="RETIRED">RETIRED (Decommissioned)</option>
            </select>
          </div>
        </div>
      </form>
    </Modal>
  );
}

