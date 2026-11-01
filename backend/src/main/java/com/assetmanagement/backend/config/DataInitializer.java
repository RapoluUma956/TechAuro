package com.assetmanagement.backend.config;

import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final HealthThresholdRepository healthThresholdRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final LicenseRepository licenseRepository;
    private final LicenseAssignmentRepository licenseAssignmentRepository;
    private final AccessoryRepository accessoryRepository;
    private final EmployeeAccessAssignmentRepository employeeAccessAssignmentRepository;

    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder,
            HealthThresholdRepository healthThresholdRepository,
            DeviceRepository deviceRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            LicenseRepository licenseRepository,
            LicenseAssignmentRepository licenseAssignmentRepository,
            AccessoryRepository accessoryRepository,
            EmployeeAccessAssignmentRepository employeeAccessAssignmentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.healthThresholdRepository = healthThresholdRepository;
        this.deviceRepository = deviceRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.licenseRepository = licenseRepository;
        this.licenseAssignmentRepository = licenseAssignmentRepository;
        this.accessoryRepository = accessoryRepository;
        this.employeeAccessAssignmentRepository = employeeAccessAssignmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = getOrCreateRole("ADMIN", "System and IT Administrator", true);
        Role employeeRole = getOrCreateRole("EMPLOYEE", "Standard Organization Employee", false);
        Role itManagerRole = getOrCreateRole("IT_MANAGER", "IT Manager with Assigned Employee Access", false);

        // Bootstrap registered modules and role permissions
        initPermissionsAndRbac(adminRole, employeeRole, itManagerRole);

        // 1. Seed or ensure single default Admin user
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

        // Ensure Admin has role assigned
        if (userRoleRepository.findByUser(admin).isEmpty()) {
            UserRole adminUserRole = new UserRole();
            adminUserRole.setUser(admin);
            adminUserRole.setRole(adminRole);
            adminUserRole.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(adminUserRole);
        }

        // 1.1 Seed IT Manager if not present
        User itManager = userRepository.findByEmail("itmanager@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmployeeCode("MGR001");
            u.setFirstName("Sarah");
            u.setLastName("Connor");
            u.setEmail("itmanager@example.com");
            u.setPhone("+1 (415) 555-0102");
            u.setPasswordHash(passwordEncoder.encode("Manager@123"));
            u.setDesignation("IT Operations Manager");
            u.setAccountStatus("ACTIVE");
            u.setMustChangePassword(false);
            u.setDateOfJoining(LocalDate.now().minusMonths(8));
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            u = userRepository.save(u);

            UserRole mgrRole = new UserRole();
            mgrRole.setUser(u);
            mgrRole.setRole(itManagerRole);
            mgrRole.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(mgrRole);
            return u;
        });

        // 1.2 Seed Managed Employee (Alice)
        User alice = userRepository.findByEmail("employee@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmployeeCode("EMP002");
            u.setFirstName("Alice");
            u.setLastName("Smith");
            u.setEmail("employee@example.com");
            u.setPhone("+1 (415) 555-0103");
            u.setPasswordHash(passwordEncoder.encode("Employee@123"));
            u.setDesignation("Senior Software Engineer");
            u.setAccountStatus("ACTIVE");
            u.setMustChangePassword(false);
            u.setDateOfJoining(LocalDate.now().minusMonths(6));
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            u = userRepository.save(u);

            UserRole empRole = new UserRole();
            empRole.setUser(u);
            empRole.setRole(employeeRole);
            empRole.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(empRole);
            return u;
        });

        // 1.3 Seed Unmanaged Employee (Bob)
        User bob = userRepository.findByEmail("employee2@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmployeeCode("EMP003");
            u.setFirstName("Bob");
            u.setLastName("Jones");
            u.setEmail("employee2@example.com");
            u.setPhone("+1 (415) 555-0104");
            u.setPasswordHash(passwordEncoder.encode("Employee@123"));
            u.setDesignation("Data Analyst");
            u.setAccountStatus("ACTIVE");
            u.setMustChangePassword(false);
            u.setDateOfJoining(LocalDate.now().minusMonths(3));
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            u = userRepository.save(u);

            UserRole empRole = new UserRole();
            empRole.setUser(u);
            empRole.setRole(employeeRole);
            empRole.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(empRole);
            return u;
        });

        // 1.4 Assign Alice to IT Manager
        if (employeeAccessAssignmentRepository.findAssignedEmployeeIdsByManagerId(itManager.getUserId()).isEmpty()) {
            EmployeeAccessAssignment eaa = new EmployeeAccessAssignment(itManager, alice);
            employeeAccessAssignmentRepository.save(eaa);
        }

        // Ensure all existing users without an assigned role are mapped to the default EMPLOYEE role
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (userRoleRepository.findByUser(u).isEmpty()) {
                UserRole empUserRole = new UserRole();
                empUserRole.setUser(u);
                empUserRole.setRole(employeeRole);
                empUserRole.setAssignedAt(LocalDateTime.now());
                userRoleRepository.save(empUserRole);
            }
        }

        // 2. Seed Health Thresholds if not present
        if (healthThresholdRepository.count() == 0) {
            HealthThreshold threshold = new HealthThreshold(90, 90, 90, 5, 30, 30);
            threshold.setUpdatedAt(LocalDateTime.now());
            healthThresholdRepository.save(threshold);
        }

        // 3. Connect Admin with the live laptop (PF5N8G2C or any existing device)
        deviceRepository.findBySerialNumber("PF5N8G2C").ifPresent(device -> {
            boolean alreadyAssigned = deviceAssignmentRepository.findByUser_UserIdAndStatus(admin.getUserId(), "ACTIVE")
                    .stream()
                    .anyMatch(da -> device.getDeviceId().equals(da.getDevice().getDeviceId()));

            if (!alreadyAssigned) {
                DeviceAssignment da = new DeviceAssignment();
                da.setDevice(device);
                da.setUser(admin);
                da.setSerialNumber(device.getSerialNumber());
                da.setStatus("ACTIVE");
                da.setAssignedAt(LocalDateTime.now());
                deviceAssignmentRepository.save(da);

                device.setCurrentUserId(admin.getUserId());
                device.setDeviceStatus("ACTIVE");
                deviceRepository.save(device);
            }
        });

        // 4. Seed Accessories if empty
        if (accessoryRepository.count() == 0) {
            seedAccessories();
        }

        // 5. Seed Licenses if empty
        if (licenseRepository.count() == 0) {
            seedLicenses(admin);
        }
    }

    private Role getOrCreateRole(String name, String description, boolean isSystem) {
        return roleRepository.findByRoleName(name)
                .map(r -> {
                    boolean modified = false;
                    if (isSystem && !Boolean.TRUE.equals(r.getIsSystemRole())) {
                        r.setIsSystemRole(true);
                        modified = true;
                    }
                    if (modified) {
                        r.setUpdatedAt(LocalDateTime.now());
                        return roleRepository.save(r);
                    }
                    return r;
                })
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(name);
                    role.setDescription(description);
                    role.setIsSystemRole(isSystem);
                    role.setCreatedAt(LocalDateTime.now());
                    role.setUpdatedAt(LocalDateTime.now());
                    return roleRepository.save(role);
                });
    }

    private record ModuleDef(String code, String name, String description) {}

    private void initPermissionsAndRbac(Role adminRole, Role employeeRole, Role itManagerRole) {
        List<ModuleDef> modules = List.of(
                new ModuleDef("DASHBOARD", "Dashboard", "Overview dashboard with key asset metrics and status widgets"),
                new ModuleDef("EMPLOYEES_DEVICES", "Employees & Devices", "Manage employees, device assignments, and hardware details"),
                new ModuleDef("ACCESSORIES", "Accessories", "Manage peripheral assets and assignments"),
                new ModuleDef("LICENSE_MANAGEMENT", "License Management", "Manage software subscriptions, keys, and allocations"),
                new ModuleDef("NOTIFICATIONS_VULNERABILITIES", "Notifications & Security", "Security vulnerabilities and system alerts"),
                new ModuleDef("ROLE_PERMISSIONS", "Role Permissions", "Configure roles and module access privileges"),
                new ModuleDef("SETTINGS", "Settings", "Application preferences and configuration")
        );

        List<Permission> allPermissions = new ArrayList<>();
        for (ModuleDef mod : modules) {
            Permission perm = permissionRepository.findByPermissionCode(mod.code())
                    .or(() -> permissionRepository.findByPermissionName(mod.code()))
                    .orElseGet(() -> {
                        Permission p = new Permission();
                        p.setPermissionCode(mod.code());
                        p.setPermissionName(mod.code());
                        p.setModuleName(mod.name());
                        p.setDescription(mod.description());
                        p.setCreatedAt(LocalDateTime.now());
                        p.setUpdatedAt(LocalDateTime.now());
                        return permissionRepository.save(p);
                    });

            boolean changed = false;
            if (perm.getModuleName() == null || !perm.getModuleName().equals(mod.name())) {
                perm.setModuleName(mod.name());
                changed = true;
            }
            if (perm.getPermissionCode() == null || !perm.getPermissionCode().equals(mod.code())) {
                perm.setPermissionCode(mod.code());
                changed = true;
            }
            if (changed) {
                perm.setUpdatedAt(LocalDateTime.now());
                perm = permissionRepository.save(perm);
            }
            allPermissions.add(perm);
        }

        // 1. ADMIN role: WRITE access and ALL data scope on all application modules
        for (Permission perm : allPermissions) {
            RolePermission rp = rolePermissionRepository.findByRole_RoleIdAndPermission_PermissionId(adminRole.getRoleId(), perm.getPermissionId())
                    .orElseGet(() -> {
                        RolePermission newRp = new RolePermission();
                        newRp.setRole(adminRole);
                        newRp.setPermission(perm);
                        newRp.setAssignedAt(LocalDateTime.now());
                        return newRp;
                    });
            rp.setAccessLevel("WRITE");
            rp.setDataScope("ALL");
            rp.setUpdatedAt(LocalDateTime.now());
            rolePermissionRepository.save(rp);
        }

        // 2. EMPLOYEE role: READ on general modules, HIDE on admin modules, OWN data scope
        for (Permission perm : allPermissions) {
            String defaultLevel = switch (perm.getPermissionCode()) {
                case "ROLE_PERMISSIONS", "SETTINGS" -> "HIDE";
                default -> "READ";
            };
            RolePermission rp = rolePermissionRepository.findByRole_RoleIdAndPermission_PermissionId(employeeRole.getRoleId(), perm.getPermissionId())
                    .orElseGet(() -> {
                        RolePermission newRp = new RolePermission();
                        newRp.setRole(employeeRole);
                        newRp.setPermission(perm);
                        newRp.setAssignedAt(LocalDateTime.now());
                        return newRp;
                    });
            rp.setAccessLevel(defaultLevel);
            rp.setDataScope("OWN");
            rp.setUpdatedAt(LocalDateTime.now());
            rolePermissionRepository.save(rp);
        }

        // 3. IT_MANAGER role: READ on general modules, HIDE on admin modules, ASSIGNED data scope (DO NOT overwrite existing)
        for (Permission perm : allPermissions) {
            String defaultLevel = switch (perm.getPermissionCode()) {
                case "ROLE_PERMISSIONS", "SETTINGS" -> "HIDE";
                default -> "READ";
            };
            String defaultScope = switch (perm.getPermissionCode()) {
                case "ROLE_PERMISSIONS", "SETTINGS" -> "OWN";
                default -> "ASSIGNED";
            };
            Optional<RolePermission> existingRp = rolePermissionRepository.findByRole_RoleIdAndPermission_PermissionId(itManagerRole.getRoleId(), perm.getPermissionId());
            if (existingRp.isEmpty()) {
                RolePermission newRp = new RolePermission();
                newRp.setRole(itManagerRole);
                newRp.setPermission(perm);
                newRp.setAccessLevel(defaultLevel);
                newRp.setDataScope(defaultScope);
                newRp.setAssignedAt(LocalDateTime.now());
                newRp.setUpdatedAt(LocalDateTime.now());
                rolePermissionRepository.save(newRp);
            }
        }
    }

    private void seedAccessories() {
        createAccessoryItem("Wireless Mouse", "Mouse", "Wireless", "Logitech", "Logitech", "M185",
                "Compact wireless mouse with plug-and-play USB receiver", 40, 10, 2, LocalDate.now().minusMonths(4));

        createAccessoryItem("USB-C Headset", "Headset", "Wired USB-C", "Jabra", "Jabra", "Evolve 20",
                "Professional wired headset with inline call control unit", 18, 3, 1, LocalDate.now().minusMonths(6));

        createAccessoryItem("External Monitor", "Monitor", "Standard 24\"", "Dell", "Dell", "P2422H",
                "23.8-inch FHD IPS display with height-adjustable stand", 20, 3, 1, LocalDate.now().minusMonths(8));

        createAccessoryItem("Mechanical Keyboard", "Keyboard", "Wireless", "Logitech", "Logitech", "MX Mechanical",
                "Tactile quiet wireless mechanical illuminated keyboard", 30, 12, 0, LocalDate.now().minusMonths(3));

        createAccessoryItem("Thunderbolt 4 Dock", "Docking Station", "Thunderbolt 4", "CalDigit", "CalDigit", "TS4",
                "18-port Thunderbolt 4 dock with 98W host charging", 15, 5, 0, LocalDate.now().minusMonths(5));

        createAccessoryItem("65W USB-C GaN Charger", "Charger / Adapter", "USB-C 65W", "Anker", "Anker", "735 Charger",
                "Compact multi-port fast wall charger with PowerIQ 4.0", 25, 9, 0, LocalDate.now().minusMonths(2));
    }

    private void createAccessoryItem(String name, String category, String subCat, String brand, String manufacturer,
                                     String model, String desc, int total, int available, int damaged, LocalDate purchaseDate) {
        Accessory a = new Accessory();
        a.setAccessoryName(name);
        a.setProductName(name);
        a.setCategory(category);
        a.setSubCategory(subCat);
        a.setBrand(brand);
        a.setManufacturer(manufacturer);
        a.setModel(model);
        a.setDescription(desc);
        a.setTotalQuantity(total);
        a.setAvailableQuantity(available);
        a.setDamagedQuantity(damaged);
        a.setUnavailableQuantity(0);
        a.setPurchaseDate(purchaseDate);
        a.setCreatedAt(LocalDateTime.now().minusMonths(6));
        a.setUpdatedAt(LocalDateTime.now());
        accessoryRepository.save(a);
    }

    private void seedLicenses(User admin) {
        License m365 = createFullLicense("Microsoft 365 Business", "Microsoft 365", "Microsoft", "SUBSCRIPTION", "YEARLY", "AUTO", 100, 15, LocalDate.of(2026, 12, 15), "ACTIVE", "Business subscription");

        if (admin != null) {
            LicenseAssignment la = new LicenseAssignment();
            la.setLicense(m365);
            la.setUser(admin);
            la.setAssignedAt(LocalDateTime.now().minusDays(10));
            la.setStatus("ACTIVE");
            la.setNotes("Assigned for corporate administration");
            licenseAssignmentRepository.save(la);
        }

        createFullLicense("IntelliJ IDEA Commercial", "IntelliJ IDEA", "JetBrains", "SUBSCRIPTION", "YEARLY", "AUTO", 50, 2, LocalDate.of(2027, 1, 2), "ACTIVE", "Commercial development license");
        createFullLicense("Windows 11 Pro", "Windows 11", "Microsoft", "VOLUME", "ONE_TIME", "NOT_APPLICABLE", 200, 20, null, "ACTIVE", "Volume OEM license");
        createFullLicense("Adobe Acrobat Pro", "Acrobat Pro", "Adobe", "SUBSCRIPTION", "YEARLY", "AUTO", 30, 5, LocalDate.of(2026, 11, 20), "ACTIVE", "PDF and document editing");
        createFullLicense("Visual Studio Enterprise", "Visual Studio", "Microsoft", "SUBSCRIPTION", "YEARLY", "AUTO", 40, 2, LocalDate.now().plusDays(14), "ACTIVE", "Expiring enterprise suite");
        createFullLicense("AutoCAD", "AutoCAD", "Autodesk", "PERPETUAL", "ONE_TIME", "NOT_APPLICABLE", 25, 5, null, "ACTIVE", "Engineering drafting license");
        createFullLicense("Figma Organization", "Figma", "Figma", "SUBSCRIPTION", "MONTHLY", "AUTO", 25, 5, LocalDate.now().plusMonths(6), "ACTIVE", "UI/UX design team license");
        createFullLicense("Slack Enterprise Grid", "Slack", "Salesforce", "SUBSCRIPTION", "YEARLY", "AUTO", 50, 8, LocalDate.now().plusMonths(9), "ACTIVE", "Company messaging");
    }

    private License createFullLicense(String name, String software, String publisher, String type, String renewalType, String renewalMethod, int total, int avail, LocalDate expiryDate, String status, String notes) {
        License l = new License();
        l.setLicenseName(name);
        l.setSoftwareName(software);
        l.setPublisher(publisher);
        l.setLicenseType(type);
        l.setRenewalType(renewalType);
        l.setRenewalMethod(renewalMethod);
        l.setTotalQuantity(total);
        l.setTotalLicenses(total);
        l.setAvailableQuantity(avail);
        l.setPurchaseDate(LocalDate.now().minusMonths(6));
        l.setStartDate(LocalDate.now().minusMonths(6));
        l.setExpiryDate(expiryDate);
        l.setStatus(status);
        l.setNotes(notes);
        l.setCreatedAt(LocalDateTime.now().minusMonths(3));
        l.setUpdatedAt(LocalDateTime.now());
        return licenseRepository.save(l);
    }
}
