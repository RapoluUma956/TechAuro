package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.FieldErrorResponse;
import com.assetmanagement.backend.dto.auth.*;
import com.assetmanagement.backend.entity.Role;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.entity.UserRole;
import com.assetmanagement.backend.exception.AuthException;
import com.assetmanagement.backend.repository.RoleRepository;
import com.assetmanagement.backend.repository.UserRepository;
import com.assetmanagement.backend.repository.UserRoleRepository;
import com.assetmanagement.backend.security.CsrfService;
import com.assetmanagement.backend.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_COOKIE_PATH = "/api/auth";
    private static final long REFRESH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60; // 7 days

    // Minimum 12 characters, at least 1 uppercase, 1 lowercase, 1 digit, 1 special character
    private static final Pattern PASSWORD_POLICY = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^_-])[A-Za-z\\d@$!%*?&#^_-]{12,}$"
    );

    // Rate limiting tracker: IP/email -> attempts and window expiry
    private final Map<String, LoginAttemptRecord> attemptTracker = new ConcurrentHashMap<>();

    // Active refresh tokens store: refreshToken -> RefreshSession(userId, expiry)
    private final Map<String, RefreshSession> refreshSessions = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CsrfService csrfService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CsrfService csrfService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.csrfService = csrfService;
    }

    public CsrfResponseData getCsrfToken() {
        String token = csrfService.generateToken();
        return new CsrfResponseData(token);
    }

    @Transactional
    public LoginResponseData login(LoginRequest request, String clientIp, HttpServletResponse response) {
        String email = request.email() != null ? request.email().trim().toLowerCase() : "";
        String rateLimitKey = clientIp + ":" + email;

        checkRateLimit(rateLimitKey);

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.password(), userOpt.get().getPasswordHash())) {
            recordFailedAttempt(rateLimitKey);
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User user = userOpt.get();

        if (!"ACTIVE".equalsIgnoreCase(user.getAccountStatus())) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Your account is inactive. Please contact your administrator.");
        }

        // Reset rate limit on successful credentials match
        attemptTracker.remove(rateLimitKey);

        List<String> roles = getUserRoles(user);
        boolean passwordChangeRequired = Boolean.TRUE.equals(user.getMustChangePassword());

        String accessToken = jwtService.generateAccessToken(user, roles, passwordChangeRequired);
        long expiresIn = passwordChangeRequired ? 300 : 900;

        // Create refresh token session and set HttpOnly cookie
        String refreshToken = jwtService.generateRefreshToken();
        long refreshExpiry = System.currentTimeMillis() + (REFRESH_TOKEN_TTL_SECONDS * 1000L);
        refreshSessions.put(refreshToken, new RefreshSession(user.getUserId(), refreshExpiry));
        setRefreshCookie(response, refreshToken, REFRESH_TOKEN_TTL_SECONDS);

        if (!passwordChangeRequired) {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }

        UserAuthDto userAuthDto = toUserAuthDto(user, roles);
        return new LoginResponseData(accessToken, "Bearer", expiresIn, passwordChangeRequired, userAuthDto);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, User currentUser, HttpServletResponse response) {
        List<FieldErrorResponse> fieldErrors = new ArrayList<>();

        if (request.currentPassword() == null || request.currentPassword().isBlank()) {
            fieldErrors.add(new FieldErrorResponse("currentPassword", "Current password is required."));
        }
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            fieldErrors.add(new FieldErrorResponse("newPassword", "New password is required."));
        }
        if (request.confirmPassword() == null || request.confirmPassword().isBlank()) {
            fieldErrors.add(new FieldErrorResponse("confirmPassword", "Confirm password is required."));
        }

        if (!fieldErrors.isEmpty()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Please correct the highlighted fields", fieldErrors);
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            fieldErrors.add(new FieldErrorResponse("confirmPassword", "New password and confirm password do not match"));
            throw new AuthException(HttpStatus.BAD_REQUEST, "Please correct the highlighted fields", fieldErrors);
        }

        if (!PASSWORD_POLICY.matcher(request.newPassword()).matches()) {
            fieldErrors.add(new FieldErrorResponse("newPassword",
                    "Password must be at least 12 characters and include uppercase, lowercase, numbers, and special characters."));
            throw new AuthException(HttpStatus.BAD_REQUEST, "New password does not meet the password requirements.", fieldErrors);
        }

        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPasswordHash())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Current password is incorrect.");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        currentUser.setMustChangePassword(false);
        currentUser.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(currentUser);

        // Invalidate all refresh sessions for this user to require fresh login
        refreshSessions.entrySet().removeIf(entry -> entry.getValue().userId().equals(currentUser.getUserId()));
        clearRefreshCookie(response);
    }

    @Transactional(readOnly = true)
    public RefreshResponseData refresh(String refreshTokenCookie, HttpServletResponse response) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            clearRefreshCookie(response);
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        RefreshSession session = refreshSessions.get(refreshTokenCookie);
        if (session == null || System.currentTimeMillis() > session.expiry()) {
            refreshSessions.remove(refreshTokenCookie);
            clearRefreshCookie(response);
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        User user = userRepository.findById(session.userId()).orElse(null);
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getAccountStatus())) {
            refreshSessions.remove(refreshTokenCookie);
            clearRefreshCookie(response);
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        List<String> roles = getUserRoles(user);
        boolean passwordChangeRequired = Boolean.TRUE.equals(user.getMustChangePassword());

        // "If the user's mandatory password reset is still pending, refresh must not issue a normal access token."
        String newAccessToken = jwtService.generateAccessToken(user, roles, passwordChangeRequired);
        long expiresIn = passwordChangeRequired ? 300 : 900;

        return new RefreshResponseData(newAccessToken, "Bearer", expiresIn);
    }

    public void logout(String refreshTokenCookie, HttpServletResponse response) {
        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            refreshSessions.remove(refreshTokenCookie);
        }
        clearRefreshCookie(response);
    }

    @Transactional(readOnly = true)
    public UserAuthDto getMe(User currentUser) {
        List<String> roles = getUserRoles(currentUser);
        return toUserAuthDto(currentUser, roles);
    }

    public List<String> getUserRoles(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        if (userRoles == null || userRoles.isEmpty()) {
            return List.of("EMPLOYEE");
        }
        return userRoles.stream()
                .map(ur -> ur.getRole().getRoleName().toUpperCase())
                .distinct()
                .toList();
    }

    public UserAuthDto toUserAuthDto(User user, List<String> roles) {
        return new UserAuthDto(
                user.getUserId(),
                user.getEmployeeCode(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDesignation(),
                user.getAccountStatus(),
                roles,
                Collections.emptyMap()
        );
    }

    private void checkRateLimit(String key) {
        LoginAttemptRecord record = attemptTracker.get(key);
        if (record != null) {
            long now = System.currentTimeMillis();
            if (now < record.blockUntil()) {
                throw new AuthException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts. Please try again later.");
            } else if (now > record.windowExpiry()) {
                attemptTracker.remove(key);
            }
        }
    }

    private void recordFailedAttempt(String key) {
        long now = System.currentTimeMillis();
        attemptTracker.compute(key, (k, existing) -> {
            if (existing == null || now > existing.windowExpiry()) {
                return new LoginAttemptRecord(1, now + 300000L, 0); // 5 min window
            }
            int newCount = existing.attempts() + 1;
            long blockUntil = (newCount >= 5) ? now + 300000L : 0; // block for 5 min after 5 fails
            return new LoginAttemptRecord(newCount, existing.windowExpiry(), blockUntil);
        });
    }

    private void setRefreshCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // Localhost HTTP compatibility
                .path(REFRESH_COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private record LoginAttemptRecord(int attempts, long windowExpiry, long blockUntil) {}
    private record RefreshSession(Long userId, long expiry) {}
}
