package com.assetmanagement.backend.config;

import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final HealthThresholdRepository healthThresholdRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final LicenseRepository licenseRepository;

    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            HealthThresholdRepository healthThresholdRepository,
            DeviceRepository deviceRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            VulnerabilityRepository vulnerabilityRepository,
            LicenseRepository licenseRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.healthThresholdRepository = healthThresholdRepository;
        this.deviceRepository = deviceRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.licenseRepository = licenseRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = getOrCreateRole("ADMIN", "System and IT Administrator");
        Role employeeRole = getOrCreateRole("EMPLOYEE", "Standard Organization Employee");

        // 1. Seed default Admin if not present
        User admin = userRepository.findByEmail("admin@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmployeeCode("EMP001");
            u.setFirstName("Admin");
            u.setLastName("User");
            u.setEmail("admin@example.com");
            u.setPhone("+1 (415) 555-0101");
            u.setPasswordHash(passwordEncoder.encode("Admin@123"));
            u.setDesignation("IT Administrator");
            u.setAccountStatus("ACTIVE");
            u.setMustChangePassword(false);
            u.setDateOfJoining(LocalDate.now().minusYears(1));
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            u = userRepository.save(u);

            UserRole adminUserRole = new UserRole();
            adminUserRole.setUser(u);
            adminUserRole.setRole(adminRole);
            adminUserRole.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(adminUserRole);
            return u;
        });

        // 2. Seed default Employee if not present
        if (userRepository.findByEmail("employee@example.com").isEmpty()) {
            User employee = new User();
            employee.setEmployeeCode("EMP002");
            employee.setFirstName("Employee");
            employee.setLastName("User");
            employee.setEmail("employee@example.com");
            employee.setPhone("+1 (415) 555-0102");
            employee.setPasswordHash(passwordEncoder.encode("Initial@123"));
            employee.setDesignation("Employee");
            employee.setAccountStatus("ACTIVE");
            employee.setMustChangePassword(true);
            employee.setDateOfJoining(LocalDate.now().minusMonths(1));
            employee.setCreatedAt(LocalDateTime.now());
            employee.setUpdatedAt(LocalDateTime.now());
            employee = userRepository.save(employee);

            UserRole employeeUserRole = new UserRole();
            employeeUserRole.setUser(employee);
            employeeUserRole.setRole(employeeRole);
            employeeUserRole.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(employeeUserRole);
        }

        // 3. Seed Health Thresholds
        if (healthThresholdRepository.count() == 0) {
            HealthThreshold threshold = new HealthThreshold(90, 90, 90, 5, 30, 30);
            threshold.setUpdatedAt(LocalDateTime.now());
            healthThresholdRepository.save(threshold);
        }

        // 4. Seed Devices, Employees, Assignments, Vulnerabilities, and Licenses if device table is empty
        if (deviceRepository.count() == 0) {
            seedDashboardData(employeeRole);
        }
    }

    private void seedDashboardData(Role employeeRole) {
        LocalDateTime now = LocalDateTime.now();

        // Seed Users
        User alice = getOrCreateUser("EMP101", "Alice", "Johnson", "alice.johnson@example.com", "Senior Software Engineer", employeeRole);
        User bob = getOrCreateUser("EMP102", "Bob", "Smith", "bob.smith@example.com", "Sales Executive", employeeRole);
        User carol = getOrCreateUser("EMP103", "Carol", "White", "carol.white@example.com", "HR Specialist", employeeRole);
        User david = getOrCreateUser("EMP104", "David", "Lee", "david.lee@example.com", "Finance Lead", employeeRole);
        User emma = getOrCreateUser("EMP105", "Emma", "Davis", "emma.davis@example.com", "Marketing Manager", employeeRole);
        User frank = getOrCreateUser("EMP106", "Frank", "Miller", "frank.miller@example.com", "Staff Platform Engineer", employeeRole);
        User grace = getOrCreateUser("EMP107", "Grace", "Hopper", "grace.hopper@example.com", "DevOps Architect", employeeRole);
        User hank = getOrCreateUser("EMP108", "Hank", "Pym", "hank.pym@example.com", "Product Designer", employeeRole);

        // Seed Devices
        // LAP-001 (Critical: CPU 92%, RAM 78%, Disk 65%, Online)
        Device lap001 = createDevice("LAP-001", "SN-DELL-001", "Dell", "XPS 15 9530", 92, 78, 65, "ONLINE", now.minusMinutes(1), LocalDate.now().plusMonths(8));
        assignDevice(lap001, alice);

        // DESK-014 (Warning: CPU 45%, RAM 62%, Disk 88%, Offline)
        Device desk014 = createDevice("DESK-014", "SN-HP-014", "HP", "EliteDesk 800 G9", 45, 62, 88, "OFFLINE", now.minusHours(2), LocalDate.now().plusMonths(14));
        assignDevice(desk014, bob);

        // LAP-032 (Critical: CPU 88%, RAM 81%, Disk 92%, Online)
        Device lap032 = createDevice("LAP-032", "SN-APPLE-032", "Apple", "MacBook Pro 16", 88, 81, 92, "ONLINE", now.minusMinutes(2), LocalDate.now().plusMonths(5));
        assignDevice(lap032, carol);

        // LAP-067 (Warning: CPU 60%, RAM 70%, Disk 61%, Offline timeout)
        Device lap067 = createDevice("LAP-067", "SN-LENOVO-067", "Lenovo", "ThinkPad X1 Carbon Gen 11", 60, 70, 61, "ONLINE", now.minusMinutes(25), LocalDate.now().plusDays(20));
        assignDevice(lap067, david);

        // DESK-089 (Healthy: CPU 32%, RAM 48%, Disk 42%, Online)
        Device desk089 = createDevice("DESK-089", "SN-DELL-089", "Dell", "OptiPlex 7010", 32, 48, 42, "ONLINE", now.minusMinutes(1), LocalDate.now().plusYears(2));
        assignDevice(desk089, emma);

        // LAP-102 (Critical: CPU 95%, RAM 84%, Disk 91%, Offline)
        Device lap102 = createDevice("LAP-102", "SN-APPLE-102", "Apple", "MacBook Air M3", 95, 84, 91, "OFFLINE", now.minusHours(4), LocalDate.now().plusMonths(11));
        assignDevice(lap102, frank);

        // DESK-115 (Warning: CPU 58%, RAM 72%, Disk 69%, Online, Warranty soon)
        Device desk115 = createDevice("DESK-115", "SN-HP-115", "HP", "Z2 Mini G9 Workstation", 58, 72, 69, "ONLINE", now.minusMinutes(3), LocalDate.now().plusDays(15));
        assignDevice(desk115, grace);

        // LAP-144 (Healthy: CPU 28%, RAM 44%, Disk 38%, Online)
        Device lap144 = createDevice("LAP-144", "SN-LENOVO-144", "Lenovo", "ThinkPad T14s Gen 4", 28, 44, 38, "ONLINE", now.minusMinutes(2), LocalDate.now().plusYears(1));
        assignDevice(lap144, hank);

        // Seed 10 additional healthy devices for realistic enterprise scale
        for (int i = 1; i <= 10; i++) {
            String tag = String.format("LAP-20%d", i);
            String sn = String.format("SN-CORP-20%d", i);
            createDevice(tag, sn, "Dell", "Latitude 5540", 25 + (i * 3), 35 + (i * 2), 40 + (i * 2), "ONLINE", now.minusMinutes(i % 4), LocalDate.now().plusMonths(12 + i));
        }

        // Seed Vulnerabilities
        createVulnerability(lap001, "CVE-2024-21626", "Critical runc container breakout allowing host access", "CRITICAL", now.minusHours(5));
        createVulnerability(lap001, "CVE-2024-38063", "Windows TCP/IP Remote Code Execution Vulnerability", "CRITICAL", now.minusHours(8));
        createVulnerability(lap032, "CVE-2024-43093", "Android Runtime OS Privilege Escalation / Buffer Overflow", "CRITICAL", now.minusDays(1));
        createVulnerability(lap067, "CVE-2023-4863", "Heap buffer overflow in WebP image decoding library", "HIGH", now.minusDays(2));
        createVulnerability(desk014, "CVE-2024-0056", "Timing side-channel discrepancy in TLS cipher suites", "HIGH", now.minusDays(3));
        createVulnerability(lap102, "CVE-2023-38545", "SOCKS5 heap buffer overflow in libcurl", "HIGH", now.minusDays(4));
        createVulnerability(lap001, "CVE-2023-21716", "Microsoft Word RCE in RTF parser", "HIGH", now.minusDays(5));
        createVulnerability(desk115, "CVE-2023-36884", "Office and Windows HTML RCE Vulnerability", "HIGH", now.minusDays(6));
        createVulnerability(lap032, "CVE-2024-1086", "Linux kernel nf_tables privilege escalation", "HIGH", now.minusDays(7));
        createVulnerability(desk089, "CVE-2023-35311", "Microsoft Outlook Security Feature Bypass", "HIGH", now.minusDays(8));
        createVulnerability(desk014, "CVE-2023-36033", "Windows DWM Core Library Elevation of Privilege", "MEDIUM", now.minusDays(9));
        createVulnerability(lap144, "CVE-2023-36036", "Windows Cloud Files Mini-filter Driver Elevation of Privilege", "MEDIUM", now.minusDays(10));
        createVulnerability(desk115, "CVE-2023-36025", "Windows SmartScreen Security Feature Bypass", "MEDIUM", now.minusDays(11));
        createVulnerability(lap067, "CVE-2024-21410", "Microsoft Exchange Server Privilege Escalation", "MEDIUM", now.minusDays(12));
        createVulnerability(lap102, "CVE-2023-38831", "WinRAR arbitrary code execution vulnerability", "MEDIUM", now.minusDays(13));
        createVulnerability(lap001, "CVE-2024-21413", "Microsoft Outlook Remote Code Execution", "LOW", now.minusDays(14));
        createVulnerability(desk089, "CVE-2023-36038", "ASP.NET Core Denial of Service Vulnerability", "LOW", now.minusDays(15));
        createVulnerability(lap144, "CVE-2023-36413", "Microsoft Office Security Feature Bypass", "LOW", now.minusDays(16));

        // Seed Licenses
        createLicense("Microsoft 365 E5", "Microsoft 365", "Microsoft Corp", 200, 180, LocalDate.now().minusDays(5), "EXPIRED");
        createLicense("Adobe Creative Cloud Enterprise", "Adobe CC", "Adobe Inc", 40, 5, LocalDate.now().minusDays(12), "EXPIRED");
        createLicense("CrowdStrike Falcon Enterprise", "CrowdStrike Falcon", "CrowdStrike", 250, 240, LocalDate.now().plusDays(3), "ACTIVE");
        createLicense("IntelliJ IDEA Ultimate", "IntelliJ IDEA", "JetBrains", 80, 75, LocalDate.now().plusDays(6), "ACTIVE");
        createLicense("Zoom Enterprise Webinar", "Zoom Workplace", "Zoom", 30, 28, LocalDate.now().plusDays(24), "ACTIVE");
        createLicense("GitHub Enterprise Cloud", "GitHub Enterprise", "GitHub Inc", 150, 145, LocalDate.now().plusMonths(8), "ACTIVE");
    }

    private User getOrCreateUser(String code, String first, String last, String email, String designation, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmployeeCode(code);
            u.setFirstName(first);
            u.setLastName(last);
            u.setEmail(email);
            u.setPhone("+1 (415) 555-01" + code.substring(code.length() - 2));
            u.setPasswordHash(passwordEncoder.encode("Initial@123"));
            u.setDesignation(designation);
            u.setAccountStatus("ACTIVE");
            u.setMustChangePassword(false);
            u.setDateOfJoining(LocalDate.now().minusMonths(6));
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            u = userRepository.save(u);

            UserRole ur = new UserRole();
            ur.setUser(u);
            ur.setRole(role);
            ur.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(ur);
            return u;
        });
    }

    private Device createDevice(String tag, String sn, String brand, String model, int cpu, int ram, int disk, String connStatus, LocalDateTime recordedAt, LocalDate warrantyEnd) {
        Device d = new Device();
        d.setAssetTag(tag);
        d.setSerialNumber(sn);
        d.setBrand(brand);
        d.setModel(model);
        d.setStatus("ACTIVE");
        d.setCpuUsagePercent(BigDecimal.valueOf(cpu));
        d.setMemoryUsagePercent(BigDecimal.valueOf(ram));
        d.setStorageUsagePercent(BigDecimal.valueOf(disk));
        d.setConnectionStatus(connStatus);
        d.setRecordedAt(recordedAt);
        d.setWarrantyStartDate(LocalDate.now().minusYears(1));
        d.setWarrantyEndDate(warrantyEnd);
        d.setCreatedAt(LocalDateTime.now().minusMonths(6));
        d.setUpdatedAt(recordedAt);
        return deviceRepository.save(d);
    }

    private void assignDevice(Device device, User user) {
        DeviceAssignment da = new DeviceAssignment();
        da.setDevice(device);
        da.setUser(user);
        da.setAssignedAt(LocalDateTime.now().minusMonths(3));
        da.setStatus("ACTIVE");
        deviceAssignmentRepository.save(da);
    }

    private void createVulnerability(Device device, String cve, String title, String severity, LocalDateTime detectedAt) {
        Vulnerability v = new Vulnerability();
        v.setDevice(device);
        v.setCveId(cve);
        v.setTitle(title);
        v.setDescription(title);
        v.setSeverity(severity);
        v.setStatus("OPEN");
        v.setDetectedAt(detectedAt);
        vulnerabilityRepository.save(v);
    }

    private void createLicense(String name, String software, String publisher, int total, int avail, LocalDate expiryDate, String status) {
        License l = new License();
        l.setLicenseName(name);
        l.setSoftwareName(software);
        l.setPublisher(publisher);
        l.setTotalQuantity(total);
        l.setAvailableQuantity(avail);
        l.setPurchaseDate(LocalDate.now().minusYears(1));
        l.setStartDate(LocalDate.now().minusYears(1));
        l.setExpiryDate(expiryDate);
        l.setStatus(status);
        l.setCreatedAt(LocalDateTime.now().minusMonths(6));
        l.setUpdatedAt(LocalDateTime.now());
        licenseRepository.save(l);
    }

    private Role getOrCreateRole(String name, String description) {
        return roleRepository.findByRoleName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(name);
                    role.setDescription(description);
                    role.setCreatedAt(LocalDateTime.now());
                    return roleRepository.save(role);
                });
    }
}
