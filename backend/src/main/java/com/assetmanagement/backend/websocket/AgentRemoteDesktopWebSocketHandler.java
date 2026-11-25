package com.assetmanagement.backend.websocket;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.RemoteAccessAudit;
import com.assetmanagement.backend.entity.RemoteAccessSession;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.RemoteAccessAuditRepository;
import com.assetmanagement.backend.repository.RemoteAccessSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AgentRemoteDesktopWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentRemoteDesktopWebSocketHandler.class);
    private static final int BUFFER_LIMIT = 10 * 1024 * 1024; // 10MB to prevent 1009

    private final RemoteDesktopRegistry registry;
    private final DeviceRepository deviceRepository;
    private final RemoteAccessSessionRepository sessionRepository;
    private final RemoteAccessAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public AgentRemoteDesktopWebSocketHandler(
            RemoteDesktopRegistry registry,
            DeviceRepository deviceRepository,
            RemoteAccessSessionRepository sessionRepository,
            RemoteAccessAuditRepository auditRepository,
            ObjectMapper objectMapper) {
        this.registry = registry;
        this.deviceRepository = deviceRepository;
        this.sessionRepository = sessionRepository;
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(BUFFER_LIMIT);
        session.setBinaryMessageSizeLimit(BUFFER_LIMIT);

        log.info("[AgentDesktopWS] Agent connected from {} (wsId: {})",
                session.getRemoteAddress(), session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            RemoteDesktopMessage msg = objectMapper.readValue(message.getPayload(), RemoteDesktopMessage.class);
            if (msg.getMessageType() == null) return;

            switch (msg.getMessageType()) {
                case "REGISTER":
                    handleRegister(session, msg);
                    break;

                case "HEARTBEAT":
                    RemoteDesktopMessage ack = RemoteDesktopMessage.builder()
                            .messageType("HEARTBEAT_ACK")
                            .timestamp(System.currentTimeMillis())
                            .build();
                    safeSend(session, new TextMessage(objectMapper.writeValueAsString(ack)));
                    break;

                case "APPROVAL_RESPONSE":
                    handleApprovalResponse(session, msg);
                    break;

                case "RESOLUTION":
                case "CLIPBOARD":
                case "FILE_TRANSFER_ACK":
                case "FILE_TRANSFER_PROGRESS":
                    forwardToAdmin(session, message);
                    break;

                case "SESSION_TERMINATED":
                case "DISCONNECT":
                    handleAgentDisconnect(session, msg.getPayload() != null ? msg.getPayload() : "Employee ended remote access session");
                    break;

                default:
                    log.debug("[AgentDesktopWS] Forwarding unhandled agent message '{}' to admin", msg.getMessageType());
                    forwardToAdmin(session, message);
            }

        } catch (Exception e) {
            log.error("[AgentDesktopWS] Error handling text message from wsId={}: {}", session.getId(), e.getMessage());
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // Fast-path forward binary screen frame to admin session
        RemoteDesktopRegistry.DesktopSessionPair pair = registry.getByAgentWs(session);
        if (pair != null && pair.approved && pair.adminSession != null && pair.adminSession.isOpen()) {
            safeSend(pair.adminSession, message);
        }
    }

    private void handleRegister(WebSocketSession session, RemoteDesktopMessage msg) throws IOException {
        String serial = (msg.getSerialNumber() != null) ? msg.getSerialNumber().trim() : null;
        Long deviceId = msg.getDeviceId();
        String hostname = msg.getPayload();

        if ((deviceId == null || deviceId <= 0) && serial != null && !serial.isBlank()) {
            Optional<Device> devOpt = deviceRepository.findBySerialNumberIgnoreCase(serial);
            if (devOpt.isPresent()) {
                deviceId = devOpt.get().getDeviceId();
            }
        }

        registry.registerAgent(session, deviceId, serial, hostname);

        RemoteDesktopMessage reply = RemoteDesktopMessage.builder()
                .messageType("REGISTER_ACK")
                .deviceId(deviceId)
                .serialNumber(serial)
                .payload("Registered for remote desktop relay successfully")
                .timestamp(System.currentTimeMillis())
                .build();

        safeSend(session, new TextMessage(objectMapper.writeValueAsString(reply)));
        log.info("[AgentDesktopWS] Successfully registered agent for device serial='{}', id={}", serial, deviceId);
    }

    private void handleApprovalResponse(WebSocketSession session, RemoteDesktopMessage msg) {
        String sessionId = msg.getSessionId();
        RemoteDesktopRegistry.DesktopSessionPair pair = registry.getSession(sessionId);
        if (pair == null) {
            log.warn("[AgentDesktopWS] Approval response received for unknown session: {}", sessionId);
            return;
        }

        boolean isApproved = "APPROVED".equalsIgnoreCase(msg.getPayload()) ||
                (msg.getInputType() != null && "APPROVED".equalsIgnoreCase(msg.getInputType()));

        if (isApproved) {
            pair.approved = true;

            // Update DB session
            sessionRepository.findBySessionId(sessionId).ifPresent(dbSession -> {
                dbSession.setStatus("ACTIVE");
                dbSession.setStartedAt(LocalDateTime.now());
                sessionRepository.save(dbSession);
            });

            recordAudit(sessionId, pair.deviceId, pair.adminUserId, "APPROVED",
                    "Employee consented to remote desktop access.");

            // Notify Admin
            RemoteDesktopMessage approvedMsg = RemoteDesktopMessage.builder()
                    .messageType("APPROVED")
                    .sessionId(sessionId)
                    .payload("Employee approved remote desktop connection.")
                    .screenWidth(msg.getScreenWidth())
                    .screenHeight(msg.getScreenHeight())
                    .timestamp(System.currentTimeMillis())
                    .build();

            try {
                safeSend(pair.adminSession, new TextMessage(objectMapper.writeValueAsString(approvedMsg)));
            } catch (Exception e) {
                log.error("[AgentDesktopWS] Failed to notify admin of approval: {}", e.getMessage());
            }

            log.info("[AgentDesktopWS] Session '{}' APPROVED by employee.", sessionId);

        } else {
            // Rejected
            sessionRepository.findBySessionId(sessionId).ifPresent(dbSession -> {
                dbSession.setStatus("REJECTED");
                dbSession.setEndedAt(LocalDateTime.now());
                dbSession.setTerminationReason("Employee rejected remote access request");
                sessionRepository.save(dbSession);
            });

            recordAudit(sessionId, pair.deviceId, pair.adminUserId, "REJECTED",
                    "Employee rejected remote access request.");

            RemoteDesktopMessage rejectedMsg = RemoteDesktopMessage.builder()
                    .messageType("REJECTED")
                    .sessionId(sessionId)
                    .payload("Employee rejected the remote desktop request.")
                    .timestamp(System.currentTimeMillis())
                    .build();

            try {
                safeSend(pair.adminSession, new TextMessage(objectMapper.writeValueAsString(rejectedMsg)));
            } catch (Exception ignored) {}

            registry.removeSession(sessionId);
            log.info("[AgentDesktopWS] Session '{}' REJECTED by employee.", sessionId);
        }
    }

    private void forwardToAdmin(WebSocketSession agentSession, TextMessage message) {
        RemoteDesktopRegistry.DesktopSessionPair pair = registry.getByAgentWs(agentSession);
        if (pair != null && pair.adminSession != null && pair.adminSession.isOpen()) {
            safeSend(pair.adminSession, message);
        }
    }

    private void handleAgentDisconnect(WebSocketSession session, String reason) {
        RemoteDesktopRegistry.DesktopSessionPair pair = registry.getByAgentWs(session);
        if (pair != null) {
            sessionRepository.findBySessionId(pair.sessionId).ifPresent(dbSession -> {
                if ("PENDING_APPROVAL".equals(dbSession.getStatus()) || "ACTIVE".equals(dbSession.getStatus())) {
                    dbSession.setStatus("DISCONNECTED");
                    dbSession.setEndedAt(LocalDateTime.now());
                    dbSession.setTerminationReason(reason);
                    sessionRepository.save(dbSession);
                }
            });

            recordAudit(pair.sessionId, pair.deviceId, pair.adminUserId, "DISCONNECTED", reason);

            if (pair.adminSession != null && pair.adminSession.isOpen()) {
                RemoteDesktopMessage discMsg = RemoteDesktopMessage.builder()
                        .messageType("DISCONNECTED")
                        .sessionId(pair.sessionId)
                        .payload(reason)
                        .timestamp(System.currentTimeMillis())
                        .build();
                try {
                    safeSend(pair.adminSession, new TextMessage(objectMapper.writeValueAsString(discMsg)));
                } catch (Exception ignored) {}
            }

            registry.removeSession(pair.sessionId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[AgentDesktopWS] Agent connection closed: wsId={}, status={}", session.getId(), status);
        handleAgentDisconnect(session, "Employee device disconnected (" + status.getReason() + ")");
        registry.unregisterAgent(session);
    }

    private void recordAudit(String sessionId, Long deviceId, Long adminUserId, String action, String details) {
        try {
            RemoteAccessAudit audit = new RemoteAccessAudit();
            audit.setSessionId(sessionId);
            audit.setDeviceId(deviceId);
            audit.setAdminUserId(adminUserId);
            audit.setAction(action);
            audit.setDetails(details);
            audit.setRecordedAt(LocalDateTime.now());
            auditRepository.save(audit);
        } catch (Exception e) {
            log.warn("[AgentDesktopWS] Failed to save audit record: {}", e.getMessage());
        }
    }

    private void safeSend(WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.error("[AgentDesktopWS] Error sending message to wsId={}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }
}
