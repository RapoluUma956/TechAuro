package com.assetmanagement.backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry holding active terminal session pairings between Administrator WebSockets and Agent WebSockets.
 */
@Component
public class TerminalSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(TerminalSessionRegistry.class);

    public static class SessionPair {
        public final String sessionId;
        public final WebSocketSession adminSession;
        public final WebSocketSession agentSession;
        public final Long deviceId;
        public final String serialNumber;
        public final Long adminUserId;
        public final String adminEmail;
        public final String shellType;
        public final long createdAt;

        public SessionPair(String sessionId, WebSocketSession adminSession, WebSocketSession agentSession,
                           Long deviceId, String serialNumber, Long adminUserId, String adminEmail, String shellType) {
            this.sessionId = sessionId;
            this.adminSession = adminSession;
            this.agentSession = agentSession;
            this.deviceId = deviceId;
            this.serialNumber = serialNumber;
            this.adminUserId = adminUserId;
            this.adminEmail = adminEmail;
            this.shellType = shellType;
            this.createdAt = System.currentTimeMillis();
        }
    }

    private final Map<String, SessionPair> sessionsById = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdByAdminWs = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdByAgentWs = new ConcurrentHashMap<>();

    public void register(SessionPair pair) {
        sessionsById.put(pair.sessionId, pair);
        if (pair.adminSession != null) {
            sessionIdByAdminWs.put(pair.adminSession.getId(), pair.sessionId);
        }
        if (pair.agentSession != null) {
            sessionIdByAgentWs.put(pair.agentSession.getId(), pair.sessionId);
        }
        log.info("[TerminalSessionRegistry] Registered session '{}' for deviceId={}, serial='{}', adminUser='{}'",
                pair.sessionId, pair.deviceId, pair.serialNumber, pair.adminEmail);
    }

    public SessionPair getSession(String sessionId) {
        return sessionId != null ? sessionsById.get(sessionId) : null;
    }

    public SessionPair getByAdminWs(WebSocketSession adminWs) {
        if (adminWs == null) return null;
        String sid = sessionIdByAdminWs.get(adminWs.getId());
        return sid != null ? sessionsById.get(sid) : null;
    }

    public SessionPair getByAgentWs(WebSocketSession agentWs) {
        if (agentWs == null) return null;
        String sid = sessionIdByAgentWs.get(agentWs.getId());
        return sid != null ? sessionsById.get(sid) : null;
    }

    public SessionPair remove(String sessionId) {
        if (sessionId == null) return null;
        SessionPair pair = sessionsById.remove(sessionId);
        if (pair != null) {
            if (pair.adminSession != null) {
                sessionIdByAdminWs.remove(pair.adminSession.getId());
            }
            if (pair.agentSession != null) {
                sessionIdByAgentWs.remove(pair.agentSession.getId());
            }
            log.info("[TerminalSessionRegistry] Removed session '{}'", sessionId);
        }
        return pair;
    }
}

