package com.assetmanagement.backend.websocket;

import com.assetmanagement.backend.entity.Device;
import com.assetmanagement.backend.entity.RemoteTerminalSession;
import com.assetmanagement.backend.repository.DeviceRepository;
import com.assetmanagement.backend.repository.RemoteTerminalSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AgentTerminalWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentTerminalWebSocketHandler.class);

    private final ActiveAgentRegistry activeAgentRegistry;
    private final TerminalSessionRegistry terminalSessionRegistry;
    private final DeviceRepository deviceRepository;
    private final RemoteTerminalSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    public AgentTerminalWebSocketHandler(
            ActiveAgentRegistry activeAgentRegistry,
            TerminalSessionRegistry terminalSessionRegistry,
            DeviceRepository deviceRepository,
            RemoteTerminalSessionRepository sessionRepository,
            ObjectMapper objectMapper) {
        this.activeAgentRegistry = activeAgentRegistry;
        this.terminalSessionRegistry = terminalSessionRegistry;
        this.deviceRepository = deviceRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[AgentTerminalWS] Agent connected from {} (wsId: {})",
                session.getRemoteAddress(), session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            TerminalMessage msg = objectMapper.readValue(payload, TerminalMessage.class);

            if (msg.getMessageType() == null) {
                return;
            }

            switch (msg.getMessageType()) {
                case "REGISTER":
                    handleRegister(session, msg);
                    break;

                case "HEARTBEAT":
                    sendJson(session, TerminalMessage.builder()
                            .messageType("HEARTBEAT_ACK")
                            .timestamp(System.currentTimeMillis())
                            .build());
                    break;

                case "TERMINAL_ACCEPTED":
                case "TERMINAL_OUTPUT":
                case "TERMINAL_ERROR":
                    forwardToAdmin(session, msg);
                    break;

                case "TERMINAL_CLOSED":
                    handleTerminalClosed(session, msg);
                    break;

                default:
                    log.warn("[AgentTerminalWS] Unknown messageType '{}' from agent wsId={}",
                            msg.getMessageType(), session.getId());
            }

        } catch (Exception e) {
            log.error("[AgentTerminalWS] Error processing agent message from wsId={}: {}",
                    session.getId(), e.getMessage(), e);
        }
    }

    private void handleRegister(WebSocketSession session, TerminalMessage msg) throws IOException {
        String serial = (msg.getSerialNumber() != null) ? msg.getSerialNumber().trim() : null;
        Long deviceId = msg.getDeviceId();
        String hostname = msg.getPayload();

        // If deviceId wasn't passed by agent, look up in database by serial number
        if ((deviceId == null || deviceId <= 0) && serial != null && !serial.isBlank()) {
            Optional<Device> devOpt = deviceRepository.findBySerialNumber(serial);
            if (devOpt.isPresent()) {
                deviceId = devOpt.get().getDeviceId();
            }
        }

        activeAgentRegistry.register(session, deviceId, serial, hostname);

        TerminalMessage ack = TerminalMessage.builder()
                .messageType("REGISTER_OK")
                .deviceId(deviceId)
                .serialNumber(serial)
                .payload("Agent registered successfully with terminal relay")
                .timestamp(System.currentTimeMillis())
                .build();
        sendJson(session, ack);
    }

    private void forwardToAdmin(WebSocketSession session, TerminalMessage msg) {
        TerminalSessionRegistry.SessionPair pair = terminalSessionRegistry.getSession(msg.getSessionId());
        if (pair == null) {
            pair = terminalSessionRegistry.getByAgentWs(session);
        }

        if (pair != null && pair.adminSession != null && pair.adminSession.isOpen()) {
            try {
                if ("TERMINAL_ACCEPTED".equals(msg.getMessageType())) {
                    updateSessionStatus(pair.sessionId, "ACTIVE", null);
                }
                sendJson(pair.adminSession, msg);
            } catch (Exception e) {
                log.error("[AgentTerminalWS] Failed to forward {} to admin: {}", msg.getMessageType(), e.getMessage());
            }
        }
    }

    private void handleTerminalClosed(WebSocketSession session, TerminalMessage msg) {
        TerminalSessionRegistry.SessionPair pair = terminalSessionRegistry.remove(msg.getSessionId());
        if (pair == null) {
            pair = terminalSessionRegistry.getByAgentWs(session);
            if (pair != null) {
                terminalSessionRegistry.remove(pair.sessionId);
            }
        }

        if (pair != null) {
            updateSessionStatus(pair.sessionId, "CLOSED", null);
            if (pair.adminSession != null && pair.adminSession.isOpen()) {
                try {
                    sendJson(pair.adminSession, msg);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[AgentTerminalWS] Agent connection closed: wsId={}, status={}", session.getId(), status);
        ActiveAgentRegistry.AgentSessionMeta meta = activeAgentRegistry.getMeta(session);
        activeAgentRegistry.unregister(session);

        // Notify and close any active terminal session using this agent
        TerminalSessionRegistry.SessionPair pair = terminalSessionRegistry.getByAgentWs(session);
        if (pair != null) {
            terminalSessionRegistry.remove(pair.sessionId);
            updateSessionStatus(pair.sessionId, "DISCONNECTED", "Agent disconnected from terminal relay");

            if (pair.adminSession != null && pair.adminSession.isOpen()) {
                try {
                    String devLabel = meta != null && meta.serialNumber != null ? meta.serialNumber : String.valueOf(pair.deviceId);
                    sendJson(pair.adminSession, TerminalMessage.error(pair.sessionId,
                            "The monitoring agent for device " + devLabel + " disconnected unexpectedly."));
                    pair.adminSession.close(new CloseStatus(4005, "Agent disconnected"));
                } catch (Exception ignored) {}
            }
        }
    }

    private void updateSessionStatus(String sessionId, String status, String error) {
        if (sessionId == null) return;
        try {
            sessionRepository.findBySessionId(sessionId).ifPresent(s -> {
                s.setStatus(status);
                if ("CLOSED".equals(status) || "DISCONNECTED".equals(status) || "FAILED".equals(status)) {
                    s.setEndedAt(LocalDateTime.now());
                }
                if (error != null) {
                    s.setErrorMessage(error);
                }
                sessionRepository.save(s);
            });
        } catch (Exception e) {
            log.warn("[AgentTerminalWS] Could not update session status for '{}': {}", sessionId, e.getMessage());
        }
    }

    private void sendJson(WebSocketSession session, Object obj) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(obj)));
            }
        }
    }
}

