/**
 * Database Static Data Model
 * Faithfully mirrors the Database Schema (ERD):
 * 1. USER & SECURITY: users, user_roles, roles, role_permissions, permissions
 * 2. DEVICES (LAPTOPS): devices (all 60+ columns), device_networks, vulnerabilities, device_assignments, installed_softwares
 * 3. ACCESSORIES: accessories, accessory_assignments
 * 4. LICENSE MANAGEMENT: licenses, license_assignments
 * 5. NOTIFICATIONS: notifications
 * 6. AUDIT LOGS: audit_logs
 */

export const dbUsers = [
  {
    user_id: 1,
    employee_code: "EMP-1001",
    first_name: "Sathwika",
    last_name: "Rapolu",
    email: "sathwika.rapolu@techaura.io",
    phone: "+1 (415) 892-4019",
    password_hash: "$2a$12$e8Yk2wQh9.8U.vK88G7nO.T9vK0w1.9M.8vK2w1.8vK2w18vK2w1",
    designation: "Lead Full Stack Engineer",
    profile_image: "/avatars/sathwika.png",
    date_of_joining: "2022-01-15",
    account_status: "ACTIVE",
    must_change_password: false,
    password_changed_at: "2024-01-10T09:30:00Z",
    last_login_at: "2024-10-15T09:12:44Z",
    created_at: "2022-01-15T08:00:00Z",
    updated_at: "2024-10-15T09:12:44Z",
  },
  {
    user_id: 2,
    employee_code: "EMP-1002",
    first_name: "Arjun",
    last_name: "Verma",
    email: "arjun.verma@techaura.io",
    phone: "+1 (415) 773-8201",
    password_hash: "$2a$12$e8Yk2wQh9.8U.vK88G7nO.T9vK0w1.9M.8vK2w1.8vK2w18vK2w2",
    designation: "Senior Backend Developer",
    profile_image: "/avatars/arjun.png",
    date_of_joining: "2022-06-01",
    account_status: "ACTIVE",
    must_change_password: false,
    password_changed_at: "2024-02-14T11:20:00Z",
    last_login_at: "2024-10-15T08:45:10Z",
    created_at: "2022-06-01T08:00:00Z",
    updated_at: "2024-10-15T08:45:10Z",
  },
  {
    user_id: 3,
    employee_code: "EMP-1003",
    first_name: "Priya",
    last_name: "Sharma",
    email: "priya.sharma@techaura.io",
    phone: "+1 (415) 612-4412",
    password_hash: "$2a$12$e8Yk2wQh9.8U.vK88G7nO.T9vK0w1.9M.8vK2w1.8vK2w18vK2w3",
    designation: "Principal Product Designer",
    profile_image: "/avatars/priya.png",
    date_of_joining: "2022-09-15",
    account_status: "ACTIVE",
    must_change_password: false,
    password_changed_at: "2024-03-01T10:00:00Z",
    last_login_at: "2024-10-14T17:10:00Z",
    created_at: "2022-09-15T08:00:00Z",
    updated_at: "2024-10-14T17:10:00Z",
  },
  {
    user_id: 4,
    employee_code: "EMP-1004",
    first_name: "Rohan",
    last_name: "Kulkarni",
    email: "rohan.kulkarni@techaura.io",
    phone: "+1 (415) 990-5512",
    password_hash: "$2a$12$e8Yk2wQh9.8U.vK88G7nO.T9vK0w1.9M.8vK2w1.8vK2w18vK2w4",
    designation: "Cybersecurity Analyst",
    profile_image: "/avatars/rohan.png",
    date_of_joining: "2023-02-10",
    account_status: "ACTIVE",
    must_change_password: false,
    password_changed_at: "2024-04-12T14:15:00Z",
    last_login_at: "2024-10-15T07:22:15Z",
    created_at: "2023-02-10T08:00:00Z",
    updated_at: "2024-10-15T07:22:15Z",
  },
  {
    user_id: 5,
    employee_code: "EMP-1005",
    first_name: "Ananya",
    last_name: "Deshmukh",
    email: "ananya.d@techaura.io",
    phone: "+1 (415) 331-8902",
    password_hash: "$2a$12$e8Yk2wQh9.8U.vK88G7nO.T9vK0w1.9M.8vK2w1.8vK2w18vK2w5",
    designation: "People Operations Specialist",
    profile_image: "/avatars/ananya.png",
    date_of_joining: "2023-05-18",
    account_status: "INACTIVE",
    must_change_password: false,
    password_changed_at: "2024-01-15T09:00:00Z",
    last_login_at: "2024-09-30T16:00:00Z",
    created_at: "2023-05-18T08:00:00Z",
    updated_at: "2024-09-30T16:00:00Z",
  },
];

export const dbRoles = [
  { role_id: 1, role_name: "ADMIN", description: "Full system administrative rights", created_at: "2022-01-01T00:00:00Z" },
  { role_id: 2, role_name: "ENGINEER", description: "Software engineering staff access", created_at: "2022-01-01T00:00:00Z" },
  { role_id: 3, role_name: "SECURITY_AUDITOR", description: "Security compliance and audit access", created_at: "2022-01-01T00:00:00Z" },
];

export const dbUserRoles = [
  { user_id: 1, role_id: 2, assigned_at: "2022-01-15T08:00:00Z" },
  { user_id: 2, role_id: 2, assigned_at: "2022-06-01T08:00:00Z" },
  { user_id: 4, role_id: 3, assigned_at: "2023-02-10T08:00:00Z" },
];

/**
 * DEVICES Table - Contains ALL fields from the DB Schema diagram:
 */
export const dbDevices = [
  {
    device_id: 1,
    asset_id: "AST-2023-8042",
    device_name: "Dell Precision 5570 Workstation",
    hostname: "CORP-WKS-8042.techaura.internal",
    brand: "Dell Inc.",
    model: "Precision 5570",
    serial_number: "7X9K2P4-DL",

    // CPU Fields
    cpu: "12th Gen Intel(R) Core(TM) i9-12900H",
    cpu_vendor: "GenuineIntel",
    cpu_cores: 14,
    cpu_threads: 20,
    cpu_frequency_mhz: 2500,
    cpu_usage_percent: 18.4,
    cpu_temperature: 44.5,

    // RAM Fields
    ram_gb: 32.0,
    ram_type: "DDR5 SODIMM",
    ram_speed_mhz: 4800,
    ram_used_gb: 14.2,
    ram_available_gb: 17.8,
    ram_usage_percent: 44.38,
    swap_total_gb: 8.0,
    swap_used_gb: 0.25,

    // SSD Fields
    ssd_capacity_gb: 1024.0,
    ssd_type: "NVMe PCIe 4.0 x4 M.2 SSD",
    ssd_model: "Samsung PM9A1 NVMe 1024GB",
    ssd_used_gb: 384.6,
    ssd_available_gb: 639.4,
    ssd_health: "Healthy (99% Life Remaining, 0 Bad Sectors)",

    // OS Fields
    os_name: "Microsoft Windows 11 Enterprise",
    os_version: "23H2",
    os_build: "22631.3880",
    os_architecture: "x86_64",

    // Network IPs
    ipv4_address: "192.168.1.142",
    ipv6_address: "fe80::9a14:b2ff:fe31:88c2",

    // Battery & Power Fields
    battery_percentage: 92.0,
    battery_charging: true,
    battery_health_percent: 96.5,
    battery_cycle_count: 84,
    power_source: "AC Power Adapter (130W USB-C PD)",

    // BitLocker & Security Fields
    bitlocker_enabled: true,
    bitlocker_protection_status: "Protection On",
    bitlocker_volume_status: "Fully Encrypted",
    bitlocker_encryption_percent: 100.0,
    bitlocker_encryption_method: "XTS-AES 256-bit",
    bitlocker_key_protector: "TPM and Numerical Password",
    bitlocker_volume: "C: [Enterprise System OS]",

    // BIOS & Firmware Fields
    bios_manufacturer: "Dell Inc.",
    bios_version: "1.18.0",
    bios_release_date: "2024-05-14",
    secure_boot_enabled: true,
    tpm_present: true,
    tpm_enabled: true,
    tpm_version: "2.0 (TCG v1.38 spec)",

    // Uptime & Process Stats
    uptime_seconds: 345600,
    boot_time: "2024-10-11T08:15:00Z",
    process_count: 218,
    thread_count: 2894,

    // Relational Foreign Key & Lifecycle
    current_user_id: 1, // Assigned to Sathwika Rapolu (EMP-1001)
    asset_status: "IN_USE",
    connection_status: "ONLINE",
    last_seen_at: "2024-10-15T11:42:10Z",
    agent_installed: true,
    agent_version: "v3.8.2-enterprise",
    purchase_date: "2023-11-20",
    warranty_expiry: "2026-11-20",
    created_at: "2023-11-20T10:00:00Z",
    updated_at: "2024-10-15T11:42:10Z",
  },
  {
    device_id: 2,
    asset_id: "AST-2023-7734",
    device_name: "Dell XPS 15 (9530)",
    hostname: "CORP-WKS-7734.techaura.internal",
    brand: "Dell Inc.",
    model: "XPS 15 9530",
    serial_number: "4H8M9Q2-DL",
    cpu: "13th Gen Intel(R) Core(TM) i7-13700H",
    cpu_vendor: "GenuineIntel",
    cpu_cores: 14,
    cpu_threads: 20,
    cpu_frequency_mhz: 2600,
    cpu_usage_percent: 22.1,
    cpu_temperature: 48.0,
    ram_gb: 32.0,
    ram_type: "DDR5 SODIMM",
    ram_speed_mhz: 5200,
    ram_used_gb: 18.4,
    ram_available_gb: 13.6,
    ram_usage_percent: 57.5,
    swap_total_gb: 8.0,
    swap_used_gb: 0.1,
    ssd_capacity_gb: 1024.0,
    ssd_type: "NVMe PCIe 4.0 x4",
    ssd_model: "Kioxia XG8 NVMe 1024GB",
    ssd_used_gb: 410.2,
    ssd_available_gb: 613.8,
    ssd_health: "Healthy (98% Life Remaining)",
    os_name: "Microsoft Windows 11 Enterprise",
    os_version: "23H2",
    os_build: "22631.3880",
    os_architecture: "x86_64",
    ipv4_address: "192.168.1.189",
    ipv6_address: "fe80::8841:99ef:fe11:2233",
    battery_percentage: 88.0,
    battery_charging: false,
    battery_health_percent: 94.0,
    battery_cycle_count: 112,
    power_source: "Battery Power",
    bitlocker_enabled: true,
    bitlocker_protection_status: "Protection On",
    bitlocker_volume_status: "Fully Encrypted",
    bitlocker_encryption_percent: 100.0,
    bitlocker_encryption_method: "XTS-AES 256-bit",
    bitlocker_key_protector: "TPM and Recovery Password",
    bitlocker_volume: "C: [OS Disk]",
    bios_manufacturer: "Dell Inc.",
    bios_version: "1.12.1",
    bios_release_date: "2024-04-10",
    secure_boot_enabled: true,
    tpm_present: true,
    tpm_enabled: true,
    tpm_version: "2.0",
    uptime_seconds: 172800,
    boot_time: "2024-10-13T09:00:00Z",
    process_count: 195,
    thread_count: 2410,
    current_user_id: 2, // Arjun Verma
    asset_status: "IN_USE",
    connection_status: "ONLINE",
    last_seen_at: "2024-10-15T11:40:00Z",
    agent_installed: true,
    agent_version: "v3.8.2-enterprise",
    purchase_date: "2023-01-10",
    warranty_expiry: "2025-12-10",
    created_at: "2023-01-10T09:00:00Z",
    updated_at: "2024-10-15T11:40:00Z",
  },
];

/**
 * DEVICE_NETWORKS Table
 */
export const dbDeviceNetworks = [
  {
    device_network_id: 1,
    device_id: 1,
    network_name: "Intel(R) Wi-Fi 6E AX211 160MHz",
    network_type: "Wireless 802.11ax",
    network_ipv4_address: "192.168.1.142 / 24",
    network_ipv6_address: "fe80::9a14:b2ff:fe31:88c2",
    mac_address: "F4:D4:88:5A:21:BC",
    first_connected_at: "2023-11-20T11:00:00Z",
    last_connected_at: "2024-10-15T11:42:10Z",
  },
  {
    device_network_id: 2,
    device_id: 1,
    network_name: "Realtek USB GbE Controller (Dock / RJ-45)",
    network_type: "Gigabit Ethernet",
    network_ipv4_address: "192.168.1.143 / 24",
    network_ipv6_address: "fe80::10b2:4ff:fe5a:21bd",
    mac_address: "F4:D4:88:5A:21:BD",
    first_connected_at: "2023-11-25T09:30:00Z",
    last_connected_at: "2024-10-14T18:00:00Z",
  },
  {
    device_network_id: 3,
    device_id: 1,
    network_name: "Intel(R) Wireless Bluetooth(R) Adapter",
    network_type: "Bluetooth 5.3",
    network_ipv4_address: "N/A",
    network_ipv6_address: "N/A",
    mac_address: "F4:D4:88:5A:21:BE",
    first_connected_at: "2023-11-20T11:00:00Z",
    last_connected_at: "2024-10-15T11:42:10Z",
  },
  {
    device_network_id: 4,
    device_id: 2,
    network_name: "Intel Wi-Fi 6E AX211",
    network_type: "Wireless",
    network_ipv4_address: "192.168.1.189 / 24",
    network_ipv6_address: "fe80::8841:99ef:fe11:2233",
    mac_address: "A4:B1:C2:73:90:11",
    first_connected_at: "2023-01-10T10:00:00Z",
    last_connected_at: "2024-10-15T11:40:00Z",
  },
];

/**
 * DEVICE_ASSIGNMENTS Table - Tracks previous and current employee custodians
 */
export const dbDeviceAssignments = [
  {
    assignment_id: 1,
    device_id: 1,
    user_id: 1, // Sathwika Rapolu (EMP-1001)
    assigned_at: "2023-11-20T10:00:00Z",
    returned_at: null, // Still assigned / active custodian
    status: "CURRENT",
  },
  {
    assignment_id: 2,
    device_id: 1,
    user_id: 2, // Arjun Verma (EMP-1002) - previous owner before Sathwika!
    assigned_at: "2023-01-10T09:00:00Z",
    returned_at: "2023-11-15T17:00:00Z",
    status: "RETURNED",
  },
  {
    assignment_id: 3,
    device_id: 2,
    user_id: 2, // Arjun Verma (EMP-1002)
    assigned_at: "2023-11-18T10:00:00Z",
    returned_at: null,
    status: "CURRENT",
  },
  {
    assignment_id: 4,
    device_id: 2,
    user_id: 4, // Rohan Kulkarni (EMP-1004) - previous owner of device 2
    assigned_at: "2022-09-01T09:00:00Z",
    returned_at: "2023-11-10T16:00:00Z",
    status: "RETURNED",
  },
];

/**
 * AUDIT_LOGS Table - Tracks repair, format, and reset history
 */
export const dbAuditLogs = [
  {
    audit_id: 1,
    user_id: 1,
    action: "FORMAT_AND_REIMAGE",
    entity_type: "devices",
    entity_id: 1,
    old_value: "User Profile: Arjun Verma (EMP-1002). Encryption Key Revoked.",
    new_value: "Full Cryptographic Disk Wipe & Fresh Enterprise Windows 11 Re-Image. Enrolled into Intune MDM for Sathwika Rapolu.",
    ip_address: "10.0.4.15 (IT Lab Station 4)",
    created_at: "2023-11-18T14:30:00Z",
  },
  {
    audit_id: 2,
    user_id: 1,
    action: "REPAIR_THERMAL_CLEANING",
    entity_type: "devices",
    entity_id: 1,
    old_value: "Thermal Sensor Readings: 58°C idle, dust accumulation logged.",
    new_value: "Dell Authorized On-site Service: Heatsink cleaned, thermal paste reapplied, full ePSA hardware diagnostic suite passed.",
    ip_address: "10.0.4.10",
    created_at: "2023-08-12T11:20:00Z",
  },
  {
    audit_id: 3,
    user_id: 1,
    action: "DEVICE_INITIAL_PROVISIONING",
    entity_type: "devices",
    entity_id: 1,
    old_value: "OEM Out-of-the-box state",
    new_value: "Intune MDM Autopilot Enrollment, BitLocker 256-bit Key Escrowed to Azure AD, Corporate Certificates Installed.",
    ip_address: "10.0.1.200 (Cloud Gateway)",
    created_at: "2023-01-08T09:15:00Z",
  },
  {
    audit_id: 4,
    user_id: 2,
    action: "BIOS_FIRMWARE_UPGRADE",
    entity_type: "devices",
    entity_id: 2,
    old_value: "BIOS v1.8.0",
    new_value: "BIOS upgraded to v1.12.1 security patch release.",
    ip_address: "192.168.1.189",
    created_at: "2024-04-12T10:00:00Z",
  },
];

/**
 * INSTALLED_SOFTWARES Table
 */
export const dbInstalledSoftwares = [
  {
    installed_software_id: 1,
    device_id: 1,
    software_name: "Docker Desktop Enterprise",
    version: "4.28.0 (Build 139021)",
    publisher: "Docker Inc.",
    install_date: "2023-11-22",
    install_path: "C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe",
    detected_at: "2023-11-22T14:00:00Z",
    status: "UPDATE_AVAILABLE",
  },
  {
    installed_software_id: 2,
    device_id: 1,
    software_name: "Visual Studio Code",
    version: "1.87.2",
    publisher: "Microsoft Corporation",
    install_date: "2023-11-21",
    install_path: "C:\\Users\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe",
    detected_at: "2023-11-21T10:00:00Z",
    status: "ACTIVE",
  },
  {
    installed_software_id: 3,
    device_id: 1,
    software_name: "Google Chrome Enterprise",
    version: "122.0.6261.128",
    publisher: "Google LLC",
    install_date: "2023-11-21",
    install_path: "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
    detected_at: "2023-11-21T10:15:00Z",
    status: "ACTIVE",
  },
  {
    installed_software_id: 4,
    device_id: 1,
    software_name: "Slack Desktop",
    version: "4.36.140",
    publisher: "Slack Technologies",
    install_date: "2023-11-21",
    install_path: "C:\\Program Files\\Slack\\slack.exe",
    detected_at: "2023-11-21T10:30:00Z",
    status: "ACTIVE",
  },
  {
    installed_software_id: 5,
    device_id: 1,
    software_name: "Postman API Platform",
    version: "10.23.5",
    publisher: "Postman Inc.",
    install_date: "2023-11-23",
    install_path: "C:\\Users\\AppData\\Local\\Postman\\Postman.exe",
    detected_at: "2023-11-23T11:00:00Z",
    status: "ACTIVE",
  },
  {
    installed_software_id: 6,
    device_id: 1,
    software_name: "Node.js JavaScript Runtime",
    version: "v20.11.1 (LTS)",
    publisher: "OpenJS Foundation",
    install_date: "2023-11-21",
    install_path: "C:\\Program Files\\nodejs\\node.exe",
    detected_at: "2023-11-21T10:45:00Z",
    status: "ACTIVE",
  },
  {
    installed_software_id: 7,
    device_id: 1,
    software_name: "1Password for Windows",
    version: "8.10.28",
    publisher: "AgileBits Inc.",
    install_date: "2023-11-21",
    install_path: "C:\\Program Files\\1Password\\1Password.exe",
    detected_at: "2023-11-21T11:00:00Z",
    status: "ACTIVE",
  },
  {
    installed_software_id: 8,
    device_id: 1,
    software_name: "JetBrains IntelliJ IDEA Ultimate",
    version: "2023.3.4",
    publisher: "JetBrains s.r.o.",
    install_date: "2023-12-05",
    install_path: "C:\\Program Files\\JetBrains\\IntelliJ IDEA 2023.3.4\\bin\\idea64.exe",
    detected_at: "2023-12-05T15:00:00Z",
    status: "ACTIVE",
  },
  {
    installed_software_id: 9,
    device_id: 1,
    software_name: "SentinelOne Singularity Agent",
    version: "23.3.2.14",
    publisher: "SentinelOne Inc.",
    install_date: "2023-11-20",
    install_path: "C:\\Program Files\\SentinelOne\\Sentinel Agent\\SentinelAgent.exe",
    detected_at: "2023-11-20T10:00:00Z",
    status: "ACTIVE",
  },
  {
    installed_software_id: 10,
    device_id: 1,
    software_name: "Zoom Workplace",
    version: "5.17.11 (31580)",
    publisher: "Zoom Video Communications",
    install_date: "2023-11-21",
    install_path: "C:\\Users\\AppData\\Roaming\\Zoom\\bin\\Zoom.exe",
    detected_at: "2023-11-21T11:20:00Z",
    status: "ACTIVE",
  },
];

/**
 * VULNERABILITIES Table
 */
export const dbVulnerabilities = [
  {
    vulnerability_id: 1,
    device_id: 1,
    vulnerability_name: "CVE-2024-21626",
    severity: "HIGH",
    description: "Internal file descriptor leak in runc engine allowing container processes to break out into host filesystem.",
    affected_software: "Docker Desktop (runc)",
    detected_at: "2024-03-01T08:24:00Z",
    status: "PATCH_AVAILABLE",
    resolved_at: null,
  },
  {
    vulnerability_id: 2,
    device_id: 1,
    vulnerability_name: "CVE-2023-4863",
    severity: "MEDIUM",
    description: "Heap buffer overflow in WebP image decoding library leading to potential memory corruption.",
    affected_software: "Google Chrome (libwebp)",
    detected_at: "2024-02-14T11:15:00Z",
    status: "MITIGATED",
    resolved_at: "2024-02-15T09:00:00Z",
  },
  {
    vulnerability_id: 3,
    device_id: 1,
    vulnerability_name: "CVE-2024-0056",
    severity: "LOW",
    description: "Minor cryptographic timing discrepancy in Diffie-Hellman parameter verification under strict cipher settings.",
    affected_software: "Node.js (crypto module)",
    detected_at: "2024-01-22T14:40:00Z",
    status: "IN_REVIEW",
    resolved_at: null,
  },
];

/**
 * ACCESSORIES Table
 */
export const dbAccessories = [
  {
    accessory_id: 1,
    accessory_name: 'Dell UltraSharp 27" 4K USB-C Hub Monitor (U2723QE)',
    category: "Monitors",
    brand: "Dell",
    model: "U2723QE",
    purchase_date: "2023-10-15",
    total_quantity: 60,
    available_quantity: 14,
    unavailable_quantity: 46,
    created_at: "2023-10-15T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
  {
    accessory_id: 2,
    accessory_name: "CalDigit TS4 Thunderbolt 4 Docking Station (18-in-1)",
    category: "Docking Stations",
    brand: "CalDigit",
    model: "TS4-US",
    purchase_date: "2023-09-10",
    total_quantity: 45,
    available_quantity: 8,
    unavailable_quantity: 37,
    created_at: "2023-09-10T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
  {
    accessory_id: 3,
    accessory_name: "Logitech MX Master 3S Wireless Performance Mouse",
    category: "Peripherals",
    brand: "Logitech",
    model: "MX Master 3S",
    purchase_date: "2023-08-01",
    total_quantity: 80,
    available_quantity: 25,
    unavailable_quantity: 55,
    created_at: "2023-08-01T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
  {
    accessory_id: 4,
    accessory_name: "Jabra Evolve2 75 Wireless ANC Headset with Charging Stand",
    category: "Audio / Headset",
    brand: "Jabra",
    model: "Evolve2 75",
    purchase_date: "2023-11-05",
    total_quantity: 35,
    available_quantity: 3,
    unavailable_quantity: 32,
    created_at: "2023-11-05T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
  {
    accessory_id: 5,
    accessory_name: "Logitech MX Mechanical Wireless Keyboard",
    category: "Peripherals",
    brand: "Logitech",
    model: "MX Mechanical",
    purchase_date: "2023-01-12",
    total_quantity: 75,
    available_quantity: 22,
    unavailable_quantity: 53,
    created_at: "2023-01-12T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
];

/**
 * ACCESSORY_ASSIGNMENTS Table
 */
export const dbAccessoryAssignments = [
  {
    assignment_id: 1,
    accessory_id: 1,
    user_id: 1, // Sathwika Rapolu
    quantity: 1,
    assigned_at: "2023-11-25T10:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
  {
    assignment_id: 2,
    accessory_id: 2,
    user_id: 1, // Sathwika Rapolu
    quantity: 1,
    assigned_at: "2023-11-25T10:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
  {
    assignment_id: 3,
    accessory_id: 3,
    user_id: 1, // Sathwika Rapolu
    quantity: 1,
    assigned_at: "2023-11-25T10:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
  {
    assignment_id: 4,
    accessory_id: 4,
    user_id: 1, // Sathwika Rapolu
    quantity: 1,
    assigned_at: "2024-01-10T14:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
  {
    assignment_id: 5,
    accessory_id: 5,
    user_id: 1, // Sathwika Rapolu (Previously issued, now returned)
    quantity: 1,
    assigned_at: "2023-01-15T09:00:00Z",
    returned_at: "2023-11-20T17:00:00Z",
    status: "RETURNED",
  },
];

/**
 * LICENSES Table
 */
export const dbLicenses = [
  {
    license_id: 1,
    software_name: "GitHub Enterprise Cloud",
    license_key: "GH-ENT-8042-SAT",
    license_type: "Annual Developer Seat",
    total_licenses: 50,
    purchase_date: "2023-01-15",
    expiry_date: "2027-01-15",
    status: "ACTIVE",
    created_at: "2023-01-15T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
  {
    license_id: 2,
    software_name: "JetBrains All Products Pack",
    license_key: "JB-2024-ALL-991",
    license_type: "Annual Commercial Subscription",
    total_licenses: 30,
    purchase_date: "2023-11-20",
    expiry_date: "2026-11-20",
    status: "ACTIVE",
    created_at: "2023-11-20T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
  {
    license_id: 3,
    software_name: "Figma Organization Seat",
    license_key: "FIG-ORG-DES-410",
    license_type: "Per-User Monthly Seat",
    total_licenses: 20,
    purchase_date: "2023-12-31",
    expiry_date: "2026-12-31",
    status: "ACTIVE",
    created_at: "2023-12-31T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
  {
    license_id: 4,
    software_name: "Slack Enterprise Grid",
    license_key: "SLK-CORP-9021-SAT",
    license_type: "Enterprise Workspace License",
    total_licenses: 150,
    purchase_date: "2023-06-30",
    expiry_date: "2027-06-30",
    status: "ACTIVE",
    created_at: "2023-06-30T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
  {
    license_id: 5,
    software_name: "1Password Business",
    license_key: "1PW-BIZ-5502-ENT",
    license_type: "Annual SSO Subscription",
    total_licenses: 100,
    purchase_date: "2023-03-15",
    expiry_date: "2027-03-15",
    status: "ACTIVE",
    created_at: "2023-03-15T08:00:00Z",
    updated_at: "2024-10-15T08:00:00Z",
  },
];

/**
 * LICENSE_ASSIGNMENTS Table
 */
export const dbLicenseAssignments = [
  {
    assignment_id: 1,
    license_id: 1,
    user_id: 1,
    assigned_at: "2023-11-20T10:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
  {
    assignment_id: 2,
    license_id: 2,
    user_id: 1,
    assigned_at: "2023-12-01T09:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
  {
    assignment_id: 3,
    license_id: 3,
    user_id: 1,
    assigned_at: "2024-01-10T11:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
  {
    assignment_id: 4,
    license_id: 4,
    user_id: 1,
    assigned_at: "2023-11-20T10:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
  {
    assignment_id: 5,
    license_id: 5,
    user_id: 1,
    assigned_at: "2023-11-20T10:00:00Z",
    returned_at: null,
    status: "ASSIGNED",
  },
];

/**
 * NOTIFICATIONS Table
 */
export const dbNotifications = [
  {
    notification_id: 1,
    user_id: 1,
    device_id: 1,
    title: "Security Advisory: Patch Available",
    message: "Docker Desktop runc vulnerability CVE-2024-21626 has patch v4.28.1 available for installation.",
    notification_type: "SECURITY_ALERT",
    severity: "HIGH",
    is_read: false,
    created_at: "2024-10-15T09:30:00Z",
    read_at: null,
  },
  {
    notification_id: 2,
    user_id: 1,
    device_id: 1,
    title: "Quarterly Hardware Inspection Scheduled",
    message: "Preventive maintenance check scheduled for workstation AST-2023-8042.",
    notification_type: "MAINTENANCE",
    severity: "INFO",
    is_read: true,
    created_at: "2024-10-10T14:00:00Z",
    read_at: "2024-10-10T16:20:00Z",
  },
];
