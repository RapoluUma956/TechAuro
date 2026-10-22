import {
  dbUsers,
  dbDevices,
  dbDeviceNetworks,
  dbDeviceAssignments,
  dbInstalledSoftwares,
  dbVulnerabilities,
  dbAccessories,
  dbAccessoryAssignments,
  dbLicenses,
  dbLicenseAssignments,
  dbAuditLogs,
} from "./dbStaticData";
import { initialEmployeesData } from "./employeesData";

// Helper to format timestamps and dates
function formatDate(dateStr) {
  if (!dateStr) return "—";
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  } catch {
    return dateStr;
  }
}

/**
 * Returns complete joined single employee details according to the DB schema
 */
export function getEmployeeDetailById(idOrCode) {
  if (!idOrCode) return null;
  const queryStr = String(idOrCode).trim().toLowerCase();

  // 1. Locate user in dbUsers
  let user = dbUsers.find(
    (u) =>
      String(u.user_id) === queryStr ||
      u.employee_code.toLowerCase() === queryStr ||
      `${u.first_name} ${u.last_name}`.toLowerCase() === queryStr
  );

  // If not found in dbUsers, look in initialEmployeesData and generate a dbUser
  if (!user) {
    const fallback = initialEmployeesData.find(
      (e) =>
        String(e.id) === queryStr ||
        String(e.employeeId).toLowerCase() === queryStr ||
        (e.employeeName && e.employeeName.toLowerCase().includes(queryStr))
    );

    const nameParts = (fallback?.employeeName || `Employee ${idOrCode}`).split(" ");
    const firstName = nameParts[0] || "User";
    const lastName = nameParts.slice(1).join(" ") || "Employee";

    user = {
      user_id: fallback?.id || (parseInt(queryStr, 10) || 99),
      employee_code: fallback?.employeeId || (idOrCode.startsWith("EMP-") ? idOrCode : `EMP-${idOrCode}`),
      first_name: firstName,
      last_name: lastName,
      email: fallback?.email || `${firstName.toLowerCase()}.${lastName.toLowerCase()}@techaura.io`,
      phone: "+1 (555) 349-8812",
      password_hash: "$2a$12$...",
      designation: fallback?.role || "Enterprise Software Specialist",
      profile_image: "",
      date_of_joining: "2023-01-15",
      account_status: fallback?.deviceStatus === "Inactive" ? "INACTIVE" : "ACTIVE",
      must_change_password: false,
      password_changed_at: "2024-01-10T10:00:00Z",
      last_login_at: "2024-10-15T09:00:00Z",
      created_at: "2023-01-15T08:00:00Z",
      updated_at: "2024-10-15T09:00:00Z",
    };
  }

  // 2. Locate or synthesize assigned device in dbDevices
  let rawDevice = dbDevices.find((d) => d.current_user_id === user.user_id);

  if (!rawDevice) {
    // Generate full device matching the user
    const devIdNum = 1000 + (user.user_id % 9000);
    rawDevice = {
      device_id: user.user_id,
      asset_id: `AST-2023-${devIdNum}`,
      device_name: `Dell Latitude 7440 Workstation`,
      hostname: `CORP-WKS-${devIdNum}.techaura.internal`,
      brand: "Dell Inc.",
      model: "Latitude 7440",
      serial_number: `DL-7440-${devIdNum}X`,
      cpu: "13th Gen Intel(R) Core(TM) i7-1365U vPro",
      cpu_vendor: "GenuineIntel",
      cpu_cores: 10,
      cpu_threads: 12,
      cpu_frequency_mhz: 1800,
      cpu_usage_percent: 16.5,
      cpu_temperature: 42.0,
      ram_gb: 16.0,
      ram_type: "LPDDR5",
      ram_speed_mhz: 4800,
      ram_used_gb: 7.8,
      ram_available_gb: 8.2,
      ram_usage_percent: 48.75,
      swap_total_gb: 4.0,
      swap_used_gb: 0.1,
      ssd_capacity_gb: 512.0,
      ssd_type: "NVMe PCIe 4.0 x4",
      ssd_model: "Kioxia BG5 512GB",
      ssd_used_gb: 184.2,
      ssd_available_gb: 327.8,
      ssd_health: "Healthy (100% Life Remaining, 0 Bad Sectors)",
      os_name: "Microsoft Windows 11 Enterprise",
      os_version: "23H2",
      os_build: "22631.3880",
      os_architecture: "x86_64",
      ipv4_address: "192.168.1.155",
      ipv6_address: "fe80::14b2:88ef:2104:9901",
      battery_percentage: 95.0,
      battery_charging: true,
      battery_health_percent: 98.0,
      battery_cycle_count: 42,
      power_source: "AC Power (65W Type-C)",
      bitlocker_enabled: true,
      bitlocker_protection_status: "Protection On",
      bitlocker_volume_status: "Fully Encrypted",
      bitlocker_encryption_percent: 100.0,
      bitlocker_encryption_method: "XTS-AES 256-bit",
      bitlocker_key_protector: "TPM and Numerical Password",
      bitlocker_volume: "C: [OS Volume]",
      bios_manufacturer: "Dell Inc.",
      bios_version: "1.10.0",
      bios_release_date: "2024-03-20",
      secure_boot_enabled: true,
      tpm_present: true,
      tpm_enabled: true,
      tpm_version: "2.0",
      uptime_seconds: 259200,
      boot_time: "2024-10-12T08:00:00Z",
      process_count: 180,
      thread_count: 2200,
      current_user_id: user.user_id,
      asset_status: "IN_USE",
      connection_status: "ONLINE",
      last_seen_at: "2024-10-15T11:40:00Z",
      agent_installed: true,
      agent_version: "v3.8.2-enterprise",
      purchase_date: "2023-04-12",
      warranty_expiry: "2026-04-12",
      created_at: "2023-04-12T08:00:00Z",
      updated_at: "2024-10-15T11:40:00Z",
    };
  }

  // 3. Locate networks for this device
  let networks = dbDeviceNetworks.filter((n) => n.device_id === rawDevice.device_id);
  if (networks.length === 0) {
    networks = [
      {
        device_network_id: 101,
        device_id: rawDevice.device_id,
        network_name: "Intel(R) Wi-Fi 6E AX211 160MHz",
        network_type: "Wireless 802.11ax",
        network_ipv4_address: rawDevice.ipv4_address || "192.168.1.155 / 24",
        network_ipv6_address: rawDevice.ipv6_address || "fe80::14b2:88ef:2104:9901",
        mac_address: "A4:B1:C2:55:10:99",
        first_connected_at: "2023-04-12T10:00:00Z",
        last_connected_at: "2024-10-15T11:40:00Z",
      },
      {
        device_network_id: 102,
        device_id: rawDevice.device_id,
        network_name: "Realtek Gigabit Ethernet Controller",
        network_type: "Gigabit Ethernet",
        network_ipv4_address: "Configured (DHCP)",
        network_ipv6_address: "—",
        mac_address: "A4:B1:C2:55:10:9A",
        first_connected_at: "2023-04-15T09:00:00Z",
        last_connected_at: "2024-10-10T17:00:00Z",
      },
    ];
  }

  // 4. Locate device assignments (prior owners history!)
  let rawAssignments = dbDeviceAssignments.filter((a) => a.device_id === rawDevice.device_id);
  if (rawAssignments.length === 0) {
    rawAssignments = [
      {
        assignment_id: 901,
        device_id: rawDevice.device_id,
        user_id: user.user_id,
        assigned_at: "2023-04-12T10:00:00Z",
        returned_at: null,
        status: "CURRENT",
      },
      {
        assignment_id: 902,
        device_id: rawDevice.device_id,
        user_id: 2, // Prior owner Arjun Verma
        assigned_at: "2022-09-01T09:00:00Z",
        returned_at: "2023-04-05T17:00:00Z",
        status: "RETURNED",
      },
    ];
  }

  // Join assignments with users to form complete ownership history
  const ownershipHistory = rawAssignments.map((a) => {
    const assigneeUser = dbUsers.find((u) => u.user_id === a.user_id) || {
      employee_code: `EMP-${a.user_id}`,
      first_name: "Prior",
      last_name: "Employee",
      designation: "Software Engineer",
    };

    return {
      id: `assign-${a.assignment_id}`,
      assignmentId: a.assignment_id,
      userId: a.user_id,
      employeeId: assigneeUser.employee_code,
      employeeName: `${assigneeUser.first_name} ${assigneeUser.last_name}`.trim(),
      role: assigneeUser.designation,
      fromDate: formatDate(a.assigned_at),
      toDate: a.returned_at ? formatDate(a.returned_at) : "Present",
      status: a.status,
      isCurrent: a.status === "CURRENT",
      handoverReason:
        a.status === "CURRENT"
          ? "Active primary corporate workstation allocation"
          : "Device returned upon scheduled department hardware rotation",
    };
  });

  // 5. Locate audit logs (reset, format, repairs)
  let rawAuditLogs = dbAuditLogs.filter(
    (l) => l.entity_type === "devices" && l.entity_id === rawDevice.device_id
  );
  if (rawAuditLogs.length === 0) {
    rawAuditLogs = [
      {
        audit_id: 801,
        user_id: user.user_id,
        action: "FORMAT_AND_REIMAGE",
        entity_type: "devices",
        entity_id: rawDevice.device_id,
        old_value: "Prior employee data wiped. Cryptographic SSD erasure completed.",
        new_value: "Fresh deployment of Enterprise OS Gold Master image with BitLocker 256-bit encryption.",
        ip_address: "10.0.4.15 (IT Lab)",
        created_at: "2023-04-10T14:00:00Z",
      },
      {
        audit_id: 802,
        user_id: user.user_id,
        action: "MAINTENANCE_DIAGNOSTICS",
        entity_type: "devices",
        entity_id: rawDevice.device_id,
        old_value: "Routine preventive service check",
        new_value: "Full OEM hardware diagnostics passed. Battery health verified at 98%. Thermal paste inspected.",
        ip_address: "10.0.4.10",
        created_at: "2023-01-15T11:00:00Z",
      },
    ];
  }

  const maintenanceHistory = rawAuditLogs.map((log) => ({
    id: `audit-${log.audit_id}`,
    auditId: log.audit_id,
    date: formatDate(log.created_at),
    action: log.action,
    type: log.action.includes("FORMAT")
      ? "Reset & Format"
      : log.action.includes("REPAIR") || log.action.includes("MAINTENANCE")
      ? "Repair & Inspection"
      : "Configuration",
    title: log.action.replace(/_/g, " "),
    performedBy: `IT Administrator (IP: ${log.ip_address})`,
    description: log.new_value,
    oldValue: log.old_value,
    status: "Verified & Logged",
  }));

  // 6. Locate installed software
  let rawSoftwares = dbInstalledSoftwares.filter((s) => s.device_id === rawDevice.device_id);
  if (rawSoftwares.length === 0) {
    rawSoftwares = dbInstalledSoftwares.slice(0, 6);
  }

  const installedSoftwares = rawSoftwares.map((s) => ({
    id: s.installed_software_id,
    softwareName: s.software_name,
    version: s.version,
    publisher: s.publisher,
    installDate: formatDate(s.install_date),
    installPath: s.install_path,
    status: s.status === "ACTIVE" ? "Up to date" : "Update Available",
    architecture: rawDevice.os_architecture || "x86_64",
    detectedAt: formatDate(s.detected_at),
  }));

  // 7. Locate vulnerabilities
  let rawVulns = dbVulnerabilities.filter((v) => v.device_id === rawDevice.device_id);
  if (rawVulns.length === 0) {
    rawVulns = dbVulnerabilities;
  }

  const vulnerabilities = rawVulns.map((v) => ({
    id: v.vulnerability_id,
    cveId: v.vulnerability_name,
    severity: v.severity.charAt(0).toUpperCase() + v.severity.slice(1).toLowerCase(),
    softwareName: v.affected_software,
    description: v.description,
    detectedDate: formatDate(v.detected_at),
    remediationStatus:
      v.status === "PATCH_AVAILABLE"
        ? "Patch Available"
        : v.status === "MITIGATED"
        ? "Mitigated"
        : "In Review",
    remediationAdvice: "Apply vendor update or deploy via enterprise endpoint management policy.",
    resolvedAt: v.resolved_at ? formatDate(v.resolved_at) : null,
  }));

  // 8. Locate accessories for this user
  let rawAccAssigns = dbAccessoryAssignments.filter((a) => a.user_id === user.user_id);
  if (rawAccAssigns.length === 0) {
    rawAccAssigns = dbAccessoryAssignments.slice(0, 3);
  }

  const accessories = rawAccAssigns.map((a) => {
    const acc = dbAccessories.find((accItem) => accItem.accessory_id === a.accessory_id) || {
      accessory_name: "Dell UltraSharp 27\" 4K Monitor",
      category: "Monitors",
      brand: "Dell",
      model: "U2723QE",
    };

    return {
      id: a.assignment_id,
      accessoryId: a.accessory_id,
      accessoryName: acc.accessory_name,
      accessoryType: acc.category,
      brand: acc.brand,
      model: acc.model,
      givenDate: formatDate(a.assigned_at),
      returnDate: a.returned_at ? formatDate(a.returned_at) : "— (In Use)",
      status: a.status === "ASSIGNED" ? "Assigned" : "Returned",
      condition: "Good",
      assetTag: `ACC-${acc.category.substring(0, 3).toUpperCase()}-${String(a.accessory_id).padStart(3, "0")}`,
      serialNumber: `SN-${acc.brand.substring(0, 2).toUpperCase()}-${String(a.assignment_id * 1928)}`,
    };
  });

  // 9. Locate licenses for this user
  let rawLicAssigns = dbLicenseAssignments.filter((l) => l.user_id === user.user_id);
  if (rawLicAssigns.length === 0) {
    rawLicAssigns = dbLicenseAssignments.slice(0, 4);
  }

  const licenses = rawLicAssigns.map((l) => {
    const lic = dbLicenses.find((licItem) => licItem.license_id === l.license_id) || {
      software_name: "GitHub Enterprise Cloud",
      license_key: "GH-ENT-8042-SAT",
      license_type: "Annual Subscription",
      expiry_date: "2027-01-15",
      status: "ACTIVE",
    };

    return {
      id: l.assignment_id,
      licenseId: l.license_id,
      softwareName: lic.software_name,
      licenseKey: lic.license_key,
      vendor: lic.software_name.includes("GitHub") ? "Microsoft / GitHub" : "Enterprise SaaS",
      licenseType: lic.license_type,
      assignedDate: formatDate(l.assigned_at),
      expiryDate: formatDate(lic.expiry_date),
      status: lic.status === "ACTIVE" ? "Active" : "Expiring Soon",
      tier: "Enterprise Tier",
    };
  });

  // Assemble and return the complete unified Employee Details object
  return {
    // User / Employee Details
    employeeId: user.employee_code,
    employeeName: `${user.first_name} ${user.last_name}`.trim(),
    email: user.email,
    phone: user.phone,
    role: user.designation,
    department: "Enterprise Technology & Engineering",
    officeLocation: "Headquarters (Hybrid Workspace)",
    manager: "Vikram Malhotra (IT Director)",
    joinedDate: formatDate(user.date_of_joining),
    accountStatus: user.account_status,
    lastLoginAt: formatDate(user.last_login_at),
    createdAt: formatDate(user.created_at),

    // Raw DB Table Records (Exact snake_case columns preserved for DB transparency!)
    rawDb: {
      user: user,
      device: rawDevice,
      networks: networks,
      assignments: rawAssignments,
      auditLogs: rawAuditLogs,
      softwares: rawSoftwares,
      vulnerabilities: rawVulns,
      accessories: rawAccAssigns,
      licenses: rawLicAssigns,
    },

    // Entire devices table with all columns
    device: {
      ...rawDevice,
      // Convenience aliases
      deviceId: `DEV-${rawDevice.device_id}`,
      deviceName: rawDevice.device_name,
      assetTag: rawDevice.asset_id,
      serialNumber: rawDevice.serial_number,
      os: `${rawDevice.os_name} ${rawDevice.os_version} (Build ${rawDevice.os_build})`,
      status: rawDevice.asset_status === "IN_USE" ? "Active" : rawDevice.asset_status,
      warrantyDate: formatDate(rawDevice.warranty_expiry),
      purchaseDate: formatDate(rawDevice.purchase_date),
      mdmStatus: rawDevice.agent_installed ? `Enrolled (${rawDevice.agent_version})` : "Not Enrolled",
      encryptionStatus: rawDevice.bitlocker_enabled
        ? `BitLocker (${rawDevice.bitlocker_encryption_method})`
        : "Disabled",
      lastSeen: formatDate(rawDevice.last_seen_at),
    },

    // Categorized sub-details for the wireframe box
    cpuDetails: {
      model: rawDevice.cpu,
      vendor: rawDevice.cpu_vendor,
      architecture: rawDevice.os_architecture,
      cores: `${rawDevice.cpu_cores} Cores, ${rawDevice.cpu_threads} Threads`,
      clockSpeed: `${(rawDevice.cpu_frequency_mhz / 1000).toFixed(2)} GHz Base Frequency`,
      utilization: `${rawDevice.cpu_usage_percent}%`,
      temperature: `${rawDevice.cpu_temperature} °C`,
      processCount: rawDevice.process_count,
      threadCount: rawDevice.thread_count,
      uptime: `${Math.floor(rawDevice.uptime_seconds / 86400)} days, ${Math.floor((rawDevice.uptime_seconds % 86400) / 3600)} hours`,
      bootTime: formatDate(rawDevice.boot_time),
    },

    memoryDetails: {
      total: `${rawDevice.ram_gb} GB ${rawDevice.ram_type}`,
      type: rawDevice.ram_type,
      speed: `${rawDevice.ram_speed_mhz} MHz`,
      used: `${rawDevice.ram_used_gb} GB (${rawDevice.ram_usage_percent}%)`,
      available: `${rawDevice.ram_available_gb} GB`,
      swapTotal: `${rawDevice.swap_total_gb} GB`,
      swapUsed: `${rawDevice.swap_used_gb} GB`,
      usagePercent: rawDevice.ram_usage_percent,
    },

    storageDetails: {
      capacity: `${rawDevice.ssd_capacity_gb} GB (${(rawDevice.ssd_capacity_gb / 1024).toFixed(1)} TB)`,
      type: rawDevice.ssd_type,
      model: rawDevice.ssd_model,
      used: `${rawDevice.ssd_used_gb} GB`,
      available: `${rawDevice.ssd_available_gb} GB`,
      health: rawDevice.ssd_health,
    },

    batteryDetails: {
      percentage: `${rawDevice.battery_percentage}%`,
      charging: rawDevice.battery_charging ? "Charging" : "Discharging",
      health: `${rawDevice.battery_health_percent}% Health Condition`,
      cycleCount: `${rawDevice.battery_cycle_count} cycles`,
      powerSource: rawDevice.power_source,
    },

    securityDetails: {
      bitlockerEnabled: rawDevice.bitlocker_enabled ? "Enabled" : "Disabled",
      protectionStatus: rawDevice.bitlocker_protection_status,
      volumeStatus: rawDevice.bitlocker_volume_status,
      encryptionMethod: rawDevice.bitlocker_encryption_method,
      encryptionPercent: `${rawDevice.bitlocker_encryption_percent}%`,
      keyProtector: rawDevice.bitlocker_key_protector,
      volume: rawDevice.bitlocker_volume,
      secureBoot: rawDevice.secure_boot_enabled ? "Enabled" : "Disabled",
      tpmPresent: rawDevice.tpm_present ? "Present (Yes)" : "No",
      tpmEnabled: rawDevice.tpm_enabled ? "Active (Yes)" : "Disabled",
      tpmVersion: rawDevice.tpm_version,
    },

    biosDetails: {
      manufacturer: rawDevice.bios_manufacturer,
      version: rawDevice.bios_version,
      releaseDate: formatDate(rawDevice.bios_release_date),
      agentInstalled: rawDevice.agent_installed ? "Yes (Installed)" : "No",
      agentVersion: rawDevice.agent_version,
      connectionStatus: rawDevice.connection_status,
      lastSeenAt: formatDate(rawDevice.last_seen_at),
    },

    networkAdapters: networks.map((net) => ({
      id: `net-${net.device_network_id}`,
      name: net.network_name,
      type: net.network_type,
      interface: net.network_type.includes("Wireless") ? "Wi-Fi (wlan0)" : "Ethernet (eth0)",
      macAddress: net.mac_address,
      ipv4: net.network_ipv4_address,
      ipv6: net.network_ipv6_address,
      firstConnected: formatDate(net.first_connected_at),
      lastConnected: formatDate(net.last_connected_at),
      status: net.network_ipv4_address.includes("192") ? "Connected" : "Disconnected",
    })),

    deviceHistory: {
      ownership: ownershipHistory,
      maintenance: maintenanceHistory,
    },

    accessories: accessories,
    installedSoftwares: installedSoftwares,
    vulnerabilities: vulnerabilities,
    licenses: licenses,
  };
}

export function generateEmployeeDetail(baseEmployee) {
  if (!baseEmployee) return null;
  return getEmployeeDetailById(baseEmployee.employeeId || baseEmployee.id);
}
