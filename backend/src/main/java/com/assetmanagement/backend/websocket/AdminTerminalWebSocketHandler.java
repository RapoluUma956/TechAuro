package com.assetmanagement.backend.websocket;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.RemoteTerminalSession;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.RemoteTerminalSessionRepository;
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
public class AdminTerminalWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminTerminalWebSocketHandler.class);

    private final ActiveAgentRegistry activeAgentRegistry;
    private final TerminalSessionRegistry terminalSessionRegistry;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final RemoteTerminalSessionRepository sessionRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public AdminTerminalWebSocketHandler(
            ActiveAgentRegistry activeAgentRegistry,
            TerminalSessionRegistry terminalSessionRegistry,
            DeviceRepository deviceRepository,
            UserRepository userRepository,
            RemoteTerminalSessionRepository sessionRepository,
            JwtService jwtService,
            ObjectMapper objectMapper) {
        this.activeAgentRegistry = activeAgentRegistry;
        this.terminalSessionRegistry = terminalSessionRegistry;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[AdminTerminalWS] Admin connected from {} (wsId: {})",
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
                devOpt = deviceRepository.findBySerialNumber(serialNumber.trim());
            }

            if (devOpt.isEmpty()) {
                sendErrorAndClose(session, null, "Target device not found in system records", 4004);
                return;
            }

            Device device = devOpt.get();
            deviceId = device.getDeviceId();
            serialNumber = device.getSerialNumber();

            String shellType = params.getOrDefault("shell", "powershell").toLowerCase().trim();
            if (!"cmd".equals(shellType)) {
                shellType = "powershell";
            }

            // 3. Check if the monitoring agent for this device is online and connected
            WebSocketSession agentWs = activeAgentRegistry.getAgentSession(deviceId, serialNumber);
            if (agentWs == null || !agentWs.isOpen()) {
                String devLabel = serialNumber != null ? serialNumber : String.valueOf(deviceId);
                sendErrorAndClose(session, null,
                        "The monitoring agent for device " + devLabel + " is not currently connected to the terminal relay. Please ensure Agent.exe is running on the employee laptop.",
                        4004);
                return;
            }

            // 4. Initialize session
            String sessionId = UUID.randomUUID().toString();

            RemoteTerminalSession entity = new RemoteTerminalSession();
            entity.setSessionId(sessionId);
            entity.setDeviceId(deviceId);
            entity.setSerialNumber(serialNumber);
            entity.setAdminUserId(adminUser.getUserId());
            entity.setAdminEmail(adminUser.getEmail());
            entity.setShellType(shellType);
            entity.setStatus("INITIATED");
            entity.setStartedAt(LocalDateTime.now());
            sessionRepository.save(entity);

            TerminalSessionRegistry.SessionPair pair = new TerminalSessionRegistry.SessionPair(
                    sessionId, session, agentWs, deviceId, serialNumber,
                    adminUser.getUserId(), adminUser.getEmail(), shellType
            );
            terminalSessionRegistry.register(pair);

            // 5. Send confirmation to Admin
            sendJson(session, TerminalMessage.builder()
                    .messageType("SESSION_INITIALIZING")
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .serialNumber(serialNumber)
                    .payload("Requesting interactive " + shellType.toUpperCase() + " session from employee monitoring agent...")
                    .timestamp(System.currentTimeMillis())
                    .build());

            // 6. Send TERMINAL_REQUEST to Agent
            sendJson(agentWs, TerminalMessage.builder()
                    .messageType("TERMINAL_REQUEST")
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .serialNumber(serialNumber)
                    .payload(shellType)
                    .timestamp(System.currentTimeMillis())
                    .build());

        } catch (Exception e) {
            log.error("[AdminTerminalWS] Error during admin handshake: {}", e.getMessage(), e);
            sendErrorAndClose(session, null, "Internal relay error: " + e.getMessage(), 4500);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            TerminalMessage msg = objectMapper.readValue(payload, TerminalMessage.class);

            TerminalSessionRegistry.SessionPair pair = terminalSessionRegistry.getSession(msg.getSessionId());
            if (pair == null) {
                pair = terminalSessionRegistry.getByAdminWs(session);
            }

            if (pair == null) {
                sendJson(session, TerminalMessage.error(null, "No active terminal session associated with this connection"));
                return;
            }

            if (pair.agentSession != null && pair.agentSession.isOpen()) {
                switch (msg.getMessageType()) {
                    case "TERMINAL_INPUT":
                    case "TERMINAL_RESIZE":
                        msg.setSessionId(pair.sessionId);
                        sendJson(pair.agentSession, msg);
                        break;

                    case "TERMINAL_DISCONNECT":
                        sendJson(pair.agentSession, msg);
                        terminalSessionRegistry.remove(pair.sessionId);
                        updateSessionClosed(pair.sessionId);
                        session.close(new CloseStatus(1000, "User disconnected"));
                        break;

                    default:
                        log.debug("[AdminTerminalWS] Unhandled admin message: {}", msg.getMessageType());
                }
            } else {
                sendJson(session, TerminalMessage.error(pair.sessionId, "Agent connection is no longer available"));
            }

        } catch (Exception e) {
            log.error("[AdminTerminalWS] Error routing admin message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[AdminTerminalWS] Admin connection closed: wsId={}, status={}", session.getId(), status);
        TerminalSessionRegistry.SessionPair pair = terminalSessionRegistry.getByAdminWs(session);
        if (pair != null) {
            terminalSessionRegistry.remove(pair.sessionId);
            updateSessionClosed(pair.sessionId);

            // Inform agent to terminate the shell process
            if (pair.agentSession != null && pair.agentSession.isOpen()) {
                try {
                    sendJson(pair.agentSession, TerminalMessage.builder()
                            .messageType("TERMINAL_DISCONNECT")
                            .sessionId(pair.sessionId)
                            .timestamp(System.currentTimeMillis())
                            .build());
                } catch (Exception ignored) {}
            }
        }
    }

    private void updateSessionClosed(String sessionId) {
        if (sessionId == null) return;
        try {
            sessionRepository.findBySessionId(sessionId).ifPresent(s -> {
                if (!"CLOSED".equals(s.getStatus())) {
                    s.setStatus("CLOSED");
                    s.setEndedAt(LocalDateTime.now());
                    sessionRepository.save(s);
                }
            });
        } catch (Exception ignored) {}
    }

    private void sendErrorAndClose(WebSocketSession session, String sessionId, String errorMsg, int closeCode) {
        try {
            if (session.isOpen()) {
                sendJson(session, TerminalMessage.error(sessionId, errorMsg));
                session.close(new CloseStatus(closeCode, errorMsg));
            }
        } catch (Exception ignored) {}
    }

    private void sendJson(WebSocketSession session, Object obj) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(obj)));
            }
        }
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> map = new HashMap<>();
        if (uri == null || uri.getQuery() == null) return map;

        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String val = pair.substring(idx + 1);
                map.put(key, java.net.URLDecoder.decode(val, java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        return map;
    }
}

