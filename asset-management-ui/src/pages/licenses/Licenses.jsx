import React, { useState, useEffect, useCallback } from "react";
import { licenseService } from "../../services/licenseService";
import {
  LicenseHeader,
  LicenseMetricCards,
  LicenseFilters,
  LicenseTable,
  AddEditLicenseModal,
  AssignLicenseModal,
  LicenseDetailModal,
  DeleteLicenseModal,
} from "../../components/licenses";

// Fallback data matching wireframe in case server is starting or offline
const fallbackSummary = {
  totalLicenses: 24,
  totalSeats: 520,
  assignedSeats: 428,
  availableSeats: 92,
  expiringSoonCount: 6,
};

const fallbackLicenses = [
  {
    licenseId: 1,
    licenseName: "Microsoft 365 Business",
    softwareName: "Microsoft 365",
    publisher: "Microsoft",
    licenseType: "SUBSCRIPTION",
    renewalType: "YEARLY",
    renewalMethod: "AUTO",
    totalQuantity: 100,
    assignedQuantity: 85,
    availableQuantity: 15,
    expiryDate: "2026-12-15",
    status: "ACTIVE",
    notes: "Business subscription",
  },
  {
    licenseId: 2,
    licenseName: "IntelliJ IDEA Commercial",
    softwareName: "IntelliJ IDEA",
    publisher: "JetBrains",
    licenseType: "SUBSCRIPTION",
    renewalType: "YEARLY",
    renewalMethod: "AUTO",
    totalQuantity: 50,
    assignedQuantity: 48,
    availableQuantity: 2,
    expiryDate: "2027-01-02",
    status: "ACTIVE",
    notes: "Commercial dev licenses",
  },
  {
    licenseId: 3,
    licenseName: "Windows 11 Pro",
    softwareName: "Windows 11",
    publisher: "Microsoft",
    licenseType: "VOLUME",
    renewalType: "ONE_TIME",
    renewalMethod: "NOT_APPLICABLE",
    totalQuantity: 200,
    assignedQuantity: 180,
    availableQuantity: 20,
    expiryDate: null,
    status: "ACTIVE",
    notes: "Volume licensing",
  },
  {
    licenseId: 4,
    licenseName: "Adobe Acrobat Pro",
    softwareName: "Acrobat Pro",
    publisher: "Adobe",
    licenseType: "SUBSCRIPTION",
    renewalType: "YEARLY",
    renewalMethod: "AUTO",
    totalQuantity: 30,
    assignedQuantity: 25,
    availableQuantity: 5,
    expiryDate: "2026-11-20",
    status: "ACTIVE",
    notes: "PDF & document editor",
  },
  {
    licenseId: 5,
    licenseName: "Visual Studio Enterprise",
    softwareName: "Visual Studio",
    publisher: "Microsoft",
    licenseType: "SUBSCRIPTION",
    renewalType: "YEARLY",
    renewalMethod: "AUTO",
    totalQuantity: 40,
    assignedQuantity: 38,
    availableQuantity: 2,
    expiryDate: "2026-12-10",
    status: "EXPIRING_SOON",
    notes: "Enterprise developer tooling",
  },
  {
    licenseId: 6,
    licenseName: "AutoCAD",
    softwareName: "AutoCAD",
    publisher: "Autodesk",
    licenseType: "PERPETUAL",
    renewalType: "ONE_TIME",
    renewalMethod: "NOT_APPLICABLE",
    totalQuantity: 25,
    assignedQuantity: 20,
    availableQuantity: 5,
    expiryDate: null,
    status: "ACTIVE",
    notes: "Drafting workstation software",
  },
];

export default function Licenses() {
  // 1. Summary Metrics
  const [summary, setSummary] = useState(null);
  const [summaryLoading, setSummaryLoading] = useState(true);

  // 2. Table & Filter State
  const [licenses, setLicenses] = useState([]);
  const [licensesLoading, setLicensesLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [searchQuery, setSearchQuery] = useState("");
  const [selectedSoftware, setSelectedSoftware] = useState("All");
  const [selectedPublisher, setSelectedPublisher] = useState("All");
  const [selectedStatus, setSelectedStatus] = useState("All");
  const [selectedExpiry, setSelectedExpiry] = useState("All");

  const [softwareOptions, setSoftwareOptions] = useState([]);
  const [publisherOptions, setPublisherOptions] = useState([]);

  // 3. Modals
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [editingLicense, setEditingLicense] = useState(null);
  const [assigningLicense, setAssigningLicense] = useState(null);
  const [viewingLicenseId, setViewingLicenseId] = useState(null);
  const [deletingLicense, setDeletingLicense] = useState(null);

  // 4. Toast notification
  const [toast, setToast] = useState({ message: "", type: "success" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => {
      setToast({ message: "", type: "success" });
    }, 4500);
  };

  // Fetch summary
  const fetchSummary = useCallback(async () => {
    try {
      setSummaryLoading(true);
      const data = await licenseService.getSummary();
      setSummary(data);
    } catch {
      setSummary(fallbackSummary);
    } finally {
      setSummaryLoading(false);
    }
  }, []);

  // Fetch dropdown filter options
  const fetchFilterOptions = useCallback(async () => {
    try {
      const data = await licenseService.getFilterOptions();
      if (data) {
        if (Array.isArray(data.softwares) && data.softwares.length > 0) {
          setSoftwareOptions(data.softwares);
        } else {
          setSoftwareOptions(["Microsoft 365", "IntelliJ IDEA", "Windows 11", "Acrobat Pro", "Visual Studio", "AutoCAD"]);
        }
        if (Array.isArray(data.publishers) && data.publishers.length > 0) {
          setPublisherOptions(data.publishers);
        } else {
          setPublisherOptions(["Microsoft", "JetBrains", "Adobe", "Autodesk"]);
        }
      }
    } catch {
      setSoftwareOptions(["Microsoft 365", "IntelliJ IDEA", "Windows 11", "Acrobat Pro", "Visual Studio", "AutoCAD"]);
      setPublisherOptions(["Microsoft", "JetBrains", "Adobe", "Autodesk"]);
    }
  }, []);

  // Fetch licenses page
  const fetchLicenses = useCallback(async () => {
    try {
      setLicensesLoading(true);
      const res = await licenseService.getLicenses({
        page,
        size: pageSize,
        search: searchQuery,
        software: selectedSoftware,
        publisher: selectedPublisher,
        status: selectedStatus,
        expiry: selectedExpiry,
      });

      if (res && Array.isArray(res.content)) {
        setLicenses(res.content);
        setTotalElements(res.totalElements ?? res.content.length);
        setTotalPages(res.totalPages ?? 1);
      } else {
        setLicenses(fallbackLicenses);
        setTotalElements(fallbackLicenses.length);
        setTotalPages(1);
      }
    } catch {
      // Local client filtering fallback
      let filtered = [...fallbackLicenses];

      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim();
        filtered = filtered.filter(
          (l) =>
            l.licenseName?.toLowerCase().includes(q) ||
            l.softwareName?.toLowerCase().includes(q) ||
            l.publisher?.toLowerCase().includes(q)
        );
      }

      if (selectedSoftware !== "All") {
        filtered = filtered.filter(
          (l) => l.softwareName?.toLowerCase() === selectedSoftware.toLowerCase()
        );
      }

      if (selectedPublisher !== "All") {
        filtered = filtered.filter(
          (l) => l.publisher?.toLowerCase() === selectedPublisher.toLowerCase()
        );
      }

      if (selectedStatus !== "All") {
        filtered = filtered.filter(
          (l) => l.status?.toUpperCase() === selectedStatus.toUpperCase()
        );
      }

      setLicenses(filtered);
      setTotalElements(filtered.length);
      setTotalPages(Math.ceil(filtered.length / pageSize) || 1);
    } finally {
      setLicensesLoading(false);
    }
  }, [page, pageSize, searchQuery, selectedSoftware, selectedPublisher, selectedStatus, selectedExpiry]);

  // Initial mount
  useEffect(() => {
    fetchSummary();
    fetchFilterOptions();
  }, [fetchSummary, fetchFilterOptions]);

  useEffect(() => {
    fetchLicenses();
  }, [fetchLicenses]);

  // Reset Filters
  const handleResetFilters = () => {
    setSearchQuery("");
    setSelectedSoftware("All");
    setSelectedPublisher("All");
    setSelectedStatus("All");
    setSelectedExpiry("All");
    setPage(0);
  };

  // Create / Update License
  const handleSaveLicense = async (payload, licenseId) => {
    try {
      if (licenseId) {
        await licenseService.updateLicense(licenseId, payload);
        showToast(`Successfully updated license: ${payload.licenseName}`);
      } else {
        await licenseService.createLicense(payload);
        showToast(`Successfully created new license: ${payload.licenseName}`);
      }
      fetchSummary();
      fetchLicenses();
      fetchFilterOptions();
    } catch (err) {
      showToast(err.message || "Failed to save license.", "error");
      throw err;
    }
  };

  // Assign License
  const handleAssignLicense = async (licenseId, payload, employeeName) => {
    try {
      await licenseService.assignLicense(licenseId, payload);
      showToast(`Successfully assigned license seat to ${employeeName}!`);
      fetchSummary();
      fetchLicenses();
    } catch (err) {
      showToast(err.message || "Failed to assign license.", "error");
      throw err;
    }
  };

  // Delete License
  const handleDeleteLicense = async (licenseId) => {
    try {
      await licenseService.deleteLicense(licenseId);
      showToast("License deleted successfully.");
      fetchSummary();
      fetchLicenses();
      fetchFilterOptions();
    } catch (err) {
      showToast(err.message || "Failed to delete license.", "error");
      throw err;
    }
  };

  // Released seat callback
  const handleReleaseSuccess = () => {
    showToast("License seat released successfully.");
    fetchSummary();
    fetchLicenses();
  };

  return (
    <div className="space-y-6 text-[#24211D]">
      {/* Toast Notification */}
      {toast.message && (
        <div
          className={`flex items-center justify-between rounded-xl border px-4 py-3 text-sm font-semibold shadow-xs transition-all ${
            toast.type === "error"
              ? "border-rose-200 bg-rose-50 text-rose-800"
              : "border-emerald-200 bg-emerald-50 text-emerald-800"
          }`}
        >
          <div className="flex items-center gap-2">
            <span
              className={`flex h-5 w-5 items-center justify-center rounded-full text-xs font-bold ${
                toast.type === "error"
                  ? "bg-rose-200 text-rose-800"
                  : "bg-emerald-200 text-emerald-800"
              }`}
            >
              {toast.type === "error" ? "✕" : "✓"}
            </span>
            <span>{toast.message}</span>
          </div>
          <button
            type="button"
            onClick={() => setToast({ message: "", type: "success" })}
            className="cursor-pointer text-[#64748B] hover:text-[#03312E]"
          >
            ✕
          </button>
        </div>
      )}

      {/* Page Header */}
      <LicenseHeader onAddLicense={() => setIsAddModalOpen(true)} />

      {/* 5 Summary Metric Cards */}
      <LicenseMetricCards metrics={summary} loading={summaryLoading} />

      {/* Filters Bar */}
      <LicenseFilters
        searchQuery={searchQuery}
        onSearchChange={(q) => {
          setSearchQuery(q);
          setPage(0);
        }}
        selectedSoftware={selectedSoftware}
        onSoftwareChange={(s) => {
          setSelectedSoftware(s);
          setPage(0);
        }}
        selectedPublisher={selectedPublisher}
        onPublisherChange={(p) => {
          setSelectedPublisher(p);
          setPage(0);
        }}
        selectedStatus={selectedStatus}
        onStatusChange={(st) => {
          setSelectedStatus(st);
          setPage(0);
        }}
        selectedExpiry={selectedExpiry}
        onExpiryChange={(exp) => {
          setSelectedExpiry(exp);
          setPage(0);
        }}
        softwareOptions={softwareOptions}
        publisherOptions={publisherOptions}
        onReset={handleResetFilters}
      />

      {/* License Inventory Table */}
      <LicenseTable
        licenses={licenses}
        loading={licensesLoading}
        totalElements={totalElements}
        currentPage={page}
        pageSize={pageSize}
        totalPages={totalPages}
        onPageChange={setPage}
        onViewLicense={(lic) => setViewingLicenseId(lic.licenseId)}
        onEditLicense={(lic) => setEditingLicense(lic)}
        onAssignLicense={(lic) => setAssigningLicense(lic)}
        onDeleteLicense={(lic) => setDeletingLicense(lic)}
      />

      {/* Add / Edit License Modal */}
      <AddEditLicenseModal
        isOpen={isAddModalOpen || !!editingLicense}
        onClose={() => {
          setIsAddModalOpen(false);
          setEditingLicense(null);
        }}
        license={editingLicense}
        onSave={handleSaveLicense}
      />

      {/* Assign License Modal */}
      <AssignLicenseModal
        isOpen={!!assigningLicense}
        onClose={() => setAssigningLicense(null)}
        license={assigningLicense}
        onAssign={handleAssignLicense}
      />

      {/* View License Details & Assignments History Modal */}
      <LicenseDetailModal
        isOpen={!!viewingLicenseId}
        onClose={() => setViewingLicenseId(null)}
        licenseId={viewingLicenseId}
        onReleaseSuccess={handleReleaseSuccess}
      />

      {/* Delete Confirmation Modal */}
      <DeleteLicenseModal
        isOpen={!!deletingLicense}
        onClose={() => setDeletingLicense(null)}
        license={deletingLicense}
        onConfirmDelete={handleDeleteLicense}
      />
    </div>
  );
}
