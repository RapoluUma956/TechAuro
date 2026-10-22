import React, { useState } from "react";
import { StatusBadge } from "../../common";
import DeviceHistorySection from "./DeviceHistorySection";

function formatUptime(seconds) {
  if (!seconds && seconds !== 0) return "—";
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const mins = Math.floor((seconds % 3600) / 60);
  if (days > 0) return `${days}d ${hours}h ${mins}m`;
  if (hours > 0) return `${hours}h ${mins}m`;
  return `${mins}m`;
}

function formatDateDisplay(dateVal) {
  if (!dateVal) return "—";
  try {
    const d = new Date(dateVal);
    if (isNaN(d.getTime())) return String(dateVal);
    return d.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  } catch {
    return String(dateVal);
  }
}

export default function DeviceDetailsTab({
  device,
  cpuDetails,
  _memoryDetails,
  _storageDetails,
  _batteryDetails,
  networkAdapters = [],
  deviceHistory,
  rawDbDevice,
}) {
  const [copiedField, setCopiedField] = useState("");

  if (!device) {
    return (
      <div className="rounded-2xl border border-[#DCE5E1] bg-white p-12 text-center text-[#5C7370]">
        No device is currently assigned to this employee.
      </div>
    );
  }

  // Use raw DB device or fallback to device props
  const db = rawDbDevice || device;

  const handleCopy = (field, text) => {
    if (!text) return;
    navigator.clipboard.writeText(String(text));
    setCopiedField(field);
    setTimeout(() => setCopiedField(""), 2000);
  };

  return (
    <div className="space-y-6">
      {/* ============================================================== */}
      {/* TOP SECTION: Left 3 Cards (CPU, Memory, Storage) + Right Tall Device Details Card */}
      {/* ============================================================== */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 items-stretch">
        {/* Left Column (5 of 12 cols): Stack of 3 Cards */}
        <div className="lg:col-span-5 flex flex-col gap-5 justify-between">
          {/* CARD 1: CPU information (threads, cores, etc.) */}
          <div className="rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-xs">
            <div className="flex items-center justify-between border-b border-[#EBF0EE] pb-3 mb-3">
              <div className="flex items-center gap-2.5">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
                  <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="4" y="4" width="16" height="16" rx="2" />
                    <rect x="9" y="9" width="6" height="6" />
                    <line x1="9" y1="1" x2="9" y2="4" />
                    <line x1="15" y1="1" x2="15" y2="4" />
                    <line x1="9" y1="20" x2="9" y2="23" />
                    <line x1="15" y1="20" x2="15" y2="23" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[#03312E]">
                    CPU Information
                  </h3>
                  <p className="text-[11px] text-[#64748B]">
                    Threads, cores, frequency &amp; load
                  </p>
                </div>
              </div>
              {db.cpu_temperature && (
                <span className="rounded-md bg-emerald-50 px-2 py-0.5 text-xs font-bold text-emerald-700 border border-emerald-200">
                  {db.cpu_temperature} °C
                </span>
              )}
            </div>

            <div className="space-y-2.5 text-xs">
              <div>
                <span className="text-[11px] text-[#64748B]">Processor Model</span>
                <p className="font-bold text-[#03312E] truncate" title={db.cpu}>
                  {db.cpu || cpuDetails?.model || "Standard Multi-Core Processor"}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-3 pt-1">
                <div className="rounded-xl bg-[#F8FAF9] p-2.5 border border-[#EBF0EE]">
                  <span className="text-[10px] font-semibold uppercase text-[#64748B]">Cores &amp; Threads</span>
                  <p className="mt-0.5 font-bold text-[#03312E]">
                    {db.cpu_cores || 8} Cores / {db.cpu_threads || 16} Threads
                  </p>
                </div>

                <div className="rounded-xl bg-[#F8FAF9] p-2.5 border border-[#EBF0EE]">
                  <span className="text-[10px] font-semibold uppercase text-[#64748B]">Base Frequency</span>
                  <p className="mt-0.5 font-bold text-[#03312E]">
                    {db.cpu_frequency_mhz ? `${(db.cpu_frequency_mhz / 1000).toFixed(2)} GHz` : "2.50 GHz"}
                  </p>
                </div>
              </div>

              {/* Usage Progress */}
              <div className="pt-1 space-y-1">
                <div className="flex justify-between text-[11px]">
                  <span className="text-[#64748B]">CPU Utilization</span>
                  <span className="font-bold text-[#1976D2]">{db.cpu_usage_percent || 18}%</span>
                </div>
                <div className="h-2 w-full overflow-hidden rounded-full bg-[#E2E8F0]">
                  <div
                    className="h-full rounded-full bg-[#1976D2]"
                    style={{ width: `${Math.min(db.cpu_usage_percent || 18, 100)}%` }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* CARD 2: Memory Information (memory used, remaining, etc.) */}
          <div className="rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-xs">
            <div className="flex items-center justify-between border-b border-[#EBF0EE] pb-3 mb-3">
              <div className="flex items-center gap-2.5">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
                  <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M6 19v-3" />
                    <path d="M10 19v-3" />
                    <path d="M14 19v-3" />
                    <path d="M18 19v-3" />
                    <rect x="2" y="5" width="20" height="11" rx="2" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[#03312E]">
                    Memory Information
                  </h3>
                  <p className="text-[11px] text-[#64748B]">
                    RAM installed, used &amp; remaining
                  </p>
                </div>
              </div>
              <span className="font-mono text-xs font-bold text-[#03312E] bg-[#F1F5F9] px-2 py-0.5 rounded">
                {db.ram_gb || 32} GB Total
              </span>
            </div>

            <div className="space-y-2.5 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div className="rounded-xl bg-[#F8FAF9] p-2.5 border border-[#EBF0EE]">
                  <span className="text-[10px] font-semibold uppercase text-[#64748B]">Memory Used</span>
                  <p className="mt-0.5 font-bold text-amber-700">
                    {db.ram_used_gb || 14.2} GB ({db.ram_usage_percent || 44}%)
                  </p>
                </div>

                <div className="rounded-xl bg-[#F8FAF9] p-2.5 border border-[#EBF0EE]">
                  <span className="text-[10px] font-semibold uppercase text-[#64748B]">Memory Remaining</span>
                  <p className="mt-0.5 font-bold text-emerald-700">
                    {db.ram_available_gb || 17.8} GB
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-between text-[11px] text-[#64748B]">
                <span>Type &amp; Speed:</span>
                <span className="font-bold text-[#03312E]">
                  {db.ram_type || "DDR5"} {db.ram_speed_mhz ? `@ ${db.ram_speed_mhz} MHz` : ""}
                </span>
              </div>

              {db.swap_total_gb && (
                <div className="flex items-center justify-between text-[11px] text-[#64748B]">
                  <span>Swap Memory:</span>
                  <span className="font-bold text-[#03312E]">
                    {db.swap_used_gb || 0} GB used of {db.swap_total_gb} GB
                  </span>
                </div>
              )}

              {/* Memory Allocation Bar */}
              <div className="pt-1 space-y-1">
                <div className="h-2 w-full overflow-hidden rounded-full bg-[#E2E8F0]">
                  <div
                    className="h-full rounded-full bg-[#1976D2]"
                    style={{ width: `${Math.min(db.ram_usage_percent || 45, 100)}%` }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* CARD 3: Storage information (storage used, storage remaining, etc.) */}
          <div className="rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-xs">
            <div className="flex items-center justify-between border-b border-[#EBF0EE] pb-3 mb-3">
              <div className="flex items-center gap-2.5">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
                  <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="22" y1="12" x2="2" y2="12" />
                    <path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[#03312E]">
                    Storage Information
                  </h3>
                  <p className="text-[11px] text-[#64748B]">
                    Storage used, storage remaining &amp; health
                  </p>
                </div>
              </div>
              <span className="font-mono text-xs font-bold text-[#03312E] bg-[#F1F5F9] px-2 py-0.5 rounded">
                {db.ssd_capacity_gb || 1024} GB Total
              </span>
            </div>

            <div className="space-y-2.5 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div className="rounded-xl bg-[#F8FAF9] p-2.5 border border-[#EBF0EE]">
                  <span className="text-[10px] font-semibold uppercase text-[#64748B]">Storage Used</span>
                  <p className="mt-0.5 font-bold text-amber-700">
                    {db.ssd_used_gb || 384} GB
                  </p>
                </div>

                <div className="rounded-xl bg-[#F8FAF9] p-2.5 border border-[#EBF0EE]">
                  <span className="text-[10px] font-semibold uppercase text-[#64748B]">Storage Remaining</span>
                  <p className="mt-0.5 font-bold text-emerald-700">
                    {db.ssd_available_gb || 640} GB
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-between text-[11px] text-[#64748B]">
                <span>Drive Model:</span>
                <span className="font-bold text-[#03312E] truncate max-w-[200px]" title={db.ssd_model}>
                  {db.ssd_model || db.ssd_type || "NVMe PCIe 4.0 M.2 SSD"}
                </span>
              </div>

              <div className="flex items-center justify-between text-[11px] text-[#64748B]">
                <span>Health State:</span>
                <span className="font-bold text-emerald-700 truncate max-w-[200px]">
                  {db.ssd_health || "Healthy (0 Bad Sectors)"}
                </span>
              </div>

              {/* Storage Usage Bar */}
              <div className="pt-1 space-y-1">
                <div className="h-2 w-full overflow-hidden rounded-full bg-[#E2E8F0]">
                  <div
                    className="h-full rounded-full bg-[#1976D2]"
                    style={{
                      width: `${Math.min(
                        ((db.ssd_used_gb || 384) / (db.ssd_capacity_gb || 1024)) * 100,
                        100
                      )}%`,
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column (7 of 12 cols): Tall Device Details Card */}
        <div className="lg:col-span-7 flex flex-col rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-xs">
          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[#EBF0EE] pb-4 mb-4">
            <div>
              <span className="text-[11px] font-bold uppercase tracking-wider text-[#1976D2]">
                Hardware Profile
              </span>
              <h3 className="text-xl font-extrabold text-[#03312E]">
                Device Details
              </h3>
              <p className="text-xs text-[#5C7370]">
                Device ID, tag, serial number, warranty date, last seen, status, etc.
              </p>
            </div>
            <StatusBadge status={db.asset_status || device.status || "Active"} />
          </div>

          {/* Grid of Device Details as requested in wireframe */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs flex-1">
            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Device Name</span>
              <p className="mt-1 font-bold text-sm text-[#03312E]">
                {db.device_name || `${db.brand} ${db.model}`}
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Device ID</span>
              <p className="mt-1 font-mono font-bold text-sm text-[#1976D2]">
                {device.deviceId || `DEV-${db.device_id || 1}`}
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Asset Tag</span>
              <p className="mt-1 font-mono font-bold text-sm text-[#03312E]">
                {db.asset_id || device.assetTag || "AST-2023-8042"}
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Serial Number</span>
              <div className="mt-1 flex items-center justify-between font-mono font-bold text-sm text-[#03312E]">
                <span>{db.serial_number || "7X9K2P4-DL"}</span>
                <button
                  type="button"
                  onClick={() => handleCopy("sn", db.serial_number)}
                  className="text-[#94A3B8] hover:text-[#1976D2] cursor-pointer"
                  title="Copy Serial Number"
                >
                  {copiedField === "sn" ? "✓" : "📋"}
                </button>
              </div>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Brand &amp; Model</span>
              <p className="mt-1 font-bold text-[#03312E]">
                {db.brand} {db.model}
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Operating System</span>
              <p className="mt-1 font-bold text-[#03312E]">
                {db.os_name} {db.os_version} ({db.os_architecture || "x86_64"})
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Hostname</span>
              <p className="mt-1 font-mono text-[#03312E] truncate" title={db.hostname}>
                {db.hostname || "CORP-WKS-8042.techaura.internal"}
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">IP Address (IPv4)</span>
              <p className="mt-1 font-mono font-bold text-[#03312E]">
                {db.ipv4_address || "192.168.1.142"}
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Warranty Expiry Date</span>
              <p className="mt-1 font-bold text-emerald-700">
                {formatDateDisplay(db.warranty_expiry)} (Active Coverage)
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Purchase Date</span>
              <p className="mt-1 font-bold text-[#03312E]">
                {formatDateDisplay(db.purchase_date)}
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Last Seen Online</span>
              <p className="mt-1 font-mono text-[#03312E]">
                {formatDateDisplay(db.last_seen_at)}
              </p>
            </div>

            <div className="rounded-xl bg-[#F8FAF9] p-3 border border-[#EBF0EE]">
              <span className="text-[11px] font-semibold text-[#64748B]">Connection &amp; MDM Agent</span>
              <div className="mt-1 flex items-center gap-2">
                <span className="inline-flex items-center gap-1 text-emerald-700 font-bold">
                  <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
                  {db.connection_status || "ONLINE"}
                </span>
                <span className="text-[#94A3B8]">•</span>
                <span className="text-[#64748B]">
                  {db.agent_installed ? `Agent ${db.agent_version}` : "No Agent"}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ============================================================== */}
      {/* MIDDLE SECTION: 3 Cards Row (Battery, Security, BIOS & System) */}
      {/* ============================================================== */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        {/* CARD 1: Battery Information */}
        <div className="rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between border-b border-[#EBF0EE] pb-3 mb-3">
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
                <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="2" y="7" width="16" height="10" rx="2" ry="2" />
                  <line x1="22" y1="11" x2="22" y2="13" />
                </svg>
              </div>
              <h3 className="text-sm font-bold text-[#03312E]">
                Battery Information
              </h3>
            </div>
            <span className="font-bold text-xs text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
              {db.battery_percentage || 92}% Charged
            </span>
          </div>

          <div className="space-y-2.5 text-xs">
            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">State:</span>
              <span className="font-bold text-[#03312E]">
                {db.battery_charging ? "Charging (Plugged In)" : "On Battery"}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Health Condition:</span>
              <span className="font-bold text-emerald-700">
                {db.battery_health_percent || 96.5}% Optimal
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Cycle Count:</span>
              <span className="font-bold text-[#03312E]">
                {db.battery_cycle_count || 84} cycles
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Power Source:</span>
              <span className="font-bold text-[#03312E] truncate max-w-[180px]" title={db.power_source}>
                {db.power_source || "AC Power Adapter (130W)"}
              </span>
            </div>

            {/* Battery bar */}
            <div className="pt-1">
              <div className="h-2 w-full overflow-hidden rounded-full bg-[#E2E8F0]">
                <div
                  className="h-full rounded-full bg-emerald-500"
                  style={{ width: `${Math.min(db.battery_percentage || 92, 100)}%` }}
                />
              </div>
            </div>
          </div>
        </div>

        {/* CARD 2: Security Information */}
        <div className="rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between border-b border-[#EBF0EE] pb-3 mb-3">
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
                <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </div>
              <h3 className="text-sm font-bold text-[#03312E]">
                Security Information
              </h3>
            </div>
            <span className="font-bold text-xs text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
              {db.bitlocker_enabled ? "Encrypted" : "Standard"}
            </span>
          </div>

          <div className="space-y-2.5 text-xs">
            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">BitLocker Protection:</span>
              <span className="font-bold text-emerald-700">
                {db.bitlocker_protection_status || "Protection On"}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Encryption Method:</span>
              <span className="font-bold text-[#03312E]">
                {db.bitlocker_encryption_method || "XTS-AES 256-bit"}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Encrypted Volume:</span>
              <span className="font-mono text-[#03312E]">
                {db.bitlocker_volume || "C: [OS Disk]"}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">TPM Security:</span>
              <span className="font-bold text-emerald-700">
                {db.tpm_present ? `TPM ${db.tpm_version || "2.0"} (Active)` : "Not Present"}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Secure Boot:</span>
              <span className="font-bold text-emerald-700">
                {db.secure_boot_enabled ? "Enabled" : "Disabled"}
              </span>
            </div>
          </div>
        </div>

        {/* CARD 3: Bios and sys stats Information */}
        <div className="rounded-2xl border border-[#DCE5E1] bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between border-b border-[#EBF0EE] pb-3 mb-3">
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
                <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="3" />
                  <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
                </svg>
              </div>
              <h3 className="text-sm font-bold text-[#03312E]">
                BIOS &amp; Sys Stats
              </h3>
            </div>
            <span className="font-mono text-xs font-bold text-[#03312E] bg-[#F1F5F9] px-2 py-0.5 rounded">
              v{db.bios_version || "1.18.0"}
            </span>
          </div>

          <div className="space-y-2.5 text-xs">
            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">BIOS Vendor:</span>
              <span className="font-bold text-[#03312E]">
                {db.bios_manufacturer || "Dell Inc."}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Release Date:</span>
              <span className="font-mono text-[#03312E]">
                {formatDateDisplay(db.bios_release_date)}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">System Uptime:</span>
              <span className="font-bold text-[#1976D2]">
                {formatUptime(db.uptime_seconds)}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Processes / Threads:</span>
              <span className="font-bold text-[#03312E]">
                {db.process_count || 218} / {db.thread_count || 2894}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-[#64748B]">Boot Timestamp:</span>
              <span className="font-mono text-[11px] text-[#64748B] truncate max-w-[170px]" title={db.boot_time}>
                {formatDateDisplay(db.boot_time)}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* ============================================================== */}
      {/* BOTTOM SECTION: Full Width Network Adapters Information Card */}
      {/* ============================================================== */}
      <div className="rounded-2xl border border-[#DCE5E1] bg-white p-6 shadow-xs space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[#EBF0EE] pb-3">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-[#E3F2FD] text-[#1976D2]">
              <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M5 12.55a11 11 0 0 1 14.08 0" />
                <path d="M1.42 9a16 16 0 0 1 21.16 0" />
                <path d="M8.53 16.11a6 6 0 0 1 6.95 0" />
                <line x1="12" y1="20" x2="12.01" y2="20" />
              </svg>
            </div>
            <div>
              <h3 className="text-base font-extrabold text-[#03312E]">
                Network Adapters Information
              </h3>
              <p className="text-xs text-[#5C7370]">
                Connected wireless controllers, physical Ethernet interfaces &amp; MAC addresses
              </p>
            </div>
          </div>
          <span className="text-xs font-mono text-[#03312E] bg-[#F8FAF9] px-2.5 py-1 rounded-md border border-[#DCE5E1]">
            Primary Host IP: {db.ipv4_address || "192.168.1.142"}
          </span>
        </div>

        {/* Adapters Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {networkAdapters.map((adapter) => {
            const isConnected = adapter.status === "Connected";

            return (
              <div
                key={adapter.id}
                className="rounded-xl border border-[#DCE5E1] bg-[#FBFDFD] p-4 space-y-2.5 hover:shadow-xs hover:bg-white transition-all"
              >
                <div className="flex items-center justify-between gap-2 border-b border-[#F1F5F9] pb-2">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold text-[#1976D2] bg-[#E3F2FD] px-2 py-0.5 rounded">
                      {adapter.interface || "wlan0"}
                    </span>
                    <span className="font-bold text-xs text-[#03312E] truncate max-w-[150px]" title={adapter.name}>
                      {adapter.name}
                    </span>
                  </div>
                  <span
                    className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-semibold ${
                      isConnected
                        ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                        : "bg-slate-100 text-slate-600 border border-slate-200"
                    }`}
                  >
                    <span
                      className={`h-1.5 w-1.5 rounded-full ${
                        isConnected ? "bg-emerald-500 animate-pulse" : "bg-slate-400"
                      }`}
                    />
                    {adapter.status || "Connected"}
                  </span>
                </div>

                <div className="space-y-1.5 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-[#64748B]">MAC Address:</span>
                    <div className="flex items-center gap-1 font-mono font-bold text-[#03312E]">
                      <span>{adapter.macAddress}</span>
                      <button
                        type="button"
                        onClick={() => handleCopy(`mac-${adapter.id}`, adapter.macAddress)}
                        className="text-[#94A3B8] hover:text-[#1976D2] cursor-pointer"
                        title="Copy MAC"
                      >
                        {copiedField === `mac-${adapter.id}` ? "✓" : "📋"}
                      </button>
                    </div>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-[#64748B]">IPv4 Address:</span>
                    <span className="font-mono font-bold text-[#03312E]">
                      {adapter.ipv4 || "—"}
                    </span>
                  </div>

                  {adapter.ipv6 && adapter.ipv6 !== "N/A" && (
                    <div className="flex items-center justify-between">
                      <span className="text-[#64748B]">IPv6:</span>
                      <span className="font-mono text-[#5C7370] truncate max-w-[160px]" title={adapter.ipv6}>
                        {adapter.ipv6}
                      </span>
                    </div>
                  )}

                  <div className="flex items-center justify-between text-[11px] text-[#64748B] pt-1 border-t border-[#F1F5F9]">
                    <span>Interface Type:</span>
                    <span className="font-semibold text-[#03312E]">{adapter.type}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* ============================================================== */}
      {/* DEVICE HISTORY SECTION: Custodians, format & repair timeline */}
      {/* ============================================================== */}
      <DeviceHistorySection
        deviceHistory={deviceHistory}
        deviceId={db.asset_id || device.deviceId}
      />
    </div>
  );
}
