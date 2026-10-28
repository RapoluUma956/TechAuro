import React, { useState } from "react";
import { Modal, Button } from "../common";

export default function QuickAddStockModal({
  isOpen,
  onClose,
  accessory,
  onAddStock,
}) {
  const [quantity, setQuantity] = useState(10);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  if (!isOpen || !accessory) return null;

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setErrorMessage("");

    const toAdd = parseInt(quantity, 10);
    if (!toAdd || toAdd <= 0) {
      setErrorMessage("Please enter a valid quantity greater than zero.");
      return;
    }

    try {
      setSubmitting(true);
      await onAddStock(accessory, toAdd);
      onClose();
    } catch (err) {
      setErrorMessage(err.message || "Failed to update stock quantity.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Add Inventory Units"
      subtitle={`Increment stock allocation for ${accessory.accessoryName || accessory.productName}`}
      size="sm"
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
            Add {quantity} Units
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

        <div className="rounded-xl border border-[#DCE5E1] bg-[#F8FAF9] p-3 text-xs text-[#5C7370]">
          <div className="font-bold text-[#03312E]">
            {accessory.accessoryName || accessory.productName}
          </div>
          <div className="mt-0.5">
            Current stock: {accessory.totalQuantity || 0} total ({accessory.availableQuantity || 0} available)
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-[#03312E]">
            Number of units to add <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            min="1"
            max="1000"
            required
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm text-[#03312E] transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
          />
        </div>
      </form>
    </Modal>
  );
}
