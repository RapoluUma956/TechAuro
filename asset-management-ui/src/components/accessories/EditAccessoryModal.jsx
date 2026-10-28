import React, { useState, useEffect } from "react";
import { Modal, Button } from "../common";

export default function EditAccessoryModal({
  isOpen,
  onClose,
  accessory,
  onSave,
}) {
  const [accessoryName, setAccessoryName] = useState("");
  const [category, setCategory] = useState("Mouse");
  const [subCategory, setSubCategory] = useState("");
  const [brand, setBrand] = useState("");
  const [model, setModel] = useState("");
  const [totalQuantity, setTotalQuantity] = useState(0);
  const [availableQuantity, setAvailableQuantity] = useState(0);
  const [damagedQuantity, setDamagedQuantity] = useState(0);
  const [description, setDescription] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (accessory) {
      setAccessoryName(accessory.accessoryName || accessory.productName || "");
      setCategory(accessory.category || "Mouse");
      setSubCategory(accessory.subCategory || "");
      setBrand(accessory.brand || "");
      setModel(accessory.model || "");
      setTotalQuantity(accessory.totalQuantity || 0);
      setAvailableQuantity(accessory.availableQuantity || 0);
      setDamagedQuantity(accessory.damagedQuantity || 0);
      setDescription(accessory.description || "");
      setErrorMessage("");
    }
  }, [accessory]);

  if (!isOpen || !accessory) return null;

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setErrorMessage("");

    try {
      setSubmitting(true);
      await onSave({
        ...accessory,
        accessoryName: accessoryName.trim(),
        productName: accessoryName.trim(),
        category: category.trim(),
        subCategory: subCategory.trim(),
        brand: brand.trim(),
        model: model.trim(),
        totalQuantity: parseInt(totalQuantity, 10) || 0,
        availableQuantity: parseInt(availableQuantity, 10) || 0,
        damagedQuantity: parseInt(damagedQuantity, 10) || 0,
        description: description.trim(),
      });
      onClose();
    } catch (err) {
      setErrorMessage(err.message || "Failed to update accessory.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Edit Accessory"
      subtitle={`Update details and inventory levels for ${accessory.accessoryName || accessory.productName}`}
      size="md"
      footer={
        <div className="flex items-center justify-end gap-3 w-full">
          <Button variant="outline" size="md" onClick={onClose}>
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

        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Accessory Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            required
            value={accessoryName}
            onChange={(e) => setAccessoryName(e.target.value)}
            className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Category <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Sub-Category
            </label>
            <input
              type="text"
              value={subCategory}
              onChange={(e) => setSubCategory(e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Brand <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              value={brand}
              onChange={(e) => setBrand(e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
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
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>
        </div>

        <div className="grid grid-cols-3 gap-3">
          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Total Units
            </label>
            <input
              type="number"
              min="0"
              value={totalQuantity}
              onChange={(e) => setTotalQuantity(parseInt(e.target.value, 10) || 0)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Available Units
            </label>
            <input
              type="number"
              min="0"
              value={availableQuantity}
              onChange={(e) => setAvailableQuantity(parseInt(e.target.value, 10) || 0)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              In Repair
            </label>
            <input
              type="number"
              min="0"
              value={damagedQuantity}
              onChange={(e) => setDamagedQuantity(parseInt(e.target.value, 10) || 0)}
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2 px-3 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Description
          </label>
          <textarea
            rows={2}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
          />
        </div>
      </form>
    </Modal>
  );
}
