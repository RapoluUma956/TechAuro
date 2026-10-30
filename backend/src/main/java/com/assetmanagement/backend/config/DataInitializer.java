package com.assetmanagement.backend.config;

import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final LicenseRepository licenseRepository;
    private final LicenseAssignmentRepository licenseAssignmentRepository;
    private final AccessoryRepository accessoryRepository;

    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            HealthThresholdRepository healthThresholdRepository,
            DeviceRepository deviceRepository,
            DeviceAssignmentRepository deviceAssignmentRepository,
            LicenseRepository licenseRepository,
            LicenseAssignmentRepository licenseAssignmentRepository,
            AccessoryRepository accessoryRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.healthThresholdRepository = healthThresholdRepository;
        this.deviceRepository = deviceRepository;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.licenseRepository = licenseRepository;
        this.licenseAssignmentRepository = licenseAssignmentRepository;
        this.accessoryRepository = accessoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = getOrCreateRole("ADMIN", "System and IT Administrator");
        getOrCreateRole("EMPLOYEE", "Standard Organization Employee");

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
