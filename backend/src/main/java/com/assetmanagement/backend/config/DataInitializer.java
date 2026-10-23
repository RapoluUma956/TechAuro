package com.assetmanagement.backend.config;

import com.assetmanagement.backend.entity.Role;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.entity.UserRole;
import com.assetmanagement.backend.repository.RoleRepository;
import com.assetmanagement.backend.repository.UserRepository;
import com.assetmanagement.backend.repository.UserRoleRepository;
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

    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = getOrCreateRole("ADMIN", "System and IT Administrator");
        Role employeeRole = getOrCreateRole("EMPLOYEE", "Standard Organization Employee");

        // Seed default Admin if not present
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setEmployeeCode("EMP001");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@example.com");
            admin.setPhone("+1 (415) 555-0101");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setDesignation("IT Administrator");
            admin.setAccountStatus("ACTIVE");
            admin.setMustChangePassword(false);
            admin.setDateOfJoining(LocalDate.now().minusYears(1));
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            admin = userRepository.save(admin);

            UserRole adminUserRole = new UserRole();
            adminUserRole.setUser(admin);
            adminUserRole.setRole(adminRole);
            adminUserRole.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(adminUserRole);
        }

        // Seed default Employee with mustChangePassword = true (for first-login reset testing)
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
