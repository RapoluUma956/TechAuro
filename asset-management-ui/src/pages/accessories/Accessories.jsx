import React, { useState, useEffect, useCallback } from "react";
import { accessoryService } from "../../services/accessoryService";
import {
  AccessoryHeader,
  AccessoriesMetricCards,
  AccessoryTable,
  LaptopTable,
  AddAccessoryModal,
  EditAccessoryModal,
  QuickAddStockModal,
  AssignAccessoryModal,
} from "../../components/accessories";

export default function Accessories() {
  // Summary state
  const [summary, setSummary] = useState(null);
  const [summaryLoading, setSummaryLoading] = useState(true);

  // Accessories table state
  const [accessories, setAccessories] = useState([]);
  const [accessoriesLoading, setAccessoriesLoading] = useState(true);
  const [accessorySearch, setAccessorySearch] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [accessoryPage, setAccessoryPage] = useState(0);
  const [accessoryTotalElements, setAccessoryTotalElements] = useState(0);
  const [accessoryTotalPages, setAccessoryTotalPages] = useState(1);
  const ACCESSORY_PAGE_SIZE = 10;

  // Laptop table state
  const [laptopConfigs, setLaptopConfigs] = useState([]);
  const [laptopsLoading, setLaptopsLoading] = useState(true);
  const [laptopSearch, setLaptopSearch] = useState("");
  const [selectedBrand, setSelectedBrand] = useState("All");
  const [laptopPage, setLaptopPage] = useState(0);
  const [laptopTotalElements, setLaptopTotalElements] = useState(0);
  const [laptopTotalPages, setLaptopTotalPages] = useState(1);
  const LAPTOP_PAGE_SIZE = 10;

  // Category dropdown data
  const [categories, setCategories] = useState([]);

  // Modals
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [editingAccessory, setEditingAccessory] = useState(null);
  const [quickAddAccessory, setQuickAddAccessory] = useState(null);
  const [assigningItem, setAssigningItem] = useState(null);
  const [isAssigningLaptop, setIsAssigningLaptop] = useState(false);

  // Toast notification
  const [toast, setToast] = useState({ message: "", type: "success" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => {
      setToast({ message: "", type: "success" });
    }, 4500);
  };

  // 1. Fetch Metrics Summary
  const fetchSummary = useCallback(async () => {
    try {
      setSummaryLoading(true);
      const data = await accessoryService.getSummary();
      setSummary(
        data || {
          totalAccessories: 0,
          assignedAccessories: 0,
          unassignedAccessories: 0,
          repairAccessories: 0,
        }
      );
    } catch (err) {
      console.error("Failed to load accessory summary:", err);
      setSummary({
        totalAccessories: 0,
        assignedAccessories: 0,
        unassignedAccessories: 0,
        repairAccessories: 0,
      });
    } finally {
      setSummaryLoading(false);
    }
  }, []);

  // 2. Fetch Accessories Table
  const fetchAccessories = useCallback(async () => {
    try {
      setAccessoriesLoading(true);
      const res = await accessoryService.getAccessories({
        page: accessoryPage,
        size: ACCESSORY_PAGE_SIZE,
        search: accessorySearch,
        category: selectedCategory,
      });

      if (res && res.content) {
        setAccessories(res.content);
        setAccessoryTotalElements(res.totalElements ?? res.content.length);
        setAccessoryTotalPages(res.totalPages ?? 1);
      } else {
        setAccessories([]);
        setAccessoryTotalElements(0);
        setAccessoryTotalPages(1);
      }
    } catch (err) {
      console.error("Failed to fetch accessories:", err);
      setAccessories([]);
      setAccessoryTotalElements(0);
      setAccessoryTotalPages(1);
    } finally {
      setAccessoriesLoading(false);
    }
  }, [accessoryPage, accessorySearch, selectedCategory]);

  // 3. Fetch Laptop Configurations Table
  const fetchLaptopConfigs = useCallback(async () => {
    try {
      setLaptopsLoading(true);
      const res = await accessoryService.getLaptopConfigurations({
        page: laptopPage,
        size: LAPTOP_PAGE_SIZE,
        search: laptopSearch,
        brand: selectedBrand,
      });

      if (res && res.content) {
        setLaptopConfigs(res.content);
        setLaptopTotalElements(res.totalElements ?? res.content.length);
        setLaptopTotalPages(res.totalPages ?? 1);
      } else {
        setLaptopConfigs([]);
        setLaptopTotalElements(0);
        setLaptopTotalPages(1);
      }
    } catch (err) {
      console.error("Failed to fetch laptop configs:", err);
      setLaptopConfigs([]);
      setLaptopTotalElements(0);
      setLaptopTotalPages(1);
    } finally {
      setLaptopsLoading(false);
    }
  }, [laptopPage, laptopSearch, selectedBrand]);

  // 4. Fetch Categories
  const fetchCategories = useCallback(async () => {
    try {
      const data = await accessoryService.getCategories();
      if (Array.isArray(data)) {
        setCategories(data);
      }
    } catch (err) {
      console.error("Failed to fetch categories:", err);
    }
  }, []);

  // Initial load
  useEffect(() => {
    fetchSummary();
    fetchCategories();
  }, [fetchSummary, fetchCategories]);

  useEffect(() => {
    fetchAccessories();
  }, [fetchAccessories]);

  useEffect(() => {
    fetchLaptopConfigs();
  }, [fetchLaptopConfigs]);

  // Handler: Laptop Added
  const handleLaptopAdded = async (payload) => {
    try {
      const res = await accessoryService.bulkAddLaptops(payload);
      showToast(
        `Successfully registered ${res?.quantityAdded || payload.quantity} ${payload.brand} ${payload.model} laptops!`
      );
      fetchSummary();
      fetchLaptopConfigs();
    } catch (err) {
      showToast(err?.message || "Failed to add laptops", "error");
    }
  };

  // Handler: Accessory Added
  const handleAccessoryAdded = async (payload) => {
    try {
      const res = await accessoryService.createAccessory(payload);
      showToast(
        `Added ${res?.quantityAdded || payload.quantity} units of ${payload.accessoryName}!`
      );
      fetchSummary();
      fetchAccessories();
    } catch (err) {
      showToast(err?.message || "Failed to add accessory", "error");
    }
  };

  // Handler: Edit Accessory
  const handleSaveEditAccessory = async (updated) => {
    try {
      await accessoryService.updateAccessory(updated.accessoryId, updated);
      showToast(`Updated ${updated.accessoryName} successfully!`);
      fetchSummary();
      fetchAccessories();
    } catch (err) {
      showToast(err?.message || "Failed to update accessory", "error");
    }
  };

  // Handler: Quick Add Stock
  const handleQuickAddStock = async (accessory, quantityToAdd) => {
    try {
      const updatedTotal = (accessory.totalQuantity || 0) + quantityToAdd;
      const updatedAvail = (accessory.availableQuantity || 0) + quantityToAdd;
      await accessoryService.updateAccessory(accessory.accessoryId, {
        ...accessory,
        totalQuantity: updatedTotal,
        availableQuantity: updatedAvail,
      });
      showToast(
        `Added ${quantityToAdd} units to ${accessory.accessoryName || accessory.productName}! New total: ${updatedTotal}`
      );
      fetchSummary();
      fetchAccessories();
    } catch (err) {
      showToast(err?.message || "Failed to add stock", "error");
    }
  };

  // Handler: Delete Accessory
  const handleDeleteAccessory = async (accessoryId) => {
    try {
      await accessoryService.deleteAccessory(accessoryId);
      showToast("Accessory deleted successfully.");
      fetchSummary();
      fetchAccessories();
    } catch (err) {
      showToast(err?.message || "Failed to delete accessory", "error");
    }
  };

  // Handler: Assign Asset (Accessory or Laptop)
  const handleAssign = async ({
    item,
    userId,
    employeeName,
    assignedDate,
    quantity,
    notes,
    isLaptop,
  }) => {
    try {
      if (isLaptop) {
        await accessoryService.assignLaptop(item.deviceId, {
          userId,
          assignedDate,
          notes,
        });
        showToast(
          `Successfully assigned ${item.brand || ""} ${item.model || "Laptop"} to ${employeeName}!`
        );
        fetchSummary();
        fetchLaptopConfigs();
      } else {
        await accessoryService.assignAccessory(item.accessoryId, {
          userId,
          assignedDate,
          quantity,
          notes,
        });
        showToast(
          `Assigned ${quantity} unit(s) of ${item.accessoryName || item.productName} to ${employeeName}!`
        );
        fetchSummary();
        fetchAccessories();
      }
    } catch (err) {
      showToast(err?.message || "Failed to assign asset", "error");
    }
  };

  return (
    <div className="space-y-6 text-[#24211D]">
      {/* Toast Notification */}
      {toast.message && (
        <div
          className={`flex items-center justify-between rounded-xl border px-4 py-3 text-sm font-semibold shadow-xs transition-all ${
            toast.type === "success"
              ? "border-emerald-200 bg-emerald-50 text-emerald-800"
              : "border-red-200 bg-red-50 text-red-800"
          }`}
        >
          <div className="flex items-center gap-2">
            <span
              className={`flex h-5 w-5 items-center justify-center rounded-full text-xs font-bold ${
                toast.type === "success"
                  ? "bg-emerald-200 text-emerald-800"
                  : "bg-red-200 text-red-800"
              }`}
            >
              {toast.type === "success" ? "✓" : "!"}
            </span>
            <span>{toast.message}</span>
          </div>
          <button
            type="button"
            onClick={() => setToast({ message: "", type: "success" })}
            className="cursor-pointer text-gray-500 hover:text-gray-700"
          >
            ✕
          </button>
        </div>
      )}

      {/* Page Header */}
      <AccessoryHeader onAddAccessory={() => setIsAddModalOpen(true)} />

      {/* Top Metrics Summary Cards */}
      <AccessoriesMetricCards metrics={summary} loading={summaryLoading} />

      {/* Both Tables Stacked Directly on the Page */}
      <AccessoryTable
        accessories={accessories}
        loading={accessoriesLoading}
        totalElements={accessoryTotalElements}
        currentPage={accessoryPage}
        pageSize={ACCESSORY_PAGE_SIZE}
        totalPages={accessoryTotalPages}
        onPageChange={setAccessoryPage}
        searchQuery={accessorySearch}
        onSearchChange={(q) => {
          setAccessorySearch(q);
          setAccessoryPage(0);
        }}
        selectedCategory={selectedCategory}
        onCategoryChange={(cat) => {
          setSelectedCategory(cat);
          setAccessoryPage(0);
        }}
        categories={categories}
        onEditAccessory={(acc) => setEditingAccessory(acc)}
        onDeleteAccessory={handleDeleteAccessory}
        onQuickAddQuantity={(acc) => setQuickAddAccessory(acc)}
        onAssignAccessory={(acc) => {
          setAssigningItem(acc);
          setIsAssigningLaptop(false);
        }}
      />

      <LaptopTable
        laptopConfigurations={laptopConfigs}
        loading={laptopsLoading}
        totalElements={laptopTotalElements}
        currentPage={laptopPage}
        pageSize={LAPTOP_PAGE_SIZE}
        totalPages={laptopTotalPages}
        onPageChange={setLaptopPage}
        searchQuery={laptopSearch}
        onSearchChange={(q) => {
          setLaptopSearch(q);
          setLaptopPage(0);
        }}
        selectedBrand={selectedBrand}
        onBrandChange={(b) => {
          setSelectedBrand(b);
          setLaptopPage(0);
        }}
        onRefresh={fetchLaptopConfigs}
        onAssignLaptop={(dev) => {
          setAssigningItem(dev);
          setIsAssigningLaptop(true);
        }}
      />

      {/* Add Accessory Modal */}
      <AddAccessoryModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onLaptopAdded={handleLaptopAdded}
        onAccessoryAdded={handleAccessoryAdded}
        categories={categories}
      />

      {/* Edit Accessory Modal */}
      <EditAccessoryModal
        isOpen={!!editingAccessory}
        onClose={() => setEditingAccessory(null)}
        accessory={editingAccessory}
        onSave={handleSaveEditAccessory}
      />

      {/* Quick Add Stock Modal */}
      <QuickAddStockModal
        isOpen={!!quickAddAccessory}
        onClose={() => setQuickAddAccessory(null)}
        accessory={quickAddAccessory}
        onAddStock={handleQuickAddStock}
      />

      {/* Assign Accessory / Laptop Modal */}
      <AssignAccessoryModal
        isOpen={!!assigningItem}
        onClose={() => setAssigningItem(null)}
        item={assigningItem}
        isLaptop={isAssigningLaptop}
        onAssign={handleAssign}
      />
    </div>
  );
}
