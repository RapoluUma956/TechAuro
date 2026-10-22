import React, { useState, useMemo } from "react";
import { DataTable, StatusBadge, SearchBar } from "../../common";

export default function VulnerabilitiesTab({ vulnerabilities = [] }) {
  const [searchQuery, setSearchQuery] = useState("");
  const [severityFilter, setSeverityFilter] = useState("ALL");

  const filteredData = useMemo(() => {
    return vulnerabilities.filter((vuln) => {
      if (severityFilter !== "ALL" && (vuln.severity || "").toUpperCase() !== severityFilter) {
        return false;
      }
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase().trim();
        const fields = [
          vuln.cveId,
          vuln.softwareName,
          vuln.description,
          vuln.severity,
          vuln.remediationStatus,
        ];
        return fields.some((f) => (f || "").toLowerCase().includes(query));
      }
      return true;
    });
  }, [vulnerabilities, searchQuery, severityFilter]);

  const severityCounts = useMemo(() => {
    return {
      total: vulnerabilities.length,
      high: vulnerabilities.filter((v) => (v.severity || "").toLowerCase() === "high").length,
      medium: vulnerabilities.filter((v) => (v.severity || "").toLowerCase() === "medium").length,
      low: vulnerabilities.filter((v) => (v.severity || "").toLowerCase() === "low").length,
    };
  }, [vulnerabilities]);

  const columns = [
    {
      key: "cveId",
      header: "CVE ID",
      render: (item) => (
        <span className="font-mono text-xs font-black text-[#1976D2] bg-[#E3F2FD] px-2.5 py-1 rounded-md border border-[#BBDEFB]">
          {item.cveId}
        </span>
      ),
    },
    {
      key: "severity",
      header: "Severity",
      render: (item) => {
        const sev = (item.severity || "Low").toLowerCase();
        const badgeColors =
          sev === "critical" || sev === "high"
            ? { bg: "bg-rose-50", text: "text-rose-700", border: "border-rose-200", dot: "bg-rose-500", pulse: true }
            : sev === "medium"
            ? { bg: "bg-amber-50", text: "text-amber-700", border: "border-amber-200", dot: "bg-amber-500", pulse: false }
            : { bg: "bg-slate-50", text: "text-slate-700", border: "border-slate-200", dot: "bg-slate-400", pulse: false };

        return <StatusBadge status={item.severity} customColors={badgeColors} />;
      },
    },
    {
      key: "softwareName",
      header: "Affected Software",
      render: (item) => (
        <div>
          <p className="font-bold text-xs text-[#03312E]">{item.softwareName}</p>
          {item.version && (
            <p className="font-mono text-[11px] text-[#64748B]">Ver: {item.version}</p>
          )}
        </div>
      ),
    },
    {
      key: "description",
      header: "Vulnerability Description",
      render: (item) => (
        <p className="text-xs text-[#334155] leading-relaxed max-w-md">
          {item.description}
        </p>
      ),
    },
    {
      key: "detectedDate",
      header: "Detected Date",
      render: (item) => (
        <span className="font-mono text-xs text-[#5C7370]">
          {item.detectedDate}
        </span>
      ),
    },
    {
      key: "remediationStatus",
      header: "Remediation",
      render: (item) => (
        <div className="space-y-1">
          <StatusBadge status={item.remediationStatus || "Pending"} />
          {item.remediationAdvice && (
            <p className="text-[11px] text-[#64748B] italic max-w-xs">
              {item.remediationAdvice}
            </p>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Header and Summary Cards */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-[#03312E]">
            Device Vulnerabilities &amp; Security Risks
          </h2>
          <p className="text-xs text-[#5C7370]">
            Endpoint CVE findings, CVSS threat scores, and recommended patch mitigations for this device.
          </p>
        </div>

        {/* Search */}
        <div className="w-full sm:w-72">
          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Search CVEs or software..."
          />
        </div>
      </div>

      {/* Severity Metrics Strip */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <button
          type="button"
          onClick={() => setSeverityFilter("ALL")}
          className={`cursor-pointer rounded-xl p-3 text-left border transition-all ${
            severityFilter === "ALL"
              ? "border-[#1976D2] bg-[#E3F2FD] ring-2 ring-[#1976D2]/20"
              : "border-[#DCE5E1] bg-white hover:bg-[#F8FAF9]"
          }`}
        >
          <p className="text-[11px] font-bold uppercase text-[#64748B]">All CVEs</p>
          <p className="mt-0.5 text-lg font-black text-[#03312E]">{severityCounts.total}</p>
        </button>

        <button
          type="button"
          onClick={() => setSeverityFilter("HIGH")}
          className={`cursor-pointer rounded-xl p-3 text-left border transition-all ${
            severityFilter === "HIGH"
              ? "border-rose-400 bg-rose-50 ring-2 ring-rose-300"
              : "border-[#DCE5E1] bg-white hover:bg-[#F8FAF9]"
          }`}
        >
          <p className="text-[11px] font-bold uppercase text-rose-700">High / Critical</p>
          <p className="mt-0.5 text-lg font-black text-rose-700">{severityCounts.high}</p>
        </button>

        <button
          type="button"
          onClick={() => setSeverityFilter("MEDIUM")}
          className={`cursor-pointer rounded-xl p-3 text-left border transition-all ${
            severityFilter === "MEDIUM"
              ? "border-amber-400 bg-amber-50 ring-2 ring-amber-300"
              : "border-[#DCE5E1] bg-white hover:bg-[#F8FAF9]"
          }`}
        >
          <p className="text-[11px] font-bold uppercase text-amber-700">Medium</p>
          <p className="mt-0.5 text-lg font-black text-amber-700">{severityCounts.medium}</p>
        </button>

        <button
          type="button"
          onClick={() => setSeverityFilter("LOW")}
          className={`cursor-pointer rounded-xl p-3 text-left border transition-all ${
            severityFilter === "LOW"
              ? "border-slate-400 bg-slate-50 ring-2 ring-slate-300"
              : "border-[#DCE5E1] bg-white hover:bg-[#F8FAF9]"
          }`}
        >
          <p className="text-[11px] font-bold uppercase text-[#64748B]">Low</p>
          <p className="mt-0.5 text-lg font-black text-[#334155]">{severityCounts.low}</p>
        </button>
      </div>

      {/* Vulnerabilities Table */}
      <DataTable
        data={filteredData}
        columns={columns}
        keyField="id"
        showSerialNo={true}
        serialNoHeader="s.no"
        pageSize={5}
        emptyMessage="No vulnerabilities detected"
        emptySubtitle="This workstation has no active CVE security threats matching your filters."
      />
    </div>
  );
}
