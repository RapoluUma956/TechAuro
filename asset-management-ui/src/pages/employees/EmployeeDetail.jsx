import React, { useState, useEffect, useMemo, useCallback } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import { employeeService } from "../../services/employeeService";
import {
  EmployeeDetailHeader,
  EmployeeDetailTabs,
  DeviceDetailsTab,
  AccessoriesTab,
  InstalledSoftwareTab,
  VulnerabilitiesTab,
  LicensesTab,
} from "../../components/employees/detail";
import { Modal, Button } from "../../components/common";
import useAutoRefresh from "../../hooks/useAutoRefresh";

function formatUptime(seconds) {
  if (!seconds && seconds !== 0) return "—";
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const mins = Math.floor((seconds % 3600) / 60);
  if (days > 0) return `${days}d ${hours}h ${mins}m`;
  if (hours > 0) return `${hours}h ${mins}m`;
  return `${mins}m`;
}

export default function EmployeeDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Tab state synced with search param ?tab=
  const initialTab = searchParams.get("tab") || "device";
  const [activeTab, setActiveTab] = useState(initialTab);

  // Employee profile & summary state
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Tab-specific data states
  const [deviceData, setDeviceData] = useState(null);
  const [accessoriesData, setAccessoriesData] = useState([]);
  const [softwareData, setSoftwareData] = useState([]);
  const [vulnerabilitiesData, setVulnerabilitiesData] = useState([]);
  const [licensesData, setLicensesData] = useState([]);
  const [tabLoading, setTabLoading] = useState(false);

  // Device assignment / reassignment modal state
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [assignSerialNumber, setAssignSerialNumber] = useState("");
  const [reassignReason, setReassignReason] = useState("");
  const [availableDevices, setAvailableDevices] = useState([]);
  const [assignLoading, setAssignLoading] = useState(false);
  const [assignError, setAssignError] = useState("");
  const [toastMessage, setToastMessage] = useState("");

  // 1. Fetch Employee Profile
  const fetchProfile = useCallback(async (isBackground = false) => {
    try {
      if (!isBackground) {
        setLoading(true);
        setError("");
      }
      const res = await employeeService.getEmployeeById(id);
      if (res && (res.employeeId || res.employeeCode)) {
        setProfile(res);
      } else {
        if (!isBackground) {
          setError(`Employee profile with identifier "${id}" could not be found.`);
        }
      }
    } catch (err) {
      console.error("Failed to load employee profile:", err);
      if (!isBackground) {
        setError(err?.message || "Failed to load employee details.");
      }
    } finally {
      if (!isBackground) {
        setLoading(false);
      }
    }
  }, [id]);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  // 2. Fetch Active Tab Data
  const fetchActiveTabData = useCallback(async (isBackground = false) => {
    if (!profile) return;
    if (!isBackground) {
      setTabLoading(true);
    }

    try {
      const empId = profile.employeeId || id;

      if (activeTab === "device") {
        const devRes = await employeeService.getEmployeeDevice(empId);
        const dev = devRes?.device;
        if (dev) {
          let historyRes = null;
          if (dev.deviceId) {
            try {
              historyRes = await employeeService.getDeviceHistory(dev.deviceId);
            } catch (hErr) {
              console.warn("Could not fetch device history:", hErr);
            }
          }

          const isOnline = dev.connectionStatus === "ONLINE";
          const hasTelemetry = Boolean(dev.cpu?.processorModel || dev.memory?.totalGB || dev.lastSeenAt);

          const mappedDevice = {
            deviceId: dev.serialNumber || `DEV-${dev.deviceId}`,
            deviceName:
              dev.deviceName ||
              `${dev.manufacturer || ""} ${dev.model || ""}`.trim() ||
              "Assigned Laptop",
            assetTag: dev.assetTag || `AST-${dev.deviceId || 1}`,
            serialNumber: dev.serialNumber,
            os: dev.operatingSystem?.name
              ? `${dev.operatingSystem.name} ${dev.operatingSystem.version || ""}`.trim()
              : (dev.operatingSystem || "—"),
            status: isOnline ? "Active" : (dev.connectionStatus || "Offline"),
            warrantyDate: dev.warrantyExpiryDate,
            purchaseDate: dev.purchaseDate,
            mdmStatus: dev.agent?.version
              ? `Enrolled (${dev.agent.version})`
              : (isOnline ? "Enrolled" : "Awaiting Agent"),
            encryptionStatus: dev.security?.bitLockerProtection === "ON"
              ? "BitLocker Encrypted"
              : (isOnline ? "Standard" : "—"),
            lastSeen: dev.lastSeenAt,
            brand: dev.manufacturer || "—",
            model: dev.model || "—",
          };

          const mappedRawDb = {
            device_id: dev.deviceId,
            device_name: mappedDevice.deviceName,
            asset_id: dev.assetTag || `AST-${dev.deviceId || 1}`,
            brand: dev.manufacturer || "—",
            model: dev.model || "—",
            serial_number: dev.serialNumber,
            connection_status: dev.connectionStatus || "OFFLINE",
            asset_status: dev.deviceStatus || "ACTIVE",
            cpu: dev.cpu?.processorModel || null,
            cpu_vendor: dev.cpu?.vendor || null,
            cpu_cores: dev.cpu?.cores ?? null,
            cpu_threads: dev.cpu?.threads ?? null,
            cpu_frequency_mhz: dev.cpu?.frequencyMHz ?? null,
            cpu_usage_percent: dev.cpu?.utilizationPercent ?? null,
            cpu_temperature: dev.cpu?.temperatureCelsius ?? null,
            ram_gb: dev.memory?.totalGB ?? null,
            ram_used_gb: dev.memory?.usedGB ?? null,
            ram_available_gb: dev.memory?.availableGB ?? null,
            ram_usage_percent: dev.memory?.utilizationPercent ?? null,
            ram_type: dev.memory?.type || null,
            ram_speed_mhz: dev.memory?.speedMHz ?? null,
            ssd_capacity_gb: dev.storage?.totalGB ?? null,
            ssd_used_gb: dev.storage?.usedGB ?? null,
            ssd_available_gb: dev.storage?.availableGB ?? null,
            ssd_model: dev.storage?.model || null,
            ssd_type: dev.storage?.type || null,
            ssd_health: dev.storage?.healthStatus || null,
            battery_percentage: dev.battery?.percentage ?? null,
            battery_charging: dev.battery?.charging ?? null,
            battery_health_percent: dev.battery?.healthPercent ?? null,
            battery_cycle_count: dev.battery?.cycleCount ?? null,
            power_source: dev.battery?.powerSource || null,
            os_name: dev.operatingSystem?.name || null,
            os_version: dev.operatingSystem?.version || null,
            os_architecture: dev.operatingSystem?.architecture || null,
            hostname: dev.hostname || null,
            ipv4_address: dev.networkAdapters?.[0]?.ipv4Address || null,
            bitlocker_enabled: dev.security?.bitLockerProtection === "ON",
            bitlocker_protection_status: dev.security?.bitLockerProtection || null,
            bitlocker_encryption_method: dev.security?.encryptionMethod || null,
            bitlocker_volume: dev.security?.volume || null,
            tpm_present: Boolean(dev.security?.tpmVersion),
            tpm_version: dev.security?.tpmVersion || null,
            secure_boot_enabled: dev.security?.secureBootEnabled ?? null,
            bios_manufacturer: dev.manufacturer || null,
            bios_version: dev.biosVersion || null,
            uptime_seconds: dev.agent?.uptimeSeconds ?? null,
            process_count: dev.processCount ?? null,
            thread_count: dev.threadCount ?? null,
            warranty_expiry: dev.warrantyExpiryDate,
            purchase_date: dev.purchaseDate,
            last_seen_at: dev.lastSeenAt,
            agent_installed: Boolean(dev.agent?.version || isOnline),
            agent_version: dev.agent?.version || null,
            has_telemetry: hasTelemetry,
          };

          const mappedAdapters = (dev.networkAdapters || []).map((net, idx) => ({
            id: net.networkId || idx + 1,
            interface: net.name || net.interfaceName || `Adapter ${idx + 1}`,
            name: net.name || net.interfaceName || "Network Controller",
            macAddress: net.macAddress || "—",
            ipv4: net.ipv4Address || "—",
            ipv6: net.ipv6Address || "N/A",
            status:
              net.status === "CONNECTED" || net.connectionStatus === "UP"
                ? "Connected"
                : net.status || "Disconnected",
            type: net.interfaceType || "Physical",
          }));

          const mappedHistory = {
            ownership: (historyRes?.history || []).map((h) => ({
              id: h.historyId,
              name: h.employeeName,
              employeeId: h.employeeCode,
              department: h.reason || "Hardware Assignment",
              fromDate: h.assignedAt ? h.assignedAt.split("T")[0] : "—",
              toDate: h.unassignedAt
                ? h.unassignedAt.split("T")[0]
                : h.status === "ACTIVE"
                ? "Present"
                : "—",
              reasonForChange: h.reason || "Device Assignment",
            })),
            maintenance: [],
          };

          setDeviceData({
            device: mappedDevice,
            rawDbDevice: mappedRawDb,
            networkAdapters: mappedAdapters,
            deviceHistory: mappedHistory,
          });
        } else {
          setDeviceData(null);
        }
      } else if (activeTab === "accessories") {
        const accRes = await employeeService.getEmployeeAccessories(empId);
        const list = accRes?.accessories || accRes?.content || [];
        const mapped = list.map((acc) => ({
          id: acc.accessoryId,
          accessoryName: acc.accessoryName || acc.name || acc.productName || "Accessory",
          accessoryType: acc.accessoryType || acc.type || acc.category || "General",
          serialNumber: acc.serialNumber || "—",
          assetTag: acc.assetTag || `ACC-${acc.accessoryId}`,
          assignedDate: acc.assignedDate || "—",
          status: acc.status || "Assigned",
        }));
        setAccessoriesData(mapped);
      } else if (activeTab === "software") {
        const swRes = await employeeService.getEmployeeSoftware(empId, { size: 1000 });
        const list = swRes?.software || swRes?.content || [];
        const mapped = list.map((sw) => ({
          id: sw.softwareId,
          softwareName: sw.softwareName || sw.name,
          publisher: sw.publisher || "—",
          version: sw.version || "—",
          installDate: sw.installDate || "—",
          installPath: sw.installPath || sw.installLocation || "—",
          size: sw.sizeMb ? `${sw.sizeMb} MB` : "",
          status: sw.status || sw.updateStatus || "Installed",
        }));
        setSoftwareData(mapped);
      } else if (activeTab === "vulnerabilities") {
        const vulnRes = await employeeService.getEmployeeVulnerabilities(empId);
        const list = vulnRes?.vulnerabilities || vulnRes?.content || [];
        const mapped = list.map((v) => ({
          id: v.vulnerabilityId,
          cveId: v.cveId,
          severity: v.severity,
          cvssScore: v.cvssScore,
          softwareName: v.affectedSoftware || v.softwareName,
          description: v.description || v.title || "—",
          detectedDate: v.detectedDate || "—",
          remediationStatus: v.remediationStatus || v.status || "In Review",
        }));
        setVulnerabilitiesData(mapped);
      } else if (activeTab === "licences") {
        const licRes = await employeeService.getEmployeeLicenses(empId);
        const list = licRes?.licenses || licRes?.content || [];
        const mapped = list.map((lic) => ({
          id: lic.licenseId,
          softwareName: lic.softwareName || lic.licenseName || lic.name,
          vendor: lic.vendor || lic.publisher || "—",
          licenseKey: lic.licenseKey || "—",
          licenseType: lic.licenseType || "Standard",
          assignedDate: lic.assignedDate || "—",
          expiryDate: lic.expiryDate || "—",
          status: lic.status || "Active",
        }));
        setLicensesData(mapped);
      }
    } catch (err) {
      console.error(`Failed to load data for tab ${activeTab}:`, err);
    } finally {
      if (!isBackground) {
        setTabLoading(false);
      }
    }
  }, [profile, id, activeTab]);

  useEffect(() => {
    fetchActiveTabData();
  }, [fetchActiveTabData]);

  // 30-Second Active-Tab Auto-Refresh for live telemetry and hardware metrics
  useAutoRefresh(
    useCallback(
      (isBackground) => {
        fetchProfile(isBackground);
        fetchActiveTabData(isBackground);
      },
      [fetchProfile, fetchActiveTabData]
    ),
    30000
  );

  // Open Assign / Reassign Device modal
  const handleOpenAssignModal = async () => {
    setAssignError("");
    setAssignSerialNumber("");
    setReassignReason("");
    setIsAssignModalOpen(true);
    try {
      const res = await employeeService.getAvailableDevices();
      const list = res?.devices || (Array.isArray(res) ? res : []);
      setAvailableDevices(list);
      if (list.length > 0) {
        setAssignSerialNumber(list[0].serialNumber || "");
      }
    } catch {
      setAvailableDevices([]);
    }
  };

  // Submit device reassignment / assignment
  const handleAssignSubmit = async (e) => {
    if (e) e.preventDefault();
    if (!assignSerialNumber.trim()) {
      setAssignError("Please enter or select a Serial Number.");
      return;
    }

    try {
      setAssignLoading(true);
      setAssignError("");
      const empId = profile?.employeeId || id;
      await employeeService.reassignEmployeeDevice(empId, {
        serialNumber: assignSerialNumber.trim(),
        reason: reassignReason.trim() || "Assigned by IT administrator",
      });
      setIsAssignModalOpen(false);
      setToastMessage(
        `Hardware device (${assignSerialNumber.trim()}) assigned successfully!`
      );
      await fetchProfile();
      await fetchActiveTabData();
    } catch (err) {
      console.error("Assignment failed:", err);
      setAssignError(
        err?.message ||
          "Failed to assign device. The device may already be assigned or inactive."
      );
    } finally {
      setAssignLoading(false);
      setTimeout(() => setToastMessage(""), 5000);
    }
  };

  // Unassign device
  const handleUnassignDevice = async () => {
    if (
      !window.confirm(
        "Are you sure you want to unassign this hardware device from this employee?"
      )
    ) {
      return;
    }

    try {
      setLoading(true);
      const empId = profile?.employeeId || id;
      await employeeService.unassignEmployeeDevice(empId);
      setToastMessage("Device unassigned successfully.");
      await fetchProfile();
      await fetchActiveTabData();
    } catch (err) {
      console.error("Unassign failed:", err);
      setError(err?.message || "Failed to unassign device.");
    } finally {
      setLoading(false);
      setTimeout(() => setToastMessage(""), 5000);
    }
  };

  // Tab change
  const handleTabChange = (newTab) => {
    setActiveTab(newTab);
    setSearchParams({ tab: newTab });
  };

  // Export Specs JSON
  const handleExportSpecs = () => {
    if (!profile) return;
    const exportPayload = {
      profile,
      deviceData,
      accessories: accessoriesData,
      software: softwareData,
      vulnerabilities: vulnerabilitiesData,
      licenses: licensesData,
    };
    const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(
      JSON.stringify(exportPayload, null, 2)
    )}`;
    const downloadAnchor = document.createElement("a");
    downloadAnchor.setAttribute("href", jsonString);
    downloadAnchor.setAttribute(
      "download",
      `${profile.employeeCode || profile.employeeId}_audit.json`
    );
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  // Summary counts
  const summaryCounts = useMemo(() => {
    if (!profile) return {};
    return {
      device: profile.assignedDevice ? 1 : 0,
      accessories: profile.summary?.accessoryCount ?? accessoriesData.length,
      softwares: profile.summary?.softwareCount ?? softwareData.length,
      vulnerabilities:
        profile.summary?.vulnerabilityCount ?? vulnerabilitiesData.length,
      licenses: profile.summary?.licenseCount ?? licensesData.length,
    };
  }, [
    profile,
    accessoriesData.length,
    softwareData.length,
    vulnerabilitiesData.length,
    licensesData.length,
  ]);

  // Unified header employee object
  const headerEmployee = useMemo(() => {
    if (!profile) return null;
    return {
      id: profile.employeeId,
      employeeId: profile.employeeCode || `EMP-${profile.employeeId}`,
      employeeName:
        profile.fullName ||
        [profile.firstName, profile.lastName].filter(Boolean).join(" "),
      email: profile.email,
      phone: profile.phone || "—",
      role: profile.designation || "Engineering",
      department: profile.department || "Enterprise Engineering",
      officeLocation: profile.location || "Corporate Headquarters",
      manager: profile.reportsTo?.fullName || "IT Endpoint Administrator",
      joinedDate: profile.dateOfJoining || "—",
      accountStatus: profile.employmentStatus || "ACTIVE",
    };
  }, [profile]);

  // Unified header device object
  const headerDevice = useMemo(() => {
    if (deviceData?.device) return deviceData.device;
    if (profile?.assignedDevice) {
      const dev = profile.assignedDevice;
      return {
        deviceId: dev.serialNumber || `DEV-${dev.deviceId}`,
        deviceName: dev.deviceName,
        serialNumber: dev.serialNumber,
        os: dev.operatingSystem || "Windows 11",
        status:
          dev.connectionStatus === "ONLINE"
            ? "Active"
            : dev.connectionStatus || "Offline",
      };
    }
    return null;
  }, [deviceData, profile]);

  if (loading) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="h-6 w-48 rounded bg-slate-200" />
        <div className="h-44 rounded-2xl bg-white p-6 shadow-sm border border-[#DCE5E1]" />
        <div className="h-14 rounded-2xl bg-white p-2 shadow-sm border border-[#DCE5E1]" />
        <div className="h-96 rounded-2xl bg-white p-6 shadow-sm border border-[#DCE5E1]" />
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center text-red-700 space-y-4">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-red-100 text-red-600">
          <svg
            className="h-7 w-7"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        </div>
        <h2 className="text-lg font-bold text-red-900">
          {error || "Employee Not Found"}
        </h2>
        <p className="text-xs text-red-600 max-w-md mx-auto">
          We couldn't retrieve the employee record for "{id}".
        </p>
        <button
          type="button"
          onClick={() => navigate("/employees")}
          className="inline-flex items-center gap-2 rounded-xl bg-red-600 px-4 py-2 text-sm font-semibold text-white shadow-xs hover:bg-red-700 transition-colors cursor-pointer"
        >
          ← Return to Employee List
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6 text-[#24211D]">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="flex items-center justify-between rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-800 shadow-sm animate-fadeIn">
          <div className="flex items-center gap-2">
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-200 text-emerald-800">
              ✓
            </span>
            <span>{toastMessage}</span>
          </div>
          <button
            type="button"
            onClick={() => setToastMessage("")}
            className="cursor-pointer text-emerald-700 hover:text-emerald-900"
          >
            ✕
          </button>
        </div>
      )}

      {/* 1. Header with Employee Details */}
      <EmployeeDetailHeader
        employee={headerEmployee}
        device={headerDevice}
        summaryCounts={summaryCounts}
        onExport={handleExportSpecs}
      />

      {/* Device Assignment Quick Actions Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-[#DCE5E1] bg-white p-4 shadow-2xs">
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-[#03312E]">
            Hardware Device Link:
          </span>
          {headerDevice ? (
            <span className="inline-flex items-center gap-1.5 rounded-lg bg-emerald-50 px-2.5 py-1 text-xs font-mono font-bold text-emerald-800 border border-emerald-200">
              <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
              Serial #{headerDevice.serialNumber} • {headerDevice.deviceName} (
              {headerDevice.status || "Active"})
            </span>
          ) : (
            <span className="rounded-lg bg-amber-50 px-2.5 py-1 text-xs font-medium text-amber-800 border border-amber-200">
              No hardware device assigned
            </span>
          )}
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenAssignModal}
            className="flex items-center gap-1.5"
          >
            {headerDevice
              ? "Reassign / Change Device"
              : "Assign Device by Serial #"}
          </Button>

          {headerDevice && (
            <Button
              variant="outline"
              size="sm"
              onClick={handleUnassignDevice}
              className="text-red-600 border-red-200 hover:bg-red-50 hover:text-red-700"
            >
              Unassign Device
            </Button>
          )}
        </div>
      </div>

      {/* 2. Main Tab Navigation Bar */}
      <EmployeeDetailTabs
        activeTab={activeTab}
        onTabChange={handleTabChange}
        counts={summaryCounts}
      />

      {/* 3. Tab Contents with Loading State */}
      <div className="min-h-[420px]">
        {tabLoading ? (
          <div className="rounded-2xl border border-[#DCE5E1] bg-white p-12 text-center text-sm text-gray-500 space-y-3">
            <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent" />
            <p>Loading tab information...</p>
          </div>
        ) : (
          <>
            {/* TAB 1: Device details */}
            {activeTab === "device" &&
              (deviceData?.device ? (
                <DeviceDetailsTab
                  device={deviceData.device}
                  networkAdapters={deviceData.networkAdapters}
                  deviceHistory={deviceData.deviceHistory}
                  rawDbDevice={deviceData.rawDbDevice}
                />
              ) : (
                <div className="rounded-2xl border border-dashed border-[#DCE5E1] bg-white p-12 text-center space-y-4">
                  <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-[#E3F2FD] text-[#1976D2]">
                    <svg
                      className="h-7 w-7"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <rect x="2" y="3" width="20" height="14" rx="2" />
                      <line x1="8" y1="21" x2="16" y2="21" />
                      <line x1="12" y1="17" x2="12" y2="21" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="text-base font-bold text-[#03312E]">
                      No Monitored Device Assigned
                    </h3>
                    <p className="mt-1 text-xs text-[#5C7370] max-w-md mx-auto">
                      Assign a hardware device using its unique Serial Number to
                      view live telemetry metrics, CPU/RAM utilization, and
                      installed software.
                    </p>
                  </div>
                  <Button variant="primary" onClick={handleOpenAssignModal}>
                    Assign Monitored Device Now
                  </Button>
                </div>
              ))}

            {/* TAB 2: accessories */}
            {activeTab === "accessories" && (
              <AccessoriesTab accessories={accessoriesData} />
            )}

            {/* TAB 3: software */}
            {activeTab === "software" && (
              <InstalledSoftwareTab installedSoftwares={softwareData} />
            )}

            {/* TAB 4: vulnerabilities */}
            {activeTab === "vulnerabilities" && (
              <VulnerabilitiesTab vulnerabilities={vulnerabilitiesData} />
            )}

            {/* TAB 5: licences */}
            {activeTab === "licences" && (
              <LicensesTab licenses={licensesData} />
            )}
          </>
        )}
      </div>

      {/* Assign / Reassign Device Modal */}
      <Modal
        isOpen={isAssignModalOpen}
        onClose={() => setIsAssignModalOpen(false)}
        title={headerDevice ? "Reassign Hardware Device" : "Assign Hardware Device"}
        subtitle="Select a monitored agent device or enter its unique Serial Number"
        size="md"
        footer={
          <>
            <Button
              variant="outline"
              onClick={() => setIsAssignModalOpen(false)}
              disabled={assignLoading}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleAssignSubmit}
              disabled={assignLoading}
            >
              {assignLoading ? "Assigning..." : "Confirm Assignment"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleAssignSubmit} className="space-y-4">
          {assignError && (
            <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700 font-medium">
              {assignError}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Select Monitored Device
            </label>
            {availableDevices.length > 0 ? (
              <select
                value={assignSerialNumber}
                onChange={(e) => setAssignSerialNumber(e.target.value)}
                className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] bg-white py-2.5 px-3.5 text-sm font-medium transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 cursor-pointer"
              >
                <option value="">-- Choose Monitored Device --</option>
                {availableDevices.map((dev) => (
                  <option
                    key={dev.serialNumber || dev.deviceId}
                    value={dev.serialNumber}
                  >
                    {dev.serialNumber} — {dev.deviceName || `${dev.manufacturer || ""} ${dev.model || ""}`} ({dev.connectionStatus || "ONLINE"})
                  </option>
                ))}
              </select>
            ) : (
              <p className="mt-1 text-xs text-gray-500">
                No detected devices found in dropdown. You can enter the serial
                number manually below.
              </p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#03312E]">
              Or Enter Serial Number Manually
            </label>
            <input
              type="text"
              value={assignSerialNumber}
              onChange={(e) =>
                setAssignSerialNumber(e.target.value.toUpperCase())
              }
              placeholder="e.g. PF5N8G2C"
              className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm font-mono uppercase transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
            />
            <p className="mt-1 text-[11px] text-[#64748B]">
              The unique hardware serial number connects this employee with the
              Asset Monitoring Agent telemetry.
            </p>
          </div>

          {headerDevice && (
            <div>
              <label className="block text-xs font-semibold text-[#03312E]">
                Reason for Reassignment (Optional)
              </label>
              <input
                type="text"
                value={reassignReason}
                onChange={(e) => setReassignReason(e.target.value)}
                placeholder="e.g. Hardware upgrade or device swap"
                className="mt-1.5 w-full rounded-xl border border-[#DCE5E1] py-2.5 px-3.5 text-sm transition-colors focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20"
              />
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
}
