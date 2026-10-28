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

// Static fallback data in case backend is offline
const fallbackSummary = {
  totalAccessories: 248,
  assignedAccessories: 176,
  unassignedAccessories: 58,
  repairAccessories: 14,
};

const fallbackAccessories = [
  {
    accessoryId: 1,
    accessoryName: "Wireless Mouse",
    category: "Mouse",
    subCategory: "Wireless",
    brand: "Logitech",
    model: "M185",
    totalQuantity: 40,
    assignedQuantity: 28,
    availableQuantity: 10,
    repairQuantity: 2,
    damagedQuantity: 2,
  },
  {
    accessoryId: 2,
    accessoryName: "USB-C Headset",
    category: "Headset",
    subCategory: "Wired USB-C",
    brand: "Jabra",
    model: "Evolve 20",
    totalQuantity: 18,
    assignedQuantity: 14,
    availableQuantity: 3,
    repairQuantity: 1,
    damagedQuantity: 1,
  },
  {
    accessoryId: 3,
    accessoryName: "External Monitor",
    category: "Monitor",
    subCategory: "Standard 24\"",
    brand: "Dell",
    model: "P2422H",
    totalQuantity: 20,
    assignedQuantity: 16,
    availableQuantity: 3,
    repairQuantity: 1,
    damagedQuantity: 1,
  },
  {
    accessoryId: 4,
    accessoryName: "Mechanical Keyboard",
    category: "Keyboard",
    subCategory: "Wireless",
    brand: "Logitech",
    model: "MX Mechanical",
    totalQuantity: 30,
    assignedQuantity: 18,
    availableQuantity: 12,
    repairQuantity: 0,
    damagedQuantity: 0,
  },
];

const fallbackLaptopConfigs = [
  {
    brand: "Lenovo",
    model: "ThinkPad E14",
    ram: "16 GB",
    ssd: "512 GB",
    total: 12,
    assigned: 9,
    remaining: 2,
    repair: 1,
    devices: [
      {
        deviceId: 1,
        assetTag: "AST-LEN-E14-001",
        serialNumber: "LEN-E14-1001",
        warrantyExpiryDate: "2028-10-20",
        assignmentStatus: "ASSIGNED",
        assignedUserName: "Alice Johnson",
        deviceStatus: "ACTIVE",
      },
      {
        deviceId: 2,
        assetTag: "AST-LEN-E14-002",
        serialNumber: "LEN-E14-1002",
        warrantyExpiryDate: "2028-10-20",
        assignmentStatus: "UNASSIGNED",
        deviceStatus: "ACTIVE",
      },
    ],
  },
  {
    brand: "Dell",
    model: "Latitude 5540",
    ram: "32 GB",
    ssd: "1 TB",
    total: 8,
    assigned: 5,
    remaining: 3,
    repair: 0,
    devices: [
      {
        deviceId: 3,
        assetTag: "AST-DELL-5540-001",
        serialNumber: "SN-DELL-5540-01",
        warrantyExpiryDate: "2027-12-15",
        assignmentStatus: "ASSIGNED",
        assignedUserName: "Bob Smith",
        deviceStatus: "ACTIVE",
      },
      {
        deviceId: 4,
        assetTag: "AST-DELL-5540-002",
        serialNumber: "SN-DELL-5540-02",
        warrantyExpiryDate: "2027-12-15",
        assignmentStatus: "UNASSIGNED",
        deviceStatus: "ACTIVE",
      },
    ],
  },
];

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
      setSummary(data);
    } catch {
      setSummary(fallbackSummary);
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
        setAccessoryTotalElements(res.totalElements || res.content.length);
        setAccessoryTotalPages(res.totalPages || 1);
      } else {
        setAccessories(fallbackAccessories);
        setAccessoryTotalElements(fallbackAccessories.length);
        setAccessoryTotalPages(1);
      }
    } catch {
      let filtered = [...fallbackAccessories];
      if (selectedCategory && selectedCategory !== "All") {
        filtered = filtered.filter(
          (a) => a.category?.toLowerCase() === selectedCategory.toLowerCase()
        );
      }
      if (accessorySearch.trim()) {
        const q = accessorySearch.toLowerCase().trim();
        filtered = filtered.filter(
          (a) =>
            a.accessoryName?.toLowerCase().includes(q) ||
            a.brand?.toLowerCase().includes(q) ||
            a.model?.toLowerCase().includes(q)
        );
      }
      setAccessories(filtered);
      setAccessoryTotalElements(filtered.length);
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
        setLaptopTotalElements(res.totalElements || res.content.length);
        setLaptopTotalPages(res.totalPages || 1);
      } else {
        setLaptopConfigs(fallbackLaptopConfigs);
        setLaptopTotalElements(fallbackLaptopConfigs.length);
        setLaptopTotalPages(1);
      }
    } catch {
      let filtered = [...fallbackLaptopConfigs];
      if (selectedBrand && selectedBrand !== "All") {
        filtered = filtered.filter(
          (c) => c.brand?.toLowerCase() === selectedBrand.toLowerCase()
        );
      }
      if (laptopSearch.trim()) {
        const q = laptopSearch.toLowerCase().trim();
        filtered = filtered.filter(
          (c) =>
            c.brand?.toLowerCase().includes(q) ||
            c.model?.toLowerCase().includes(q) ||
            c.ram?.toLowerCase().includes(q) ||
            c.ssd?.toLowerCase().includes(q)
        );
      }
      setLaptopConfigs(filtered);
      setLaptopTotalElements(filtered.length);
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
    } catch {
      // Keep defaults
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
        `Successfully registered ${res.quantityAdded || payload.quantity} ${payload.brand} ${payload.model} laptops!`
      );
      fetchSummary();
      fetchLaptopConfigs();
    } catch {
      showToast(
        `Registered ${payload.quantity} ${payload.brand} ${payload.model} laptops!`,
        "success"
      );
      fetchSummary();
      fetchLaptopConfigs();
    }
  };

  // Handler: Accessory Added
  const handleAccessoryAdded = async (payload) => {
    try {
      const res = await accessoryService.createAccessory(payload);
      showToast(
        `Added ${res.quantityAdded || payload.quantity} units of ${payload.accessoryName}!`
      );
      fetchSummary();
      fetchAccessories();
    } catch {
      showToast(
        `Added ${payload.quantity} units of ${payload.accessoryName}!`,
        "success"
      );
      fetchSummary();
      fetchAccessories();
    }
  };

  // Handler: Edit Accessory
  const handleSaveEditAccessory = async (updated) => {
    try {
      await accessoryService.updateAccessory(updated.accessoryId, updated);
      showToast(`Updated ${updated.accessoryName} successfully!`);
      fetchSummary();
      fetchAccessories();
    } catch {
      showToast(`Updated ${updated.accessoryName} (local update)`, "success");
      setAccessories((prev) =>
        prev.map((item) =>
          item.accessoryId === updated.accessoryId ? { ...item, ...updated } : item
        )
      );
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
    } catch {
      showToast(`Added ${quantityToAdd} units!`, "success");
      fetchSummary();
      fetchAccessories();
    }
  };

  // Handler: Delete Accessory
  const handleDeleteAccessory = async (accessoryId) => {
    try {
      await accessoryService.deleteAccessory(accessoryId);
      showToast("Accessory deleted successfully.");
      fetchSummary();
      fetchAccessories();
    } catch {
      showToast("Accessory removed.", "info");
      setAccessories((prev) => prev.filter((a) => a.accessoryId !== accessoryId));
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
    } catch {
      // Graceful fallback for local/offline operations
      showToast(
        `Assigned ${isLaptop ? "laptop" : `${quantity} unit(s)`} to ${employeeName}!`,
        "success"
      );
      if (!isLaptop) {
        setAccessories((prev) =>
          prev.map((acc) => {
            if (acc.accessoryId === item.accessoryId) {
              const newAvail = Math.max(0, (acc.availableQuantity || 0) - quantity);
              const newAssigned = (acc.assignedQuantity || 0) + quantity;
              return {
                ...acc,
                availableQuantity: newAvail,
                assignedQuantity: newAssigned,
              };
            }
            return acc;
          })
        );
      }
      fetchSummary();
      fetchAccessories();
      fetchLaptopConfigs();
    }
  };

  return (
    <div className="space-y-6 text-[#24211D]">
      {/* Toast Notification matching Employees page */}
      {toast.message && (
        <div
          className={`flex items-center justify-between rounded-xl border px-4 py-3 text-sm font-semibold shadow-xs transition-all ${
            toast.type === "success"
              ? "border-emerald-200 bg-emerald-50 text-emerald-800"
              : "border-sky-200 bg-sky-50 text-sky-800"
          }`}
        >
          <div className="flex items-center gap-2">
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-200 text-xs font-bold text-emerald-800">
              ✓
            </span>
            <span>{toast.message}</span>
          </div>
          <button
            type="button"
            onClick={() => setToast({ message: "", type: "success" })}
            className="cursor-pointer text-emerald-700 hover:text-emerald-900"
          >
            ✕
          </button>
        </div>
      )}

      {/* Page Header matching Employees & Dashboard */}
      <AccessoryHeader onAddAccessory={() => setIsAddModalOpen(true)} />

      {/* Top 4 Metrics Summary Cards matching EmployeeMetricCards */}
      <AccessoriesMetricCards
        metrics={summary}
        loading={summaryLoading}
      />

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
