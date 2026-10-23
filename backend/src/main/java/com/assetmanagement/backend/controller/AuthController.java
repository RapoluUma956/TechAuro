package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.ApiResponse;
import com.assetmanagement.backend.dto.auth.*;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.exception.AuthException;
import com.assetmanagement.backend.repository.UserRepository;
import com.assetmanagement.backend.security.CsrfService;
import com.assetmanagement.backend.security.JwtClaims;
import com.assetmanagement.backend.security.JwtService;
import com.assetmanagement.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final CsrfService csrfService;
    private final UserRepository userRepository;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            CsrfService csrfService,
            UserRepository userRepository) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.csrfService = csrfService;
        this.userRepository = userRepository;
    }

    // 1) GET /api/auth/csrf
    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<CsrfResponseData>> getCsrfToken() {
        CsrfResponseData data = authService.getCsrfToken();
        return ResponseEntity.ok(ApiResponse.success("CSRF token retrieved", data));
    }

    // 2) POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseData>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String clientIp = getClientIp(httpRequest);
        LoginResponseData data = authService.login(request, clientIp, httpResponse);

        String message = data.passwordChangeRequired() ? "Password reset required" : "Login successful";
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    // 3) POST /api/auth/change-password
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-CSRF-TOKEN", required = false) String csrfHeader,
            HttpServletResponse httpResponse) {
        csrfService.validateToken(csrfHeader);
        User currentUser = extractUserFromToken(authHeader);

        authService.changePassword(request, currentUser, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully. Please log in again.", null));
    }

    // 4) POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseData>> refresh(
            @RequestHeader(value = "X-CSRF-TOKEN", required = false) String csrfHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse httpResponse) {
        csrfService.validateToken(csrfHeader);

        RefreshResponseData data = authService.refresh(refreshTokenCookie, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Access token refreshed", data));
    }

    // 5) POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "X-CSRF-TOKEN", required = false) String csrfHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse httpResponse) {
        csrfService.validateToken(csrfHeader);

        authService.logout(refreshTokenCookie, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    // 6) GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserAuthDto>> getMe(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = extractUserFromToken(authHeader);
        UserAuthDto data = authService.getMe(currentUser);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", data));
    }

    private User extractUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        String token = authHeader.substring(7).trim();
        JwtClaims claims = jwtService.validateAndParse(token);

        return userRepository.findById(claims.userId())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again."));
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
