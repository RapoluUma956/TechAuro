package org.example.agent.config;

import org.example.agent.StartupManager;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized configuration for the Asset Monitoring Agent.
 * Stores production cloud backend URLs, WebSocket endpoints, timeout thresholds,
 * and handles environment variable, system property, and server_url.txt overrides.
 */
public final class AgentConfig {

    // =========================================================================
    // 1. PRODUCTION CLOUD CONFIGURATION
    // =========================================================================

    /**
     * Deployed cloud backend base URL on Render.
     */
    public static final String DEFAULT_BACKEND_URL = "https://asset-management-backend-o8dh.onrender.com";

    /**
     * Endpoint path for device telemetry ingestion.
     */
    public static final String REPORT_ENDPOINT_PATH = "/api/agent/report";

    /**
     * Connection timeout in seconds.
     * Generous timeout to accommodate varied mobile/Wi-Fi latencies and cloud routing.
     */
    public static final int CONNECT_TIMEOUT_SECONDS = 20;

    /**
     * HTTP request timeout in seconds.
     * 60 seconds specifically accommodates Render free-tier cold starts (service spin-up from idle).
     */
    public static final int REQUEST_TIMEOUT_SECONDS = 60;

    /**
     * Periodic telemetry collection interval in milliseconds (30 seconds).
     */
    public static final long MONITORING_INTERVAL_MS = 30_000L;

    /**
     * Agent application version string.
     */
    public static final String AGENT_VERSION = "1.0.0";

    // Cache to avoid constant disk I/O on every collection cycle
    private static volatile String cachedBaseUrl = null;
    private static volatile long lastCacheCheckTime = 0L;
    private static final long CACHE_TTL_MS = 30_000L;

    private AgentConfig() {
        // Utility / configuration class
    }

    // =========================================================================
    // 2. DYNAMIC URL RESOLUTION
    // =========================================================================

    /**
     * Resolves the active backend base URL in priority order:
     * 1. Environment variable: AGENT_SERVER_URL
     * 2. System property: agent.server.url
     * 3. Config file server_url.txt (in working directory or beside the executable launcher)
     * 4. Default: https://asset-management-backend-o8dh.onrender.com
     */
    public static String getBaseUrl() {
        long now = System.currentTimeMillis();
        if (cachedBaseUrl != null && (now - lastCacheCheckTime) < CACHE_TTL_MS) {
            return cachedBaseUrl;
        }

        String rawUrl = resolveRawConfiguredUrl();
        String normalizedBase = normalizeToBaseUrl(rawUrl);

        cachedBaseUrl = normalizedBase;
        lastCacheCheckTime = now;
        return normalizedBase;
    }

    /**
     * Resolves the full URL for telemetry report submission (POST /api/agent/report).
     */
    public static String getReportUrl() {
        String base = getBaseUrl();
        return base + REPORT_ENDPOINT_PATH;
    }

    /**
     * Resolves the secure WebSocket base URL for remote access / terminal functionality.
     * Converts http:// -> ws:// and https:// -> wss://.
     * E.g. https://asset-management-backend-o8dh.onrender.com -> wss://asset-management-backend-o8dh.onrender.com
     */
    public static String getWebSocketBaseUrl() {
        String base = getBaseUrl();
        if (base.startsWith("https://")) {
            return "wss://" + base.substring("https://".length());
        } else if (base.startsWith("http://")) {
            return "ws://" + base.substring("http://".length());
        }
        return "wss://" + base;
    }

    /**
     * Resolves a full WebSocket endpoint URL given its path.
     * E.g. getWebSocketUrl("/ws/terminal") -> wss://asset-management-backend-o8dh.onrender.com/ws/terminal
     */
    public static String getWebSocketUrl(String endpointPath) {
        String wsBase = getWebSocketBaseUrl();
        if (endpointPath == null || endpointPath.isBlank()) {
            return wsBase;
        }
        String path = endpointPath.startsWith("/") ? endpointPath : "/" + endpointPath;
        return wsBase + path;
    }

    /**
     * Returns the prioritized list of URLs to try when submitting reports.
     * In production, the primary cloud URL is prioritized.
     */
    public static List<String> getReportTargetUrls() {
        List<String> targets = new ArrayList<>();
        String primary = getReportUrl();
        targets.add(primary);

        // Optional local development fallback: only if specifically enabled via env var
        String enableLocalFallback = System.getenv("AGENT_ENABLE_LOCAL_FALLBACK");
        if ("true".equalsIgnoreCase(enableLocalFallback) || Boolean.getBoolean("agent.enable.local.fallback")) {
            String localFallback = "http://localhost:8080" + REPORT_ENDPOINT_PATH;
            if (!targets.contains(localFallback)) {
                targets.add(localFallback);
            }
        }

        return targets;
    }

    public static Duration getConnectTimeout() {
        return Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS);
    }

    public static Duration getRequestTimeout() {
        return Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);
    }

    public static void clearCache() {
        cachedBaseUrl = null;
        lastCacheCheckTime = 0L;
    }

    // =========================================================================
    // 3. INTERNAL CONFIGURATION DETECTION HELPERS
    // =========================================================================

    private static String resolveRawConfiguredUrl() {
        // 1. Check environment variable
        String envUrl = System.getenv("AGENT_SERVER_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl.trim();
        }

        // 2. Check system property
        String propUrl = System.getProperty("agent.server.url");
        if (propUrl != null && !propUrl.isBlank()) {
            return propUrl.trim();
        }

        // 3. Check server_url.txt in current working directory
        File localConfigFile = new File("server_url.txt");
        String fromFile = readUrlFromFile(localConfigFile);
        if (fromFile != null) {
            return fromFile;
        }

        // 4. Check server_url.txt in the application executable directory
        try {
            File exeFile = StartupManager.detectApplicationExecutable();
            if (exeFile != null && exeFile.getParentFile() != null) {
                File exeConfigFile = new File(exeFile.getParentFile(), "server_url.txt");
                fromFile = readUrlFromFile(exeConfigFile);
                if (fromFile != null) {
                    return fromFile;
                }
            }
        } catch (Exception ignored) {
        }

        // 5. Default production cloud URL
        return DEFAULT_BACKEND_URL;
    }

    private static String readUrlFromFile(File file) {
        if (file != null && file.exists() && file.isFile() && file.canRead()) {
            try {
                String content = Files.readString(file.toPath()).trim();
                if (!content.isBlank()) {
                    return content;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String normalizeToBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_BACKEND_URL;
        }

        String url = raw.trim();

        // Strip trailing slash
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        // If the URL already ends with /api/agent/report or /api/v1/agent/report, strip it to get the base
        if (url.endsWith(REPORT_ENDPOINT_PATH)) {
            url = url.substring(0, url.length() - REPORT_ENDPOINT_PATH.length());
        } else if (url.endsWith("/api/v1/agent/report")) {
            url = url.substring(0, url.length() - "/api/v1/agent/report".length());
        }

        // Strip any residual trailing slash
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        // Validate URI format
        try {
            URI uri = URI.create(url);
            if (uri.getScheme() != null && uri.getHost() != null) {
                return url;
            }
        } catch (Exception ignored) {
        }

        return DEFAULT_BACKEND_URL;
    }
}

