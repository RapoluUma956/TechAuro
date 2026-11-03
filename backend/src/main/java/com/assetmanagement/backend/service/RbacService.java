package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.rbac.RbacDtos.*;
import com.assetmanagement.backend.entity.*;
import com.assetmanagement.backend.repository.*;
import com.assetmanagement.backend.security.AuthorizedScope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final EmployeeAccessAssignmentRepository employeeAccessAssignmentRepository;

    public RbacService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            EmployeeAccessAssignmentRepository employeeAccessAssignmentRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.employeeAccessAssignmentRepository = employeeAccessAssignmentRepository;
    }

    // =========================================================================
    // 1. ROLES MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public List<RoleSummaryDto> getAllRoles(String search) {
        List<Role> roles = roleRepository.findAll();
        if (search != null && !search.isBlank()) {
            String query = search.trim().toLowerCase();
            roles = roles.stream()
                    .filter(r -> r.getRoleName().toLowerCase().contains(query)
                            || (r.getDescription() != null && r.getDescription().toLowerCase().contains(query)))
                    .toList();
        }

        return roles.stream().map(role -> {
            long count = userRoleRepository.countByRole_RoleId(role.getRoleId());
            return new RoleSummaryDto(
                    role.getRoleId(),
                    role.getRoleName(),
                    role.getDescription(),
                    Boolean.TRUE.equals(role.getIsSystemRole()),
                    count,
                    role.getCreatedAt(),
                    role.getUpdatedAt()
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public RoleSummaryDto getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + roleId));

        long count = userRoleRepository.countByRole_RoleId(role.getRoleId());
        return new RoleSummaryDto(
                role.getRoleId(),
                role.getRoleName(),
                role.getDescription(),
                Boolean.TRUE.equals(role.getIsSystemRole()),
                count,
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    @Transactional
    public RoleSummaryDto createRole(CreateRoleRequest request) {
        String roleName = request.roleName() != null ? request.roleName().trim().toUpperCase() : "";
        if (roleName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name is required");
        }

        if (roleRepository.existsByRoleNameIgnoreCase(roleName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A role with name '" + roleName + "' already exists");
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(request.description() != null ? request.description().trim() : "");
        role.setIsSystemRole(false);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        Role savedRole = roleRepository.save(role);

        // For newly created roles, initialize all module permissions to HIDE and OWN by default
        List<Permission> allPermissions = permissionRepository.findAll();
        for (Permission perm : allPermissions) {
            RolePermission rp = new RolePermission();
            rp.setRole(savedRole);
            rp.setPermission(perm);
            rp.setAccessLevel("HIDE");
            rp.setDataScope("OWN");
            rp.setAssignedAt(LocalDateTime.now());
            rp.setUpdatedAt(LocalDateTime.now());
            rolePermissionRepository.save(rp);
        }

        return new RoleSummaryDto(
                savedRole.getRoleId(),
                savedRole.getRoleName(),
                savedRole.getDescription(),
                false,
                0,
                savedRole.getCreatedAt(),
                savedRole.getUpdatedAt()
        );
    }

    @Transactional
    public RoleSummaryDto updateRole(Long roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + roleId));

        String newRoleName = request.roleName() != null ? request.roleName().trim().toUpperCase() : "";
        if (newRoleName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name is required");
        }

        // If system role, prevent changing its role name
        if (Boolean.TRUE.equals(role.getIsSystemRole()) && !role.getRoleName().equalsIgnoreCase(newRoleName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "System role names cannot be modified");
        }

        if (!role.getRoleName().equalsIgnoreCase(newRoleName) && roleRepository.existsByRoleNameIgnoreCase(newRoleName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A role with name '" + newRoleName + "' already exists");
        }

        role.setRoleName(newRoleName);
        role.setDescription(request.description() != null ? request.description().trim() : "");
        role.setUpdatedAt(LocalDateTime.now());
        Role updated = roleRepository.save(role);

        long assignedCount = userRoleRepository.countByRole_RoleId(updated.getRoleId());
        return new RoleSummaryDto(
                updated.getRoleId(),
                updated.getRoleName(),
                updated.getDescription(),
                Boolean.TRUE.equals(updated.getIsSystemRole()),
                assignedCount,
                updated.getCreatedAt(),
                updated.getUpdatedAt()
        );
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + roleId));

        // Protect system role / ADMIN role from deletion
        if (Boolean.TRUE.equals(role.getIsSystemRole()) || "ADMIN".equalsIgnoreCase(role.getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Protected system ADMIN role cannot be deleted");
        }

        // Prevent deletion if employees are assigned
        long assignedCount = userRoleRepository.countByRole_RoleId(roleId);
        if (assignedCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete role '" + role.getRoleName() + "': It is currently assigned to " + assignedCount + " employee(s).");
        }

        // Clean up role_permissions then delete role
        rolePermissionRepository.deleteByRole_RoleId(roleId);
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public List<AssignedEmployeeDto> getAssignedEmployees(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + roleId));

        List<UserRole> userRoles = userRoleRepository.findByRole_RoleId(roleId);
        return userRoles.stream().map(ur -> {
            User u = ur.getUser();
            String fullName = Stream.of(u.getFirstName(), u.getLastName())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "))
                    .trim();
            if (fullName.isBlank()) {
                fullName = u.getFirstName() != null ? u.getFirstName() : "User #" + u.getUserId();
            }
            return new AssignedEmployeeDto(
                    u.getUserId(),
                    u.getEmployeeCode(),
                    fullName,
                    u.getEmail(),
                    u.getDesignation() != null ? u.getDesignation() : "Employee",
                    u.getAccountStatus() != null ? u.getAccountStatus() : "ACTIVE",
                    ur.getAssignedAt()
            );
        }).toList();
    }

    // =========================================================================
    // 2. PERMISSIONS MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAllByOrderByPermissionIdAsc().stream()
                .map(p -> new PermissionDto(
                        p.getPermissionId(),
                        p.getModuleName(),
                        p.getPermissionCode(),
                        p.getDescription()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public RolePermissionMatrixResponse getRolePermissionMatrix(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + roleId));

        List<Permission> allPermissions = permissionRepository.findAllByOrderByPermissionIdAsc();
        List<RolePermission> assignedPermissions = rolePermissionRepository.findByRole_RoleId(roleId);
        Map<Long, RolePermission> assignedMap = assignedPermissions.stream()
                .collect(Collectors.toMap(
                        rp -> rp.getPermission().getPermissionId(),
                        rp -> rp,
                        (existing, replacement) -> replacement
                ));

        List<RolePermissionDto> dtoList = allPermissions.stream().map(perm -> {
            RolePermission rp = assignedMap.get(perm.getPermissionId());
            String level = rp != null ? rp.getAccessLevel() : "HIDE";
            String scope = rp != null ? rp.getDataScope() : "OWN";

            // System ADMIN role always has WRITE access and ALL data scope
            if ("ADMIN".equalsIgnoreCase(role.getRoleName())) {
                level = "WRITE";
                scope = "ALL";
            }

            return new RolePermissionDto(
                    perm.getPermissionId(),
                    perm.getModuleName(),
                    perm.getPermissionCode(),
                    perm.getDescription(),
                    level,
                    scope
            );
        }).toList();

        return new RolePermissionMatrixResponse(
                role.getRoleId(),
                role.getRoleName(),
                role.getDescription(),
                Boolean.TRUE.equals(role.getIsSystemRole()),
                dtoList
        );
    }

    @Transactional
    public RolePermissionMatrixResponse updateRolePermissions(Long roleId, UpdateRolePermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + roleId));

        // ADMIN role must have WRITE access on all modules and cannot be downgraded
        if ("ADMIN".equalsIgnoreCase(role.getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permissions for the system ADMIN role cannot be modified; ADMIN always retains full WRITE access.");
        }

        if (request.permissions() != null) {
            for (UpdatePermissionItemRequest item : request.permissions()) {
                String rawLevel = item.accessLevel() != null ? item.accessLevel().trim().toUpperCase() : "HIDE";
                if (!List.of("HIDE", "READ", "WRITE").contains(rawLevel)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid access level: " + item.accessLevel() + ". Must be HIDE, READ, or WRITE.");
                }

                String rawScope = item.dataScope() != null ? item.dataScope().trim().toUpperCase() : "OWN";
                if (!List.of("ALL", "OWN", "ASSIGNED").contains(rawScope)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data scope: " + item.dataScope() + ". Must be ALL, OWN, or ASSIGNED.");
                }

                Permission perm = permissionRepository.findById(item.permissionId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found with ID: " + item.permissionId()));

                RolePermission rp = rolePermissionRepository.findByRole_RoleIdAndPermission_PermissionId(roleId, perm.getPermissionId())
                        .orElseGet(() -> {
                            RolePermission newRp = new RolePermission();
                            newRp.setRole(role);
                            newRp.setPermission(perm);
                            newRp.setAssignedAt(LocalDateTime.now());
                            return newRp;
                        });

                rp.setAccessLevel(rawLevel);
                rp.setDataScope(rawScope);
                rp.setUpdatedAt(LocalDateTime.now());
                rolePermissionRepository.save(rp);
            }
        }

        return getRolePermissionMatrix(roleId);
    }

    // =========================================================================
    // 3. USER ROLE ASSIGNMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public UserRolesResponse getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        List<UserRole> userRoles = userRoleRepository.findByUser_UserId(userId);
        List<String> roleNames = userRoles.stream().map(ur -> ur.getRole().getRoleName()).toList();
        List<Long> roleIds = userRoles.stream().map(ur -> ur.getRole().getRoleId()).toList();

        return new UserRolesResponse(userId, roleNames, roleIds);
    }

    @Transactional
    public UserRolesResponse assignUserRole(Long userId, AssignUserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        List<Long> targetRoleIds = new ArrayList<>();
        if (request.roleId() != null) {
            targetRoleIds.add(request.roleId());
        }
        if (request.roleIds() != null) {
            targetRoleIds.addAll(request.roleIds());
        }

        if (targetRoleIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one role ID must be provided");
        }

        // Delete existing user roles
        userRoleRepository.deleteByUser_UserId(userId);

        for (Long roleId : targetRoleIds.stream().distinct().toList()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + roleId));

            UserRole ur = new UserRole();
            ur.setUser(user);
            ur.setRole(role);
            ur.setAssignedAt(LocalDateTime.now());
            userRoleRepository.save(ur);
        }

        return getUserRoles(userId);
    }

    // =========================================================================
    // 4. EFFECTIVE PERMISSIONS & DATA SCOPES RESOLUTION
    // =========================================================================

    @Transactional(readOnly = true)
    public Map<String, Object> getEffectivePermissions(User user) {
        Map<String, Object> permissionsMap = new LinkedHashMap<>();

        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        boolean isAdmin = userRoles.stream()
                .anyMatch(ur -> "ADMIN".equalsIgnoreCase(ur.getRole().getRoleName()));

        List<Permission> allPermissions = permissionRepository.findAllByOrderByPermissionIdAsc();

        if (isAdmin) {
            for (Permission p : allPermissions) {
                permissionsMap.put(p.getPermissionCode(), "WRITE");
            }
            return permissionsMap;
        }

        // Aggregate permissions across all roles assigned to user
        List<Role> roles = userRoles.stream().map(UserRole::getRole).toList();
        if (roles.isEmpty()) {
            roleRepository.findByRoleName("EMPLOYEE").ifPresent(roles::add);
        }

        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIn(roles);
        Map<String, List<String>> codeToLevels = new HashMap<>();
        for (RolePermission rp : rolePermissions) {
            String code = rp.getPermission().getPermissionCode();
            codeToLevels.computeIfAbsent(code, k -> new ArrayList<>()).add(rp.getAccessLevel());
        }

        for (Permission p : allPermissions) {
            List<String> levels = codeToLevels.getOrDefault(p.getPermissionCode(), List.of("HIDE"));
            String effective = "HIDE";
            if (levels.contains("WRITE")) {
                effective = "WRITE";
            } else if (levels.contains("READ")) {
                effective = "READ";
            }
            permissionsMap.put(p.getPermissionCode(), effective);
        }

        return permissionsMap;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getEffectiveDataScopes(User user) {
        Map<String, String> scopeMap = new LinkedHashMap<>();

        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        boolean isAdmin = userRoles.stream()
                .anyMatch(ur -> "ADMIN".equalsIgnoreCase(ur.getRole().getRoleName()));

        List<Permission> allPermissions = permissionRepository.findAllByOrderByPermissionIdAsc();

        if (isAdmin) {
            for (Permission p : allPermissions) {
                scopeMap.put(p.getPermissionCode(), "ALL");
            }
            return scopeMap;
        }

        List<Role> roles = userRoles.stream().map(UserRole::getRole).toList();
        if (roles.isEmpty()) {
            roleRepository.findByRoleName("EMPLOYEE").ifPresent(roles::add);
        }

        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIn(roles);
        Map<String, List<String>> codeToScopes = new HashMap<>();
        for (RolePermission rp : rolePermissions) {
            String code = rp.getPermission().getPermissionCode();
            codeToScopes.computeIfAbsent(code, k -> new ArrayList<>()).add(rp.getDataScope());
        }

        for (Permission p : allPermissions) {
            List<String> scopes = codeToScopes.getOrDefault(p.getPermissionCode(), List.of("OWN"));
            // Precedence: ALL > ASSIGNED > OWN
            String effective = "OWN";
            if (scopes.contains("ALL")) {
                effective = "ALL";
            } else if (scopes.contains("ASSIGNED")) {
                effective = "ASSIGNED";
            }
            scopeMap.put(p.getPermissionCode(), effective);
        }

        return scopeMap;
    }

    @Transactional(readOnly = true)
    public String getEffectiveScope(User user, String permissionCode) {
        Map<String, String> scopes = getEffectiveDataScopes(user);
        return scopes.getOrDefault(permissionCode, "OWN");
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(User user, String permissionCode, String requiredLevel) {
        Map<String, Object> perms = getEffectivePermissions(user);
        Object levelObj = perms.get(permissionCode);
        String level = levelObj != null ? levelObj.toString() : "HIDE";

        if ("WRITE".equalsIgnoreCase(level)) {
            return true;
        }
        if ("READ".equalsIgnoreCase(level)) {
            return !"WRITE".equalsIgnoreCase(requiredLevel);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Set<Long> getAuthorizedEmployeeIds(User user, String moduleCode) {
        if (user == null) {
            return Collections.emptySet();
        }

        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        boolean isAdmin = userRoles.stream()
                .anyMatch(ur -> "ADMIN".equalsIgnoreCase(ur.getRole().getRoleName()));
        if (isAdmin) {
            return null; // null indicates ALL employees permitted (no query filter needed)
        }

        String scope = getEffectiveScope(user, moduleCode);
        if ("ALL".equalsIgnoreCase(scope)) {
            return null; // unrestricted
        }

        if ("ASSIGNED".equalsIgnoreCase(scope)) {
            List<Long> assignedIds = employeeAccessAssignmentRepository.findAssignedEmployeeIdsByManagerId(user.getUserId());
            Set<Long> set = new HashSet<>(assignedIds);
            set.add(user.getUserId()); // Managers can always see their own record too
            return set;
        }

        // Default OWN scope: only their own user ID
        return Set.of(user.getUserId());
    }

    @Transactional(readOnly = true)
    public AuthorizedScope getAuthorizedScope(User user, String moduleCode) {
        if (user == null) {
            return AuthorizedScope.empty();
        }

        Map<String, Object> perms = getEffectivePermissions(user);
        String accessLevel = String.valueOf(perms.getOrDefault(moduleCode, "HIDE"));
        if ("HIDE".equalsIgnoreCase(accessLevel)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: You do not have permission to access the " + moduleCode + " module.");
        }

        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        boolean isAdmin = userRoles.stream()
                .anyMatch(ur -> "ADMIN".equalsIgnoreCase(ur.getRole().getRoleName()));
        if (isAdmin) {
            return AuthorizedScope.all();
        }

        String scope = getEffectiveScope(user, moduleCode);
        if ("ALL".equalsIgnoreCase(scope)) {
            return AuthorizedScope.all();
        }

        if ("ASSIGNED".equalsIgnoreCase(scope)) {
            List<Long> assignedIds = employeeAccessAssignmentRepository.findAssignedEmployeeIdsByManagerId(user.getUserId());
            Set<Long> set = new HashSet<>(assignedIds);
            set.add(user.getUserId()); // Managers can always see their own records too
            return AuthorizedScope.of(set);
        }

        // Default OWN scope
        return AuthorizedScope.single(user.getUserId());
    }

    // =========================================================================
    // 5. EMPLOYEE - MANAGER ACCESS ASSIGNMENTS
    // =========================================================================

    @Transactional
    public ManagerAssignmentSummaryDto assignEmployeesToManager(Long managerUserId, AssignManagerEmployeesRequest request) {
        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager user not found with ID: " + managerUserId));

        if (!"ACTIVE".equalsIgnoreCase(manager.getAccountStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign employees to an inactive manager.");
        }

        // Requirement: Validate manager role membership
        List<UserRole> managerRoles = userRoleRepository.findByUser(manager);
        boolean isEligibleManager = managerRoles.stream()
                .anyMatch(ur -> "IT_MANAGER".equalsIgnoreCase(ur.getRole().getRoleName()) || "ADMIN".equalsIgnoreCase(ur.getRole().getRoleName()));
        if (!isEligibleManager) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user '" + manager.getEmail() + "' does not have an IT Manager or Administrator role.");
        }

        List<Long> employeeIds = request != null && request.employeeUserIds() != null
                ? request.employeeUserIds().stream().distinct().toList()
                : Collections.emptyList();

        // Requirement: Validate that manager_user_id differs from employee_user_id
        for (Long empId : employeeIds) {
            if (Objects.equals(managerUserId, empId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A manager cannot be assigned to manage themselves.");
            }
            User emp = userRepository.findById(empId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee user not found with ID: " + empId));
            if (!"ACTIVE".equalsIgnoreCase(emp.getAccountStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee '" + emp.getEmail() + "' is inactive and cannot be assigned.");
            }
        }

        // Atomically replace manager assignments
        employeeAccessAssignmentRepository.deleteByManager_UserId(managerUserId);

        for (Long empId : employeeIds) {
            User emp = userRepository.getReferenceById(empId);
            EmployeeAccessAssignment eaa = new EmployeeAccessAssignment(manager, emp);
            employeeAccessAssignmentRepository.save(eaa);
        }

        return getManagerAssignmentSummary(managerUserId);
    }

    @Transactional(readOnly = true)
    public ManagerAssignmentSummaryDto getManagerAssignmentSummary(Long managerUserId) {
        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager user not found with ID: " + managerUserId));

        List<EmployeeAccessAssignment> assignments = employeeAccessAssignmentRepository.findByManager_UserId(managerUserId);
        List<AssignedEmployeeDto> employeeDtos = assignments.stream().map(eaa -> {
            User u = eaa.getEmployee();
            String fullName = Stream.of(u.getFirstName(), u.getLastName())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "))
                    .trim();
            if (fullName.isBlank()) {
                fullName = u.getFirstName() != null ? u.getFirstName() : "User #" + u.getUserId();
            }
            return new AssignedEmployeeDto(
                    u.getUserId(),
                    u.getEmployeeCode(),
                    fullName,
                    u.getEmail(),
                    u.getDesignation() != null ? u.getDesignation() : "Employee",
                    u.getAccountStatus() != null ? u.getAccountStatus() : "ACTIVE",
                    eaa.getCreatedAt()
            );
        }).toList();

        String managerName = Stream.of(manager.getFirstName(), manager.getLastName())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();

        return new ManagerAssignmentSummaryDto(
                manager.getUserId(),
                managerName,
                manager.getEmail(),
                employeeDtos.size(),
                employeeDtos
        );
    }

    @Transactional(readOnly = true)
    public List<AssignedEmployeeDto> getAssignedEmployeesForManager(Long managerUserId) {
        return getManagerAssignmentSummary(managerUserId).assignedEmployees();
    }
}
