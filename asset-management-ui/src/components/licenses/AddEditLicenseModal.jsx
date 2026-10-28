import React, { useState, useEffect } from "react";
import { Modal, Button } from "../common";
import { EyeIcon, EyeSlashIcon } from "./LicenseIcons";

export default function AddEditLicenseModal({
  isOpen,
  onClose,
  license = null, // null for Add mode, or license object for Edit mode
  onSave,
}) {
  const isEdit = Boolean(license && license.licenseId);

  const [formData, setFormData] = useState({
    licenseName: "",
    softwareName: "",
    publisher: "",
    licenseType: "SUBSCRIPTION",
    renewalType: "YEARLY",
    renewalMethod: "AUTO",
    licenseKey: "",
    totalQuantity: 100,
    purchaseDate: new Date().toISOString().split("T")[0],
    startDate: new Date().toISOString().split("T")[0],
    expiryDate: "",
    status: "ACTIVE",
    notes: "",
  });

  const [showKey, setShowKey] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (isOpen) {
      setErrorMessage("");
      setShowKey(false);
      if (license) {
        setFormData({
          licenseName: license.licenseName || "",
          softwareName: license.softwareName || "",
          publisher: license.publisher || "",
          licenseType: license.licenseType || "SUBSCRIPTION",
          renewalType: license.renewalType || "YEARLY",
          renewalMethod: license.renewalMethod || "AUTO",
          licenseKey: license.licenseKey || "",
          totalQuantity: license.totalQuantity ?? license.totalLicenses ?? 1,
          purchaseDate: license.purchaseDate || "",
          startDate: license.startDate || "",
          expiryDate: license.expiryDate || "",
          status: license.status || "ACTIVE",
          notes: license.notes || "",
        });
      } else {
        setFormData({
          licenseName: "",
          softwareName: "",
          publisher: "",
          licenseType: "SUBSCRIPTION",
          renewalType: "YEARLY",
          renewalMethod: "AUTO",
          licenseKey: "",
          totalQuantity: 100,
          purchaseDate: new Date().toISOString().split("T")[0],
          startDate: new Date().toISOString().split("T")[0],
          expiryDate: "",
          status: "ACTIVE",
          notes: "",
        });
      }
    }
  }, [isOpen, license]);

  if (!isOpen) return null;

  const handleChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setErrorMessage("");

    // Validations
    if (!formData.licenseName.trim()) {
      setErrorMessage("License Name is required.");
      return;
    }
    if (!formData.softwareName.trim()) {
      setErrorMessage("Software Name is required.");
      return;
    }
    if (!formData.publisher.trim()) {
      setErrorMessage("Publisher is required.");
      return;
    }
    const qty = parseInt(formData.totalQuantity, 10);
    if (isNaN(qty) || qty <= 0) {
      setErrorMessage("Total Quantity (Seats) must be a positive integer.");
      return;
    }

    if (isEdit && license && license.assignedQuantity) {
      if (qty < license.assignedQuantity) {
        setErrorMessage(
          `Cannot reduce total seats below current active assignments (${license.assignedQuantity} assigned).`
        );
        return;
      }
    }

    if (formData.startDate && formData.expiryDate) {
      if (new Date(formData.expiryDate) < new Date(formData.startDate)) {
        setErrorMessage("Expiry Date cannot be earlier than Start Date.");
        return;
      }
    }

    const payload = {
      licenseName: formData.licenseName.trim(),
      softwareName: formData.softwareName.trim(),
      publisher: formData.publisher.trim(),
      licenseType: formData.licenseType,
      renewalType: formData.renewalType,
      renewalMethod: formData.renewalMethod,
      licenseKey: formData.licenseKey ? formData.licenseKey.trim() : null,
      totalQuantity: qty,
      purchaseDate: formData.purchaseDate || null,
      startDate: formData.startDate || null,
      expiryDate: formData.expiryDate || null,
      status: formData.status,
      notes: formData.notes ? formData.notes.trim() : null,
    };

    try {
      setSubmitting(true);
      await onSave(payload, license?.licenseId);
      onClose();
    } catch (err) {
      setErrorMessage(err.message || "Failed to save license. Please try again.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEdit ? "Edit License" : "Add License"}
      subtitle={
        isEdit
          ? "Update license parameters, seats, and contract validity."
          : "Register a new software license into organizational inventory."
      }
      size="xl"
      footer={
        <div className="flex w-full items-center justify-end gap-3">
          <Button variant="outline" size="md" onClick={onClose}>
            Cancel
          </Button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={submitting}
            className="cursor-pointer rounded-xl bg-[#03312E] px-6 py-2.5 text-sm font-semibold text-white shadow-xs transition-colors hover:bg-[#02211F] disabled:cursor-not-allowed disabled:opacity-50"
          >
            {submitting
              ? "Saving..."
              : isEdit
              ? "Update License"
              : "Save License"}
          </button>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Error message */}
        {errorMessage && (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs font-semibold text-red-700">
            ⚠️ {errorMessage}
          </div>
        )}

        {/* 2-Column Grid */}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {/* Row 1: License Name & Software Name */}
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              License Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Microsoft 365 Business"
              value={formData.licenseName}
              onChange={(e) => handleChange("licenseName", e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Software Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Microsoft 365"
              value={formData.softwareName}
              onChange={(e) => handleChange("softwareName", e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          {/* Row 2: Publisher & License Type */}
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Publisher <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Microsoft"
              value={formData.publisher}
              onChange={(e) => handleChange("publisher", e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              License Type <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.licenseType}
              onChange={(e) => handleChange("licenseType", e.target.value)}
              className="mt-1.5 w-full appearance-none rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
            >
              <option value="SUBSCRIPTION">Subscription</option>
              <option value="PERPETUAL">Perpetual</option>
              <option value="VOLUME">Volume License</option>
              <option value="OEM">OEM (Bundled with device)</option>
              <option value="FREE_OPEN_SOURCE">Free / Open Source</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          {/* Row 3: Renewal Type & License Key */}
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Renewal Type <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.renewalType}
              onChange={(e) => handleChange("renewalType", e.target.value)}
              className="mt-1.5 w-full appearance-none rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
            >
              <option value="YEARLY">Yearly</option>
              <option value="MONTHLY">Monthly</option>
              <option value="ONE_TIME">One-time</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              License Key
            </label>
            <div className="relative mt-1.5">
              <input
                type={showKey ? "text" : "password"}
                placeholder="Enter license key (optional)"
                value={formData.licenseKey}
                onChange={(e) => handleChange("licenseKey", e.target.value)}
                className="w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 pl-3.5 pr-10 text-sm font-mono text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
              />
              <button
                type="button"
                onClick={() => setShowKey(!showKey)}
                aria-label={showKey ? "Hide license key" : "Show license key"}
                className="absolute inset-y-0 right-0 flex items-center pr-3 text-[#64748B] hover:text-[#03312E] cursor-pointer"
              >
                {showKey ? (
                  <EyeSlashIcon className="h-4 w-4" />
                ) : (
                  <EyeIcon className="h-4 w-4" />
                )}
              </button>
            </div>
          </div>

          {/* Row 4: Total Quantity (Seats) & Purchase Date */}
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Total Quantity (Seats) <span className="text-red-500">*</span>
              {isEdit && license?.assignedQuantity > 0 && (
                <span className="ml-1 font-normal text-[#64748B]">
                  (min {license.assignedQuantity} active)
                </span>
              )}
            </label>
            <input
              type="number"
              min="1"
              required
              placeholder="e.g. 100"
              value={formData.totalQuantity}
              onChange={(e) => handleChange("totalQuantity", e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Purchase Date <span className="text-red-500">*</span>
            </label>
            <input
              type="date"
              required
              value={formData.purchaseDate}
              onChange={(e) => handleChange("purchaseDate", e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
            />
          </div>

          {/* Row 5: Start Date & Expiry Date */}
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Start Date
            </label>
            <input
              type="date"
              value={formData.startDate}
              onChange={(e) => handleChange("startDate", e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Expiry Date
            </label>
            <input
              type="date"
              value={formData.expiryDate}
              onChange={(e) => handleChange("expiryDate", e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
            />
          </div>

          {/* Row 6: Status & Renewal Method */}
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Status <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.status}
              onChange={(e) => handleChange("status", e.target.value)}
              className="mt-1.5 w-full appearance-none rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
            >
              <option value="ACTIVE">Active</option>
              <option value="EXPIRED">Expired</option>
              <option value="SUSPENDED">Suspended</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Renewal Method
            </label>
            <select
              value={formData.renewalMethod}
              onChange={(e) => handleChange("renewalMethod", e.target.value)}
              className="mt-1.5 w-full appearance-none rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
            >
              <option value="AUTO">Automatic Renewal</option>
              <option value="MANUAL">Manual Renewal</option>
              <option value="NOT_APPLICABLE">Not Applicable</option>
            </select>
          </div>
        </div>

        {/* Notes (Full width) */}
        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Notes
          </label>
          <textarea
            rows={2}
            placeholder="Additional notes (optional)"
            value={formData.notes}
            onChange={(e) => handleChange("notes", e.target.value)}
            className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white p-3 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
          />
        </div>
      </form>
    </Modal>
  );
}
