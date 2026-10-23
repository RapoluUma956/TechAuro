package com.assetmanagement.backend.security;

import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.exception.AuthException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {

    private final String secretKey;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtService(
            @Value("${app.jwt.secret:TeChAuRaSuPeRsEcReTjWtKeY99482948294829482948294829482948}") String secretKey) {
        this.secretKey = secretKey;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    public String generateAccessToken(User user, List<String> roles, boolean passwordChangeRequired) {
        long expiresIn = passwordChangeRequired ? 300 : 900;
        long now = Instant.now().getEpochSecond();
        long exp = now + expiresIn;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(user.getUserId()));
        payload.put("email", user.getEmail());
        payload.put("employeeCode", user.getEmployeeCode());
        payload.put("roles", roles != null ? roles : Collections.emptyList());
        payload.put("pwdReset", passwordChangeRequired);
        payload.put("iat", now);
        payload.put("exp", exp);

        try {
            String headerJson = objectMapper.writeValueAsString(header);
            String payloadJson = objectMapper.writeValueAsString(payload);

            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String dataToSign = encodedHeader + "." + encodedPayload;
            byte[] signature = signHmacSha256(dataToSign);
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);

            return dataToSign + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Error generating access token", e);
        }
    }

    public String generateRefreshToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public JwtClaims validateAndParse(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        String[] parts = token.trim().split("\\.");
        if (parts.length != 3) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        String dataToSign = parts[0] + "." + parts[1];
        byte[] expectedSignature = signHmacSha256(dataToSign);
        byte[] providedSignature;

        try {
            providedSignature = Base64.getUrlDecoder().decode(parts[2]);
        } catch (IllegalArgumentException e) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        try {
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, new TypeReference<>() {});

            long now = Instant.now().getEpochSecond();
            long exp = ((Number) claims.get("exp")).longValue();
            if (now >= exp) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
            }

            Long userId = Long.valueOf(claims.get("sub").toString());
            String email = (String) claims.get("email");
            String employeeCode = (String) claims.get("employeeCode");
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.getOrDefault("roles", Collections.emptyList());
            boolean pwdReset = Boolean.TRUE.equals(claims.get("pwdReset"));
            long iat = ((Number) claims.get("iat")).longValue();

            return new JwtClaims(userId, email, employeeCode, roles, pwdReset, iat, exp);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }
    }

    private byte[] signHmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error computing HMAC-SHA256", e);
        }
    }
}
