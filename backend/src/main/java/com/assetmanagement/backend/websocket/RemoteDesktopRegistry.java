package com.assetmanagement.backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for Remote Desktop WebSocket sessions.
 * Manages connected employee agents (by both serial number and deviceId)
 * and active session pairs between Admin browsers and Agents.
 */
@Component
public class RemoteDesktopRegistry {

    private static final Logger log = LoggerFactory.getLogger(RemoteDesktopRegistry.class);

    // Connected agents
    private final Map<String, WebSocketSession> agentsBySerial = new ConcurrentHashMap<>();
    private final Map<Long, WebSocketSession> agentsByDeviceId = new ConcurrentHashMap<>();
    private final Map<String, AgentMeta> metaByWsId = new ConcurrentHashMap<>();

    public static class AgentMeta {
        public final Long deviceId;
        public final String serialNumber;
        public final String hostname;
        public final long connectedAt;

        public AgentMeta(Long deviceId, String serialNumber, String hostname) {
            this.deviceId = deviceId;
            this.serialNumber = serialNumber;
            this.hostname = hostname;
            this.connectedAt = System.currentTimeMillis();
        }
    }

    public static class DesktopSessionPair {
        public final String sessionId;
        public final WebSocketSession adminSession;
        public final WebSocketSession agentSession;
        public final Long deviceId;
        public final String serialNumber;
        public final Long adminUserId;
        public final String adminEmail;
        public final String adminName;
        public volatile boolean approved = false;
        public final long requestedAt;

        public DesktopSessionPair(String sessionId, WebSocketSession adminSession, WebSocketSession agentSession,
                                  Long deviceId, String serialNumber, Long adminUserId, String adminEmail, String adminName) {
            this.sessionId = sessionId;
            this.adminSession = adminSession;
            this.agentSession = agentSession;
            this.deviceId = deviceId;
            this.serialNumber = serialNumber;
            this.adminUserId = adminUserId;
            this.adminEmail = adminEmail;
            this.adminName = adminName;
            this.requestedAt = System.currentTimeMillis();
        }
    }

    private final Map<String, DesktopSessionPair> sessionsById = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdByAdminWs = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdByAgentWs = new ConcurrentHashMap<>();

    // Agent registration
    public synchronized void registerAgent(WebSocketSession session, Long deviceId, String serialNumber, String hostname) {
        if (session == null || !session.isOpen()) return;

        String safeSerial = serialNumber != null ? serialNumber.trim().toUpperCase() : null;
        if (safeSerial != null && !safeSerial.isBlank()) {
            WebSocketSession old = agentsBySerial.put(safeSerial, session);
            if (old != null && old.isOpen() && !old.getId().equals(session.getId())) {
                try { old.close(); } catch (Exception ignored) {}
            }
        }
        if (deviceId != null && deviceId > 0) {
            agentsByDeviceId.put(deviceId, session);
        }
        metaByWsId.put(session.getId(), new AgentMeta(deviceId, safeSerial, hostname));
        log.info("[RemoteDesktopRegistry] Agent registered: serial='{}', deviceId={}, host='{}', wsId='{}'",
                safeSerial, deviceId, hostname, session.getId());
    }

    public synchronized void unregisterAgent(WebSocketSession session) {
        if (session == null) return;
        AgentMeta meta = metaByWsId.remove(session.getId());
        if (meta != null) {
            if (meta.serialNumber != null) agentsBySerial.remove(meta.serialNumber, session);
            if (meta.deviceId != null) agentsByDeviceId.remove(meta.deviceId, session);
            log.info("[RemoteDesktopRegistry] Agent unregistered: serial='{}', deviceId={}, wsId='{}'",
                    meta.serialNumber, meta.deviceId, session.getId());
        }
    }

    public WebSocketSession getAgentSession(Long deviceId, String serialNumber) {
        if (serialNumber != null && !serialNumber.isBlank()) {
            WebSocketSession s = agentsBySerial.get(serialNumber.trim().toUpperCase());
            if (s != null && s.isOpen()) return s;
        }
        if (deviceId != null && deviceId > 0) {
            WebSocketSession s = agentsByDeviceId.get(deviceId);
            if (s != null && s.isOpen()) return s;
        }
        return null;
    }

    public boolean isAgentOnline(Long deviceId, String serialNumber) {
        WebSocketSession s = getAgentSession(deviceId, serialNumber);
        return s != null && s.isOpen();
    }

    public AgentMeta getAgentMeta(WebSocketSession session) {
        return session != null ? metaByWsId.get(session.getId()) : null;
    }

    // Session pairings
    public void registerSession(DesktopSessionPair pair) {
        sessionsById.put(pair.sessionId, pair);
        if (pair.adminSession != null) sessionIdByAdminWs.put(pair.adminSession.getId(), pair.sessionId);
        if (pair.agentSession != null) sessionIdByAgentWs.put(pair.agentSession.getId(), pair.sessionId);
        log.info("[RemoteDesktopRegistry] Registered desktop session '{}' (admin='{}', serial='{}')",
                pair.sessionId, pair.adminEmail, pair.serialNumber);
    }

    public DesktopSessionPair getSession(String sessionId) {
        return sessionId != null ? sessionsById.get(sessionId) : null;
    }

    public DesktopSessionPair getByAdminWs(WebSocketSession adminWs) {
        if (adminWs == null) return null;
        String sid = sessionIdByAdminWs.get(adminWs.getId());
        return sid != null ? sessionsById.get(sid) : null;
    }

    public DesktopSessionPair getByAgentWs(WebSocketSession agentWs) {
        if (agentWs == null) return null;
        String sid = sessionIdByAgentWs.get(agentWs.getId());
        return sid != null ? sessionsById.get(sid) : null;
    }

    public DesktopSessionPair removeSession(String sessionId) {
        if (sessionId == null) return null;
        DesktopSessionPair pair = sessionsById.remove(sessionId);
        if (pair != null) {
            if (pair.adminSession != null) sessionIdByAdminWs.remove(pair.adminSession.getId());
            if (pair.agentSession != null) sessionIdByAgentWs.remove(pair.agentSession.getId());
            log.info("[RemoteDesktopRegistry] Removed desktop session '{}'", sessionId);
        }
        return pair;
    }
}
