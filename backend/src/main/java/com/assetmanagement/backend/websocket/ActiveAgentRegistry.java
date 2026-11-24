package com.assetmanagement.backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry maintaining active outbound agent WebSocket connections.
 * Maps agents by BOTH numeric deviceId and hardware serialNumber to prevent lookup failures.
 */
@Component
public class ActiveAgentRegistry {

    private static final Logger log = LoggerFactory.getLogger(ActiveAgentRegistry.class);

    private final Map<String, WebSocketSession> agentsBySerial = new ConcurrentHashMap<>();
    private final Map<Long, WebSocketSession> agentsByDeviceId = new ConcurrentHashMap<>();
    private final Map<String, AgentSessionMeta> metaBySessionId = new ConcurrentHashMap<>();

    public static class AgentSessionMeta {
        public final Long deviceId;
        public final String serialNumber;
        public final String hostname;
        public final long connectedAt;

        public AgentSessionMeta(Long deviceId, String serialNumber, String hostname) {
            this.deviceId = deviceId;
            this.serialNumber = serialNumber;
            this.hostname = hostname;
            this.connectedAt = System.currentTimeMillis();
        }
    }

    public synchronized void register(WebSocketSession session, Long deviceId, String serialNumber, String hostname) {
        if (session == null || !session.isOpen()) {
            return;
        }

        String safeSerial = (serialNumber != null) ? serialNumber.trim().toUpperCase() : null;

        if (safeSerial != null && !safeSerial.isBlank()) {
            WebSocketSession existing = agentsBySerial.put(safeSerial, session);
            if (existing != null && existing.isOpen() && !existing.getId().equals(session.getId())) {
                try {
                    existing.close();
                } catch (Exception ignored) {}
            }
        }

        if (deviceId != null && deviceId > 0) {
            agentsByDeviceId.put(deviceId, session);
        }

        metaBySessionId.put(session.getId(), new AgentSessionMeta(deviceId, safeSerial, hostname));
        log.info("[ActiveAgentRegistry] Agent registered: serial='{}', deviceId={}, host='{}', wsId='{}'",
                safeSerial, deviceId, hostname, session.getId());
    }

    public synchronized void unregister(WebSocketSession session) {
        if (session == null) return;

        AgentSessionMeta meta = metaBySessionId.remove(session.getId());
        if (meta != null) {
            if (meta.serialNumber != null) {
                agentsBySerial.remove(meta.serialNumber, session);
            }
            if (meta.deviceId != null) {
                agentsByDeviceId.remove(meta.deviceId, session);
            }
            log.info("[ActiveAgentRegistry] Agent unregistered: serial='{}', deviceId={}, wsId='{}'",
                    meta.serialNumber, meta.deviceId, session.getId());
        }
    }

    public WebSocketSession getAgentSession(Long deviceId, String serialNumber) {
        // 1. Try by serial number first if provided
        if (serialNumber != null && !serialNumber.isBlank()) {
            WebSocketSession s = agentsBySerial.get(serialNumber.trim().toUpperCase());
            if (s != null && s.isOpen()) {
                return s;
            }
        }

        // 2. Fall back to numeric deviceId
        if (deviceId != null && deviceId > 0) {
            WebSocketSession s = agentsByDeviceId.get(deviceId);
            if (s != null && s.isOpen()) {
                return s;
            }
        }

        return null;
    }

    public boolean isAgentOnline(Long deviceId, String serialNumber) {
        WebSocketSession s = getAgentSession(deviceId, serialNumber);
        return s != null && s.isOpen();
    }

    public AgentSessionMeta getMeta(WebSocketSession session) {
        return session != null ? metaBySessionId.get(session.getId()) : null;
    }
}

