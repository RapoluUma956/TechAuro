package com.assetmanagement.backend.websocket;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.RemoteAccessAudit;
import com.assetmanagement.backend.entity.RemoteAccessSession;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.RemoteAccessAuditRepository;
import com.assetmanagement.backend.repository.RemoteAccessSessionRepository;
import com.assetmanagement.backend.repository.UserRepository;
import com.assetmanagement.backend.security.JwtClaims;
import com.assetmanagement.backend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class AdminRemoteDesktopWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminRemoteDesktopWebSocketHandler.class);
    private static final int BUFFER_LIMIT = 10 * 1024 * 1024; // 10MB to prevent 1009

    private final RemoteDesktopRegistry registry;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final RemoteAccessSessionRepository sessionRepository;
    private final RemoteAccessAuditRepository auditRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public AdminRemoteDesktopWebSocketHandler(
            RemoteDesktopRegistry registry,
            DeviceRepository deviceRepository,
            UserRepository userRepository,
            RemoteAccessSessionRepository sessionRepository,
            RemoteAccessAuditRepository auditRepository,
            JwtService jwtService,
            ObjectMapper objectMapper) {
        this.registry = registry;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.auditRepository = auditRepository;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(BUFFER_LIMIT);
        session.setBinaryMessageSizeLimit(BUFFER_LIMIT);

        log.info("[AdminDesktopWS] Admin connected from {} (wsId: {})",
                session.getRemoteAddress(), session.getId());

        try {
            Map<String, String> params = parseQueryParams(session.getUri());
            String token = params.get("token");
            if (token == null || token.isBlank()) {
                sendErrorAndClose(session, null, "Authentication required: missing token parameter", 4001);
                return;
            }

            // 1. Authenticate admin user
            JwtClaims claims;
            try {
                claims = jwtService.validateAndParse(token);
            } catch (Exception e) {
                sendErrorAndClose(session, null, "Authentication failed: invalid or expired token", 4001);
                return;
            }

            User adminUser = userRepository.findById(claims.userId())
                    .filter(u -> "ACTIVE".equalsIgnoreCase(u.getAccountStatus()))
                    .orElse(null);

            if (adminUser == null) {
                sendErrorAndClose(session, null, "Unauthorized: user account is inactive or not found", 4003);
                return;
            }

            // 2. Resolve target device
            String deviceIdStr = params.get("deviceId");
            String serialNumber = params.get("serialNumber");
            Long deviceId = null;
            if (deviceIdStr != null && !deviceIdStr.isBlank()) {
                try {
                    deviceId = Long.parseLong(deviceIdStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            Optional<Device> devOpt = Optional.empty();
            if (deviceId != null && deviceId > 0) {
                devOpt = deviceRepository.findById(deviceId);
            }
            if (devOpt.isEmpty() && serialNumber != null && !serialNumber.isBlank()) {
                devOpt = deviceRepository.findBySerialNumberIgnoreCase(serialNumber.trim());
            }

            if (devOpt.isPresent()) {
                Device d = devOpt.get();
                deviceId = d.getDeviceId();
                if (serialNumber == null || serialNumber.isBlank()) {
                    serialNumber = d.getSerialNumber();
                }
            }

            // 3. Locate active agent connection in RemoteDesktopRegistry
            WebSocketSession agentSession = registry.getAgentSession(deviceId, serialNumber);
            if (agentSession == null || !agentSession.isOpen()) {
                String devIdent = (serialNumber != null && !serialNumber.isBlank()) ? serialNumber : String.valueOf(deviceId);
                sendErrorAndClose(session, null,
                        "The monitoring agent for device " + devIdent + " is not currently connected for remote desktop. Please ensure the agent is running on the employee laptop.",
                        4004);
                return;
            }

            // 4. Create and persist new RemoteAccessSession record
            String sessionId = UUID.randomUUID().toString();
            RemoteAccessSession dbSession = new RemoteAccessSession();
            dbSession.setSessionId(sessionId);
            dbSession.setDeviceId(deviceId);
            dbSession.setSerialNumber(serialNumber);
            dbSession.setAdminUserId(adminUser.getUserId());
            dbSession.setAdminEmail(adminUser.getEmail());
            String adminDisplayName = (adminUser.getFirstName() != null && !adminUser.getFirstName().isBlank())
                    ? adminUser.getFirstName() + (adminUser.getLastName() != null && !adminUser.getLastName().isBlank() ? " " + adminUser.getLastName() : "")
                    : adminUser.getEmail();
            dbSession.setAdminName(adminDisplayName);
            dbSession.setStatus("PENDING_APPROVAL");
            dbSession.setRequestedAt(LocalDateTime.now());
            dbSession.setConnectionType("WEBSOCKET_STREAM");
            sessionRepository.save(dbSession);

            // 5. Create audit record
            recordAudit(sessionId, deviceId, adminUser.getUserId(), "SESSION_REQUESTED",
                    "Admin requested remote desktop access. Awaiting employee consent.");

            // 6. Register pairing in RemoteDesktopRegistry
            RemoteDesktopRegistry.DesktopSessionPair pair = new RemoteDesktopRegistry.DesktopSessionPair(
                    sessionId, session, agentSession, deviceId, serialNumber,
                    adminUser.getUserId(), adminUser.getEmail(), dbSession.getAdminName()
            );
            registry.registerSession(pair);

            // 7. Send REMOTE_ACCESS_REQUEST to employee agent
            RemoteDesktopMessage agentReq = RemoteDesktopMessage.builder()
                    .messageType("REMOTE_ACCESS_REQUEST")
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .serialNumber(serialNumber)
                    .adminName(dbSession.getAdminName())
                    .adminEmail(dbSession.getAdminEmail())
                    .timestamp(System.currentTimeMillis())
                    .build();

            safeSend(agentSession, new TextMessage(objectMapper.writeValueAsString(agentReq)));

            // 8. Inform Admin browser that request is pending employee approval
            RemoteDesktopMessage adminNotice = RemoteDesktopMessage.builder()
                    .messageType("REQUEST_PENDING")
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .serialNumber(serialNumber)
                    .adminName(dbSession.getAdminName())
                    .payload("Remote desktop request sent to employee. Awaiting approval on target device...")
                    .timestamp(System.currentTimeMillis())
                    .build();

            safeSend(session, new TextMessage(objectMapper.writeValueAsString(adminNotice)));

            log.info("[AdminDesktopWS] Initiated desktop request '{}' for device '{}' by admin '{}'",
                    sessionId, serialNumber, adminUser.getEmail());

        } catch (Exception e) {
            log.error("[AdminDesktopWS] Error during connection establishment: {}", e.getMessage(), e);
            sendErrorAndClose(session, null, "Failed to initialize remote desktop: " + e.getMessage(), 1011);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            RemoteDesktopMessage msg = objectMapper.readValue(message.getPayload(), RemoteDesktopMessage.class);
            if (msg.getMessageType() == null) return;

            RemoteDesktopRegistry.DesktopSessionPair pair = registry.getByAdminWs(session);
            if (pair == null) {
                log.warn("[AdminDesktopWS] Received message from unassociated admin session {}", session.getId());
                return;
            }

            switch (msg.getMessageType()) {
                case "INPUT":
                    if (pair.approved && pair.agentSession != null && pair.agentSession.isOpen()) {
                        safeSend(pair.agentSession, message);
                    }
                    break;

                case "CLIPBOARD":
                    if (pair.approved && pair.agentSession != null && pair.agentSession.isOpen()) {
                        safeSend(pair.agentSession, message);
                        recordAudit(pair.sessionId, pair.deviceId, pair.adminUserId, "CLIPBOARD_SHARED", "Clipboard text synced to remote device");
                    }
                    break;

                case "FILE_TRANSFER_META":
                case "FILE_TRANSFER_CHUNK":
                    if (pair.approved && pair.agentSession != null && pair.agentSession.isOpen()) {
                        safeSend(pair.agentSession, message);
                        if ("FILE_TRANSFER_META".equals(msg.getMessageType())) {
                            recordAudit(pair.sessionId, pair.deviceId, pair.adminUserId, "FILE_TRANSFERRED",
                                    "File send initiated: " + msg.getFileName() + " (" + msg.getFileSize() + " bytes)");
                        }
                    }
                    break;

                case "DISCONNECT":
                    handleAdminDisconnect(session, pair, "Admin clicked disconnect");
                    break;

                case "PING":
                    RemoteDesktopMessage pong = RemoteDesktopMessage.builder()
                            .messageType("PONG")
                            .timestamp(System.currentTimeMillis())
                            .build();
                    safeSend(session, new TextMessage(objectMapper.writeValueAsString(pong)));
                    break;

                default:
                    log.debug("[AdminDesktopWS] Forwarding admin messageType '{}' to agent", msg.getMessageType());
                    if (pair.agentSession != null && pair.agentSession.isOpen()) {
                        safeSend(pair.agentSession, message);
                    }
            }

        } catch (Exception e) {
            log.error("[AdminDesktopWS] Error handling message from admin {}: {}", session.getId(), e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[AdminDesktopWS] Admin connection closed: wsId={}, status={}", session.getId(), status);
        RemoteDesktopRegistry.DesktopSessionPair pair = registry.getByAdminWs(session);
        if (pair != null) {
            handleAdminDisconnect(session, pair, "Admin closed connection (" + status.getReason() + ")");
        }
    }

    private void handleAdminDisconnect(WebSocketSession session, RemoteDesktopRegistry.DesktopSessionPair pair, String reason) {
        try {
            // Notify agent that admin disconnected
            if (pair.agentSession != null && pair.agentSession.isOpen()) {
                RemoteDesktopMessage termMsg = RemoteDesktopMessage.builder()
                        .messageType("DISCONNECT")
                        .sessionId(pair.sessionId)
                        .payload(reason)
                        .timestamp(System.currentTimeMillis())
                        .build();
                safeSend(pair.agentSession, new TextMessage(objectMapper.writeValueAsString(termMsg)));
            }

            // Update database session
            sessionRepository.findBySessionId(pair.sessionId).ifPresent(dbSession -> {
                if ("PENDING_APPROVAL".equals(dbSession.getStatus()) || "ACTIVE".equals(dbSession.getStatus())) {
                    dbSession.setStatus("DISCONNECTED");
                    dbSession.setEndedAt(LocalDateTime.now());
                    dbSession.setTerminationReason(reason);
                    sessionRepository.save(dbSession);
                }
            });

            recordAudit(pair.sessionId, pair.deviceId, pair.adminUserId, "DISCONNECTED", reason);
        } catch (Exception e) {
            log.error("[AdminDesktopWS] Error closing session {}: {}", pair.sessionId, e.getMessage());
        } finally {
            registry.removeSession(pair.sessionId);
        }
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
            log.warn("[AdminDesktopWS] Failed to save audit record: {}", e.getMessage());
        }
    }

    private void safeSend(WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.error("[AdminDesktopWS] Failed to send message to session {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }

    private void sendErrorAndClose(WebSocketSession session, String sessionId, String errorMsg, int closeCode) {
        try {
            if (session.isOpen()) {
                RemoteDesktopMessage err = RemoteDesktopMessage.error(sessionId, errorMsg);
                safeSend(session, new TextMessage(objectMapper.writeValueAsString(err)));
                session.close(new CloseStatus(closeCode, errorMsg.length() > 100 ? errorMsg.substring(0, 100) : errorMsg));
            }
        } catch (Exception ignored) {}
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> map = new HashMap<>();
        if (uri == null || uri.getQuery() == null) return map;
        for (String param : uri.getQuery().split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                map.put(pair[0].trim(), pair[1].trim());
            } else if (pair.length == 1) {
                map.put(pair[0].trim(), "");
            }
        }
        return map;
    }
}
