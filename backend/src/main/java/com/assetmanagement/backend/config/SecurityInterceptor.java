package com.assetmanagement.backend.config;

import com.assetmanagement.backend.dto.ApiResponse;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.exception.AuthException;
import com.assetmanagement.backend.repository.UserRepository;
import com.assetmanagement.backend.security.JwtClaims;
import com.assetmanagement.backend.security.JwtService;
import com.assetmanagement.backend.service.RbacService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RbacService rbacService;
    private final ObjectMapper objectMapper;

    public SecurityInterceptor(
            JwtService jwtService,
            UserRepository userRepository,
            RbacService rbacService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.rbacService = rbacService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS pre-flight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // Allow public authentication endpoints and agent telemetry reporting
        if (path.startsWith("/api/auth/") || path.startsWith("/api/agent/") ||
            path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/agent/")) {
            return true;
        }

        // Enforce token validation on protected application endpoints
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
            return false;
        }

        String token = authHeader.substring(7).trim();
        JwtClaims claims;
        try {
            claims = jwtService.validateAndParse(token);
        } catch (AuthException e) {
            writeErrorResponse(response, e.getStatus(), e.getMessage());
            return false;
        } catch (Exception e) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
            return false;
        }

        // Independently prevent users with pending first-login reset from accessing protected modules
        if (claims.passwordChangeRequired()) {
            writeErrorResponse(response, HttpStatus.FORBIDDEN, "Password reset required before accessing this resource.");
            return false;
        }

        // Verify user exists and is active in database
        User currentUser = userRepository.findById(claims.userId())
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getAccountStatus()))
                .orElse(null);

        if (currentUser == null) {
            writeErrorResponse(response, HttpStatus.FORBIDDEN, "Your account is inactive. Please contact your administrator.");
            return false;
        }

        request.setAttribute("currentUserId", claims.userId());
        request.setAttribute("currentUser", currentUser);

        // RBAC Dynamic Authorization Enforcement
        String targetModule = resolveModuleForPath(path);
        if (targetModule != null) {
            String method = request.getMethod().toUpperCase();
            Map<String, Object> perms = rbacService.getEffectivePermissions(currentUser);
            String level = String.valueOf(perms.getOrDefault(targetModule, "HIDE"));

            if ("HIDE".equalsIgnoreCase(level)) {
                writeErrorResponse(response, HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to access the " + targetModule + " module.");
                return false;
            }

            if ("READ".equalsIgnoreCase(level) && !List.of("GET", "HEAD", "OPTIONS").contains(method)) {
                writeErrorResponse(response, HttpStatus.FORBIDDEN, "Access Denied: Read-only access permitted for " + targetModule + ". Write actions (create, edit, delete) are restricted.");
                return false;
            }
        }

        return true;
    }

    private String resolveModuleForPath(String rawPath) {
        String path = rawPath.replace("/api/v1/", "/api/");
        if (path.startsWith("/api/roles") || path.startsWith("/api/permissions")) {
            return "ROLE_PERMISSIONS";
        }
        if (path.startsWith("/api/managers/me")) {
            return "EMPLOYEES_DEVICES";
        }
        if (path.startsWith("/api/managers")) {
            return "ROLE_PERMISSIONS";
        }
        if (path.startsWith("/api/employees") || path.startsWith("/api/devices") || path.startsWith("/api/users")) {
            if (path.contains("/roles")) {
                return "ROLE_PERMISSIONS";
            }
            return "EMPLOYEES_DEVICES";
        }
        if (path.startsWith("/api/accessories")) {
            return "ACCESSORIES";
        }
        if (path.startsWith("/api/licenses")) {
            return "LICENSE_MANAGEMENT";
        }
        if (path.startsWith("/api/notifications") || path.startsWith("/api/vulnerabilities")) {
            return "NOTIFICATIONS_VULNERABILITIES";
        }
        if (path.startsWith("/api/dashboard")) {
            return "DASHBOARD";
        }
        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
