import React, { useState, useMemo } from "react";
import { DataTable, StatusBadge, SearchBar } from "../../common";

export default function VulnerabilitiesTab({ vulnerabilities = [] }) {
  const [searchQuery, setSearchQuery] = useState("");

  const filteredData = useMemo(() => {
    if (!searchQuery.trim()) return vulnerabilities;
    const query = searchQuery.toLowerCase().trim();
    const fields = [
      "cveId",
      "softwareName",
      "description",
      "severity",
      "remediationStatus",
    ];
    return vulnerabilities.filter((vuln) => {
      return fields.some((f) => (vuln[f] || "").toLowerCase().includes(query));
    });
  }, [vulnerabilities, searchQuery]);

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
        const sev = (item.severity || "").toLowerCase();
        const colors = {
          critical: "bg-purple-100 text-purple-700 border border-purple-200",
          high: "bg-rose-100 text-rose-700 border border-rose-200",
          medium: "bg-amber-100 text-amber-700 border border-amber-200",
          low: "bg-blue-100 text-blue-700 border border-blue-200",
        };
        return (
          <span
            className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-black uppercase ${
              colors[sev] || "bg-slate-100 text-slate-700"
            }`}
          >
            {item.severity}
          </span>
        );
      },
    },
    {
      key: "softwareName",
      header: "Vulnerable Software",
      render: (item) => (
        <div>
          <p className="font-bold text-sm text-[#03312E]">{item.softwareName}</p>
          {item.detectedVersion && (
            <p className="font-mono text-xs text-[#64748B]">v{item.detectedVersion}</p>
          )}
        </div>
      ),
    },
    {
      key: "description",
      header: "Threat Description",
      render: (item) => (
        <p className="max-w-md text-xs text-[#5C7370] line-clamp-2" title={item.description}>
          {item.description || "No threat intelligence summary available."}
        </p>
      ),
    },
    {
      key: "cvssScore",
      header: "CVSS Score",
      align: "center",
      render: (item) => {
        const score = Number(item.cvssScore || 0);
        const scoreColor =
          score >= 9.0 ? "text-purple-700" : score >= 7.0 ? "text-rose-700" : score >= 4.0 ? "text-amber-700" : "text-[#1976D2]";
        return <span className={`font-mono text-sm font-black ${scoreColor}`}>{score.toFixed(1)}</span>;
      },
    },
    {
      key: "remediationStatus",
      header: "Status",
      align: "center",
      render: (item) => (
        <StatusBadge
          status={item.remediationStatus || "Unpatched"}
          variant="pill"
        />
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Header matching wireframe */}
      <div className="flex flex-col gap-3">
        <div>
          <h2 className="text-xl font-black uppercase tracking-wider text-[#03312E]">
            CVE VULNERABILITY AUDIT
          </h2>
          <p className="text-xs text-[#5C7370]">
            Common Vulnerabilities and Exposures identified on this workstation by scheduled security audits.
          </p>
        </div>

        {/* Full Length Search */}
        <div className="w-full">
          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Search CVE ID, software name, description, or remediation status..."
            variant="long"
          />
        </div>
      </div>

      {/* Severity Counters Stat Badges (read-only metric view) */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <div className="rounded-xl p-3 text-left border border-[#DCE5E1] bg-white">
          <p className="text-[11px] font-bold uppercase text-[#64748B]">All CVEs</p>
          <p className="mt-0.5 text-lg font-black text-[#03312E]">{severityCounts.total}</p>
        </div>

        <div className="rounded-xl p-3 text-left border border-rose-200 bg-rose-50/50">
          <p className="text-[11px] font-bold uppercase text-rose-700">High / Critical</p>
          <p className="mt-0.5 text-lg font-black text-rose-700">{severityCounts.high}</p>
        </div>

        <div className="rounded-xl p-3 text-left border border-amber-200 bg-amber-50/50">
          <p className="text-[11px] font-bold uppercase text-amber-700">Medium</p>
          <p className="mt-0.5 text-lg font-black text-amber-700">{severityCounts.medium}</p>
        </div>

        <div className="rounded-xl p-3 text-left border border-slate-200 bg-slate-50/50">
          <p className="text-[11px] font-bold uppercase text-[#64748B]">Low</p>
          <p className="mt-0.5 text-lg font-black text-[#334155]">{severityCounts.low}</p>
        </div>
      </div>

      {/* Vulnerabilities Table with full sorting */}
      <DataTable
        data={filteredData}
        columns={columns}
        keyField="id"
        showSerialNo={true}
        serialNoHeader="#"
        pageSize={5}
        emptyMessage="No vulnerabilities detected"
        emptySubtitle="This workstation has no active CVE security threats matching your search."
      />
    </div>
  );
}
