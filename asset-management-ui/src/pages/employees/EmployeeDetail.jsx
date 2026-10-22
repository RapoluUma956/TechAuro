import React, { useState, useEffect, useMemo } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import { userService } from "../../services/userService";
import { getEmployeeDetailById, generateEmployeeDetail } from "../../data/employeeDetailsData";
import {
  EmployeeDetailHeader,
  EmployeeDetailTabs,
  DeviceDetailsTab,
  AccessoriesTab,
  InstalledSoftwareTab,
  VulnerabilitiesTab,
  LicensesTab,
} from "../../components/employees/detail";

export default function EmployeeDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Tab state synced with search param ?tab=
  const initialTab = searchParams.get("tab") || "device";
  const [activeTab, setActiveTab] = useState(initialTab);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [employeeDetail, setEmployeeDetail] = useState(null);

  useEffect(() => {
    let isMounted = true;

    async function loadData() {
      try {
        setLoading(true);
        setError("");

        // Attempt backend fetch first
        let backendUser = null;
        try {
          backendUser = await userService.getUserById(id);
        } catch {
          // Backend is optional/offline during preview, proceed to fallback seamlessly
        }

        if (backendUser && isMounted) {
          // Map backend response into detail schema
          const mappedEmp = {
            id: backendUser.userId || id,
            employeeId: backendUser.employeeCode || id,
            employeeName: [backendUser.firstName, backendUser.lastName]
              .filter(Boolean)
              .join(" ") || `Employee ${id}`,
            email: backendUser.email,
            phone: backendUser.phone,
            role: backendUser.designation || "Enterprise Specialist",
            accountStatus: backendUser.accountStatus || "Active",
            deviceStatus: backendUser.accountStatus || "Active",
            deviceName: backendUser.deviceName || 'MacBook Pro 16" (M3 Max)',
            deviceId: backendUser.deviceId || "DEV-8042",
          };
          const fullData = generateEmployeeDetail(mappedEmp);
          setEmployeeDetail(fullData);
        } else if (isMounted) {
          // Fallback to rich mock data matching the ID/code
          const localData = getEmployeeDetailById(id);
          if (localData) {
            setEmployeeDetail(localData);
          } else {
            setError(`Employee profile with identifier "${id}" could not be found.`);
          }
        }
      } catch (err) {
        if (isMounted) {
          // Graceful fallback
          const localData = getEmployeeDetailById(id);
          if (localData) {
            setEmployeeDetail(localData);
          } else {
            setError(err.message || "Unable to load employee details.");
          }
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    loadData();

    return () => {
      isMounted = false;
    };
  }, [id]);

  // Sync tab changes with URL query parameters
  const handleTabChange = (newTab) => {
    setActiveTab(newTab);
    setSearchParams({ tab: newTab });
  };

  // Export full JSON audit payload
  const handleExportSpecs = () => {
    if (!employeeDetail) return;
    const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(
      JSON.stringify(employeeDetail, null, 2)
    )}`;
    const downloadAnchor = document.createElement("a");
    downloadAnchor.setAttribute("href", jsonString);
    downloadAnchor.setAttribute(
      "download",
      `${employeeDetail.employeeId}_${employeeDetail.device?.deviceId || "specs"}_audit.json`
    );
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  const summaryCounts = useMemo(() => {
    if (!employeeDetail) return {};
    return {
      device: employeeDetail.device ? 1 : 0,
      accessories: employeeDetail.accessories?.length || 0,
      softwares: employeeDetail.installedSoftwares?.length || 0,
      vulnerabilities: employeeDetail.vulnerabilities?.length || 0,
      licenses: employeeDetail.licenses?.length || 0,
    };
  }, [employeeDetail]);

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

  if (error || !employeeDetail) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center text-red-700 space-y-4">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-red-100 text-red-600">
          <svg className="h-7 w-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        </div>
        <h2 className="text-lg font-bold text-red-900">
          {error || "Employee Not Found"}
        </h2>
        <p className="text-xs text-red-600 max-w-md mx-auto">
          We couldn't retrieve the employee record for "{id}". You can return to the employee directory.
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
      {/* 1. Header with Employee Name & Details from backend / mock */}
      <EmployeeDetailHeader
        employee={employeeDetail}
        device={employeeDetail.device}
        summaryCounts={summaryCounts}
        onExport={handleExportSpecs}
      />

      {/* 2. Main Tab Navigation Bar matching wireframe:
          [Device details] [accessories] [software] [vulnerabilities] [licences] */}
      <EmployeeDetailTabs
        activeTab={activeTab}
        onTabChange={handleTabChange}
        counts={summaryCounts}
      />

      {/* 3. Tab Contents */}
      <div className="min-h-[420px]">
        {/* TAB 1: Device details (Wireframe Panel 1) */}
        {activeTab === "device" && (
          <DeviceDetailsTab
            device={employeeDetail.device}
            cpuDetails={employeeDetail.cpuDetails}
            memoryDetails={employeeDetail.memoryDetails}
            storageDetails={employeeDetail.storageDetails}
            batteryDetails={employeeDetail.batteryDetails}
            securityDetails={employeeDetail.securityDetails}
            biosDetails={employeeDetail.biosDetails}
            networkAdapters={employeeDetail.networkAdapters}
            deviceHistory={employeeDetail.deviceHistory}
            rawDbDevice={employeeDetail.rawDb?.device}
          />
        )}

        {/* TAB 2: accessories (Wireframe Panel 2) */}
        {activeTab === "accessories" && (
          <AccessoriesTab accessories={employeeDetail.accessories || []} />
        )}

        {/* TAB 3: software (Wireframe Panel 3) */}
        {activeTab === "software" && (
          <InstalledSoftwareTab
            installedSoftwares={employeeDetail.installedSoftwares || []}
          />
        )}

        {/* TAB 4: vulnerabilities */}
        {activeTab === "vulnerabilities" && (
          <VulnerabilitiesTab
            vulnerabilities={employeeDetail.vulnerabilities || []}
          />
        )}

        {/* TAB 5: licences */}
        {activeTab === "licences" && (
          <LicensesTab licenses={employeeDetail.licenses || []} />
        )}
      </div>
    </div>
  );
}
