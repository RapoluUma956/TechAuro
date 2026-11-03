package com.assetmanagement.backend.security;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.DeviceAssignment;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.repository.DeviceAssignmentRepository;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.UserRepository;
import com.assetmanagement.backend.service.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class AuthorizationService {

    private final UserRepository userRepository;
    private final RbacService rbacService;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final DeviceRepository deviceRepository;

    public AuthorizationService(
            UserRepository userRepository,
            RbacService rbacService,
            DeviceAssignmentRepository deviceAssignmentRepository,
            DeviceRepository deviceRepository) {
        this.userRepository = userRepository;
        this.rbacService = rbacService;
        this.deviceAssignmentRepository = deviceAssignmentRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * Resolves the authenticated user from the current HTTP request context.
     */
    public User getCurrentUser() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            Object userObj = request.getAttribute("currentUser");
            if (userObj instanceof User u) {
                return u;
            }
            Object userIdObj = request.getAttribute("currentUserId");
            if (userIdObj instanceof Long uid) {
                return userRepository.findById(uid).orElse(null);
            }
        }
        return null;
    }

    /**
     * Ensures an authenticated user exists in the current request; throws 401 if missing.
     */
    public User requireCurrentUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated request. Please log in.");
        }
        return user;
    }

    /**
     * Resolves the authorized scope for the current user and requested module.
     * Enforces that the module is not HIDE.
     */
    public AuthorizedScope getAuthorizedScope(String moduleCode) {
        User user = requireCurrentUser();
        return rbacService.getAuthorizedScope(user, moduleCode);
    }

    /**
     * Resolves the authorized scope for a specific user and requested module.
     */
    public AuthorizedScope getAuthorizedScope(User user, String moduleCode) {
        if (user == null) {
            return AuthorizedScope.empty();
        }
        return rbacService.getAuthorizedScope(user, moduleCode);
    }

    /**
     * Returns the configured effective data scope string ("ALL", "ASSIGNED", or "OWN")
     * for the authenticated user on the requested module.
     */
    public String getEffectiveDataScope(String moduleCode) {
        User user = requireCurrentUser();
        return rbacService.getEffectiveScope(user, moduleCode);
    }

    /**
     * Returns the set of accessible employee IDs for the authenticated user on the module.
     * Returns null if access is unrestricted (ALL scope), or an exact Set<Long> for OWN/ASSIGNED.
     */
    public Set<Long> getAuthorizedEmployeeIds(String moduleCode) {
        User user = requireCurrentUser();
        return rbacService.getAuthorizedEmployeeIds(user, moduleCode);
    }

    /**
     * Validates that the current user has at least the required access level (READ or WRITE)
     * on the target module.
     */
    public void validateAccess(String moduleCode, String requiredLevel) {
        User user = requireCurrentUser();
        Map<String, Object> perms = rbacService.getEffectivePermissions(user);
        String currentLevel = String.valueOf(perms.getOrDefault(moduleCode, "HIDE"));

        if ("HIDE".equalsIgnoreCase(currentLevel)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: You do not have permission to access the " + moduleCode + " module.");
        }

        if ("WRITE".equalsIgnoreCase(requiredLevel) && !"WRITE".equalsIgnoreCase(currentLevel)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: Write operation not permitted for module " + moduleCode + ". Read-only access granted.");
        }
    }

    /**
     * Validates that the current user can access the specific employee ID under the requested module and access level.
     * Prevents IDOR vulnerabilities.
     */
    public AuthorizedScope validateEmployeeAccess(Long targetUserId, String moduleCode, String requiredLevel) {
        validateAccess(moduleCode, requiredLevel);
        AuthorizedScope scope = getAuthorizedScope(moduleCode);

        if (!scope.canAccess(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: You are not authorized to view or manage employee ID " + targetUserId + ".");
        }
        return scope;
    }

    /**
     * Resolves the device IDs belonging to the given AuthorizedScope.
     * Returns null if scope is ALL (unrestricted).
     * Returns empty set if scope is empty or has no assigned devices.
     */
    public Set<Long> resolveAuthorizedDeviceIds(AuthorizedScope scope) {
        if (scope == null || scope.isEmpty()) {
            return Collections.emptySet();
        }
        if (scope.isAll()) {
            return null; // unrestricted
        }

        Set<Long> userIds = scope.employeeIds();
        Set<Long> deviceIds = new HashSet<>();

        // 1. Through active device assignments
        List<Long> assignedDeviceIds = deviceAssignmentRepository.findDeviceIdsByUser_UserIdInAndStatus(userIds, "ACTIVE");
        if (assignedDeviceIds != null) {
            deviceIds.addAll(assignedDeviceIds);
        }

        // 2. Through currentUserId on device
        List<Long> directDeviceIds = deviceRepository.findDeviceIdsByCurrentUserIdIn(userIds);
        if (directDeviceIds != null) {
            deviceIds.addAll(directDeviceIds);
        }

        return deviceIds;
    }

    /**
     * Validates access to a specific device ID under the requested module and level.
     */
    public void validateDeviceAccess(Long deviceId, String moduleCode, String requiredLevel) {
        validateAccess(moduleCode, requiredLevel);
        AuthorizedScope scope = getAuthorizedScope(moduleCode);

        if (scope.isAll()) {
            return; // unrestricted
        }

        Set<Long> allowedDeviceIds = resolveAuthorizedDeviceIds(scope);
        if (allowedDeviceIds == null || !allowedDeviceIds.contains(deviceId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: You are not authorized to access device ID " + deviceId + ".");
        }
    }
}

