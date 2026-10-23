package com.assetmanagement.backend.security;

import com.assetmanagement.backend.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CsrfService {

    private final SecureRandom secureRandom = new SecureRandom();
    // In-memory thread-safe store for active CSRF tokens with timestamp
    private final Map<String, Long> activeTokens = new ConcurrentHashMap<>();

    public String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Store with expiry (valid for 24 hours)
        activeTokens.put(token, System.currentTimeMillis() + 86400000L);
        cleanExpiredTokens();
        return token;
    }

    public void validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Security validation failed. Please refresh the page and try again.");
        }

        Long expiry = activeTokens.get(token);
        if (expiry == null || System.currentTimeMillis() > expiry) {
            activeTokens.remove(token);
            throw new AuthException(HttpStatus.FORBIDDEN, "Security validation failed. Please refresh the page and try again.");
        }
    }

    private void cleanExpiredTokens() {
        if (activeTokens.size() > 5000) {
            long now = System.currentTimeMillis();
            activeTokens.entrySet().removeIf(entry -> entry.getValue() < now);
        }
    }
}
