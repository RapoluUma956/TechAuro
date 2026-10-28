import React, { useState, useEffect } from "react";
import { Modal, Button } from "../common";

export default function AddAccessoryModal({
  isOpen,
  onClose,
  onLaptopAdded,
  onAccessoryAdded,
  categories = [],
}) {
  const [accessoryType, setAccessoryType] = useState("laptop"); // 'laptop' | 'other'

  // Common & Laptop state
  const [brand, setBrand] = useState("");
  const [model, setModel] = useState("");
  const [units, setUnits] = useState(1);
  const [ram, setRam] = useState("16 GB");
  const [ssd, setSsd] = useState("512 GB");
  const [warrantyDate, setWarrantyDate] = useState("");
  const [purchaseDate, setPurchaseDate] = useState(
    () => new Date().toISOString().split("T")[0]
  );
  // Serial numbers: array of strings for bulk units
  const [serialNumbers, setSerialNumbers] = useState([""]);
  const [bulkSerialText, setBulkSerialText] = useState("");
  const [serialInputMode, setSerialInputMode] = useState("fields"); // 'fields' | 'bulk_paste'

  // Other accessory state
  const [accessoryName, setAccessoryName] = useState("");
  const [category, setCategory] = useState("Mouse");
  const [subCategory, setSubCategory] = useState("Wireless");
  const [description, setDescription] = useState("");

  // Submitting / UI states
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // Sync serial numbers array when units count changes
  useEffect(() => {
    const unitCount = Math.max(1, parseInt(units, 10) || 1);
    setSerialNumbers((prev) => {
      const next = [...prev];
      if (next.length < unitCount) {
        while (next.length < unitCount) {
          next.push("");
        }
      } else if (next.length > unitCount) {
        return next.slice(0, unitCount);
      }
      return next;
    });
  }, [units]);

  if (!isOpen) return null;

  const handleReset = () => {
    setBrand("");
    setModel("");
    setUnits(1);
    setRam("16 GB");
    setSsd("512 GB");
    setWarrantyDate("");
    setPurchaseDate(new Date().toISOString().split("T")[0]);
    setSerialNumbers([""]);
    setBulkSerialText("");
    setAccessoryName("");
    setCategory("Mouse");
    setSubCategory("Wireless");
    setDescription("");
    setErrorMessage("");
  };

  const handleSerialNumberChange = (index, value) => {
    const updated = [...serialNumbers];
    updated[index] = value;
    setSerialNumbers(updated);
  };

  const handleBulkTextChange = (text) => {
    setBulkSerialText(text);
    const parsed = text
      .split(/[\n,]+/)
      .map((s) => s.trim())
      .filter(Boolean);
    if (parsed.length > 0) {
      setUnits(parsed.length);
      setSerialNumbers(parsed);
    }
  };

  const handleGenerateSerials = () => {
    if (!brand.trim()) {
      setErrorMessage("Please enter Brand first to auto-generate serials.");
      return;
    }
    const bPrefix = brand.replaceAll(/[^a-zA-Z0-9]/g, "").substring(0, 4).toUpperCase() || "SN";
    const mPrefix = model.replaceAll(/[^a-zA-Z0-9]/g, "").substring(0, 4).toUpperCase() || "MOD";
    const unitCount = Math.max(1, parseInt(units, 10) || 1);
    const randomSeed = Math.floor(1000 + Math.random() * 9000);
    const generated = [];
    for (let i = 1; i <= unitCount; i++) {
      generated.push(`${bPrefix}-${mPrefix}-${randomSeed}-${String(i).padStart(3, "0")}`);
    }
    setSerialNumbers(generated);
    setBulkSerialText(generated.join("\n"));
  };

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setErrorMessage("");

    try {
      if (accessoryType === "laptop") {
        if (!brand.trim()) throw new Error("Brand is required.");
        if (!model.trim()) throw new Error("Model is required.");
        if (!warrantyDate) throw new Error("Warranty expiry date is required.");
        const unitCount = Math.max(1, parseInt(units, 10) || 1);

        let finalSerials = serialNumbers.map((s) => s.trim()).filter(Boolean);
        if (serialInputMode === "bulk_paste" && bulkSerialText.trim()) {
          finalSerials = bulkSerialText
            .split(/[\n,]+/)
            .map((s) => s.trim())
            .filter(Boolean);
        }

        if (finalSerials.length !== unitCount) {
          throw new Error(
            `You specified ${unitCount} unit(s) but provided ${finalSerials.length} serial number(s). Each physical laptop requires a unique serial number.`
          );
        }

        const uniqueSet = new Set(finalSerials.map((s) => s.toUpperCase()));
        if (uniqueSet.size !== finalSerials.length) {
          throw new Error("Duplicate serial numbers found. Each unit must have a distinct serial number.");
        }

        setSubmitting(true);
        await onLaptopAdded({
          brand: brand.trim(),
          model: model.trim(),
          ram,
          ssd,
          quantity: unitCount,
          serialNumbers: finalSerials,
          warrantyExpiryDate: warrantyDate,
          purchaseDate: purchaseDate || new Date().toISOString().split("T")[0],
        });
      } else {
        if (!accessoryName.trim()) throw new Error("Accessory name is required.");
        if (!brand.trim()) throw new Error("Brand is required.");
        if (!model.trim()) throw new Error("Model is required.");
        const unitCount = Math.max(1, parseInt(units, 10) || 1);

        setSubmitting(true);
        await onAccessoryAdded({
          accessoryName: accessoryName.trim(),
          category: category || "Other",
          subCategory: subCategory.trim(),
          brand: brand.trim(),
          manufacturer: brand.trim(),
          model: model.trim(),
          quantity: unitCount,
          purchaseDate: purchaseDate || new Date().toISOString().split("T")[0],
          description: description.trim(),
        });
      }

      handleReset();
      onClose();
    } catch (err) {
      setErrorMessage(err.message || "Failed to save accessory.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Add New Accessory"
      subtitle="Choose the accessory type to preview and complete its corresponding registration form"
      size="lg"
      footer={
        <div className="flex w-full items-center justify-between">
          <Button variant="outline" size="sm" onClick={handleReset}>
            Reset
          </Button>

          <div className="flex items-center gap-3">
            <Button variant="outline" size="md" onClick={onClose}>
              Cancel
            </Button>
            <Button
              variant="primary"
              size="md"
              isLoading={submitting}
              onClick={handleSubmit}
            >
              {accessoryType === "laptop"
                ? `Register ${units > 1 ? `${units} Laptops` : "Laptop"}`
                : `Add ${units > 1 ? `${units} Units` : "Accessory"}`}
            </Button>
          </div>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Error Alert */}
        {errorMessage && (
          <div className="flex items-center gap-2 rounded-xl border border-red-200 bg-red-50 p-3.5 text-xs font-medium text-red-700">
            <span className="text-base">⚠️</span>
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Accessory Type Radio Selection */}
        <div className="space-y-2">
          <label className="block text-xs font-bold uppercase tracking-wider text-[#5C7370]">
            Accessory Type
          </label>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label
              className={`flex cursor-pointer items-start gap-3 rounded-2xl border p-4 transition-all ${
                accessoryType === "laptop"
                  ? "border-[#1976D2] bg-[#E3F2FD]/50 shadow-xs ring-1 ring-[#1976D2]"
                  : "border-[#DCE5E1] bg-[#F8FAF9] hover:border-[#1976D2]/60"
              }`}
            >
              <input
                type="radio"
                name="accessoryType"
                value="laptop"
                checked={accessoryType === "laptop"}
                onChange={() => setAccessoryType("laptop")}
                className="mt-1 accent-[#1976D2]"
              />
              <div>
                <div className="text-sm font-bold text-[#03312E]">
                  Laptop — individually tracked device
                </div>
                <div className="mt-1 text-xs text-[#5C7370]">
                  Hardware recorded before employee assignment with distinct serial numbers.
                </div>
              </div>
            </label>

            <label
              className={`flex cursor-pointer items-start gap-3 rounded-2xl border p-4 transition-all ${
                accessoryType === "other"
                  ? "border-[#1976D2] bg-[#E3F2FD]/50 shadow-xs ring-1 ring-[#1976D2]"
                  : "border-[#DCE5E1] bg-[#F8FAF9] hover:border-[#1976D2]/60"
              }`}
            >
              <input
                type="radio"
                name="accessoryType"
                value="other"
                checked={accessoryType === "other"}
                onChange={() => setAccessoryType("other")}
                className="mt-1 accent-[#1976D2]"
              />
              <div>
                <div className="text-sm font-bold text-[#03312E]">
                  Other accessory — quantity-based inventory
                </div>
                <div className="mt-1 text-xs text-[#5C7370]">
                  Office peripherals such as mouse, keyboard, monitor, headset, or docks.
                </div>
              </div>
            </label>
          </div>
        </div>

        {/* Dynamic Form Sections */}
        {accessoryType === "laptop" ? (
          /* ==================== LAPTOP FORM ==================== */
          <div className="space-y-4">
            {/* Callout Notice */}
            <div className="flex items-start gap-3 rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] p-3.5 text-xs text-[#5C7370]">
              <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
                <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="2" y="3" width="20" height="14" rx="2" />
                  <line x1="2" y1="20" x2="22" y2="20" />
                </svg>
              </div>
              <div>
                <div className="font-bold text-[#03312E]">Laptop Registration</div>
                <div>
                  Hardware specification recorded in device table before employee assignment.
                </div>
              </div>
            </div>

            {/* Brand & Model */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Brand <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={brand}
                  onChange={(e) => setBrand(e.target.value)}
                  placeholder="e.g. Lenovo"
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Model <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={model}
                  onChange={(e) => setModel(e.target.value)}
                  placeholder="e.g. ThinkPad E14"
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>
            </div>

            {/* Units & Specs */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Number of units <span className="text-red-500">*</span>
                </label>
                <input
                  type="number"
                  min="1"
                  max="100"
                  required
                  value={units}
                  onChange={(e) => setUnits(Math.max(1, parseInt(e.target.value, 10) || 1))}
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  RAM <span className="text-red-500">*</span>
                </label>
                <select
                  value={ram}
                  onChange={(e) => setRam(e.target.value)}
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                >
                  <option value="8 GB">8 GB</option>
                  <option value="16 GB">16 GB</option>
                  <option value="32 GB">32 GB</option>
                  <option value="64 GB">64 GB</option>
                  <option value="128 GB">128 GB</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  SSD Storage <span className="text-red-500">*</span>
                </label>
                <select
                  value={ssd}
                  onChange={(e) => setSsd(e.target.value)}
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                >
                  <option value="256 GB">256 GB</option>
                  <option value="512 GB">512 GB</option>
                  <option value="1 TB">1 TB</option>
                  <option value="2 TB">2 TB</option>
                  <option value="4 TB">4 TB</option>
                </select>
              </div>
            </div>

            {/* Dates */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Warranty Expiry Date <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  required
                  value={warrantyDate}
                  onChange={(e) => setWarrantyDate(e.target.value)}
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Purchase Date
                </label>
                <input
                  type="date"
                  value={purchaseDate}
                  onChange={(e) => setPurchaseDate(e.target.value)}
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>
            </div>

            {/* Serial Numbers Box */}
            <div className="rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] p-4">
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <label className="block text-xs font-bold text-[#03312E]">
                    Serial Number(s) <span className="text-red-500">*</span> ({units} required)
                  </label>
                  <span className="text-[11px] text-[#5C7370]">
                    Each physical laptop needs its own distinct serial number.
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={handleGenerateSerials}
                    className="cursor-pointer rounded-lg border border-[#DCE5E1] bg-white px-2.5 py-1 text-xs font-semibold text-[#1976D2] hover:bg-[#E3F2FD]"
                  >
                    ⚡ Auto-generate
                  </button>
                  {units > 1 && (
                    <button
                      type="button"
                      onClick={() =>
                        setSerialInputMode(
                          serialInputMode === "fields" ? "bulk_paste" : "fields"
                        )
                      }
                      className="cursor-pointer rounded-lg border border-[#DCE5E1] bg-white px-2.5 py-1 text-xs font-semibold text-[#03312E] hover:bg-slate-100"
                    >
                      {serialInputMode === "fields" ? "Bulk Paste Mode" : "Field Mode"}
                    </button>
                  )}
                </div>
              </div>

              <div className="mt-3">
                {units === 1 ? (
                  <input
                    type="text"
                    required
                    value={serialNumbers[0] || ""}
                    onChange={(e) => handleSerialNumberChange(0, e.target.value)}
                    placeholder="Unique serial number (e.g. LEN-E14-001)"
                    className="w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 font-mono text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                  />
                ) : serialInputMode === "bulk_paste" ? (
                  <div>
                    <textarea
                      rows={4}
                      value={bulkSerialText}
                      onChange={(e) => handleBulkTextChange(e.target.value)}
                      placeholder={`Paste ${units} serial numbers, separated by newlines or commas...`}
                      className="w-full rounded-xl border border-[#DCE5E1] bg-white p-3 font-mono text-xs text-[#03312E] placeholder-[#94A3B8] focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                    />
                    <div className="mt-1 text-[11px] text-[#5C7370]">
                      {
                        bulkSerialText
                          .split(/[\n,]+/)
                          .map((s) => s.trim())
                          .filter(Boolean).length
                      }{" "}
                      / {units} serial numbers detected.
                    </div>
                  </div>
                ) : (
                  <div className="max-h-48 space-y-2 overflow-y-auto pr-1">
                    {serialNumbers.map((sn, idx) => (
                      <div key={idx} className="flex items-center gap-2">
                        <span className="w-16 text-xs font-semibold text-[#5C7370]">
                          Unit #{idx + 1}
                        </span>
                        <input
                          type="text"
                          required
                          value={sn}
                          onChange={(e) => handleSerialNumberChange(idx, e.target.value)}
                          placeholder={`Serial number for unit ${idx + 1}`}
                          className="flex-1 rounded-xl border border-[#DCE5E1] bg-white py-2 px-3 font-mono text-xs text-[#03312E] placeholder-[#94A3B8] focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                        />
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : (
          /* ==================== OTHER ACCESSORY FORM ==================== */
          <div className="space-y-4">
            {/* Callout Notice */}
            <div className="flex items-start gap-3 rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] p-3.5 text-xs text-[#5C7370]">
              <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
                <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
                </svg>
              </div>
              <div>
                <div className="font-bold text-[#03312E]">Other Accessory Registration</div>
                <div>
                  Quantity-based peripheral stock. If identical brand and model already exist, inventory units will automatically increment.
                </div>
              </div>
            </div>

            {/* Name & Category */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Accessory Name <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={accessoryName}
                  onChange={(e) => setAccessoryName(e.target.value)}
                  placeholder="e.g. Wireless Mouse"
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Category <span className="text-red-500">*</span>
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                >
                  {categories && categories.length > 0 ? (
                    categories.map((c) => {
                      const catName = typeof c === "string" ? c : c.category;
                      return (
                        <option key={catName} value={catName}>
                          {catName}
                        </option>
                      );
                    })
                  ) : (
                    <>
                      <option value="Mouse">Mouse</option>
                      <option value="Keyboard">Keyboard</option>
                      <option value="Monitor">Monitor</option>
                      <option value="Headset">Headset</option>
                      <option value="Docking Station">Docking Station</option>
                      <option value="Charger / Adapter">Charger / Adapter</option>
                      <option value="Webcam">Webcam</option>
                      <option value="Other">Other</option>
                    </>
                  )}
                </select>
              </div>
            </div>

            {/* Brand, Model, Units */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Brand <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={brand}
                  onChange={(e) => setBrand(e.target.value)}
                  placeholder="e.g. Logitech"
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Model <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={model}
                  onChange={(e) => setModel(e.target.value)}
                  placeholder="e.g. M185"
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Number of units <span className="text-red-500">*</span>
                </label>
                <input
                  type="number"
                  min="1"
                  required
                  value={units}
                  onChange={(e) => setUnits(Math.max(1, parseInt(e.target.value, 10) || 1))}
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>
            </div>

            {/* Sub-category & Purchase Date */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Sub-Category / Specs
                </label>
                <input
                  type="text"
                  value={subCategory}
                  onChange={(e) => setSubCategory(e.target.value)}
                  placeholder="e.g. Wireless, USB-C"
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#03312E]">
                  Purchase Date
                </label>
                <input
                  type="date"
                  value={purchaseDate}
                  onChange={(e) => setPurchaseDate(e.target.value)}
                  className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
                />
              </div>
            </div>

            {/* Description */}
            <div>
              <label className="block text-xs font-semibold text-[#03312E]">
                Description
              </label>
              <textarea
                rows={2}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Optional peripheral item details or notes..."
                className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] placeholder-[#94A3B8] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
              />
            </div>
          </div>
        )}
      </form>
    </Modal>
  );
}
