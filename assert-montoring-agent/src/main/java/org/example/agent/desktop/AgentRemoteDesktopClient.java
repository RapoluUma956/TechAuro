package org.example.agent.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.agent.config.AgentConfig;

import java.awt.AWTException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Maintains an outbound WebSocket connection to the central Spring Boot
 * remote desktop relay (/ws/remote-desktop/agent).
 * Listens for remote access requests, handles employee consent, streams screen frames,
 * and executes remote mouse and keyboard inputs via Robot.
 */
public class AgentRemoteDesktopClient implements WebSocket.Listener {

    private final String serialNumber;
    private final String hostname;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile WebSocket webSocket;
    private volatile ScreenCaptureService screenCaptureService;
    private volatile RemoteInputService remoteInputService;
    private volatile String currentSessionId;

    private final AtomicBoolean shouldRun = new AtomicBoolean(true);
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AgentDesktop-Heartbeat");
        t.setDaemon(true);
        return t;
    });

    public AgentRemoteDesktopClient(String serialNumber, String hostname) {
        this.serialNumber = serialNumber;
        this.hostname = hostname;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static void startInBackground(String serialNumber, String hostname) {
        Thread worker = new Thread(() -> {
            AgentRemoteDesktopClient client = new AgentRemoteDesktopClient(serialNumber, hostname);
            client.runLoop();
        }, "AgentRemoteDesktopClient-Worker");
        worker.setDaemon(true);
        worker.start();
    }

    public void runLoop() {
        System.out.println("[AgentDesktopClient] Starting outbound remote desktop client...");

        int backoffSeconds = 5;

        while (shouldRun.get()) {
            try {
                String wsUrl = AgentConfig.getWebSocketUrl("/ws/remote-desktop/agent");
                System.out.println("[AgentDesktopClient] Connecting outbound to: " + wsUrl);

                CompletableFuture<WebSocket> wsFuture = httpClient.newWebSocketBuilder()
                        .connectTimeout(Duration.ofSeconds(15))
                        .buildAsync(URI.create(wsUrl), this);

                this.webSocket = wsFuture.get(20, TimeUnit.SECONDS);
                System.out.println("[AgentDesktopClient] WebSocket connected successfully to desktop relay!");

                backoffSeconds = 5;
                sendRegisterMessage();

                synchronized (this) {
                    while (this.webSocket != null && shouldRun.get()) {
                        this.wait();
                    }
                }

            } catch (Exception e) {
                System.err.println("[AgentDesktopClient] Connection failed: " + e.getMessage());
            } finally {
                stopActiveSession("Disconnected from relay");
                cleanupWebSocket();
            }

            if (shouldRun.get()) {
                try {
                    System.out.println("[AgentDesktopClient] Reconnecting in " + backoffSeconds + "s...");
                    Thread.sleep(backoffSeconds * 1000L);
                    backoffSeconds = Math.min(backoffSeconds * 2, 60);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void sendRegisterMessage() {
        try {
            Map<String, Object> reg = new HashMap<>();
            reg.put("messageType", "REGISTER");
            reg.put("serialNumber", serialNumber);
            reg.put("payload", hostname);
            reg.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(reg);
            if (webSocket != null) {
                webSocket.sendText(json, true);
                System.out.println("[AgentDesktopClient] Sent REGISTER for serial=" + serialNumber);
            }

            startHeartbeat();

        } catch (Exception e) {
            System.err.println("[AgentDesktopClient] Failed to send register message: " + e.getMessage());
        }
    }

    private void startHeartbeat() {
        heartbeatExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (webSocket != null) {
                    Map<String, Object> hb = Map.of(
                            "messageType", "HEARTBEAT",
                            "timestamp", System.currentTimeMillis()
                    );
                    webSocket.sendText(objectMapper.writeValueAsString(hb), true);
                }
            } catch (Exception ignored) {}
        }, 20, 20, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            String payload = data.toString();
            JsonNode root = objectMapper.readTree(payload);
            String messageType = root.path("messageType").asText(null);

            if (messageType == null) {
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            switch (messageType) {
                case "REMOTE_ACCESS_REQUEST":
                    handleRemoteAccessRequest(root);
                    break;

                case "INPUT":
                    handleInputMessage(root);
                    break;

                case "CLIPBOARD":
                    if (remoteInputService != null) {
                        remoteInputService.setClipboardText(root.path("payload").asText(""));
                    }
                    break;

                case "FILE_TRANSFER_META":
                    if (remoteInputService != null) {
                        String fName = root.path("fileName").asText();
                        long fSize = root.path("fileSize").asLong(0);
                        remoteInputService.handleFileTransferMeta(fName, fSize);
                    }
                    break;

                case "FILE_TRANSFER_CHUNK":
                    if (remoteInputService != null) {
                        String fName = root.path("fileName").asText();
                        String fData = root.path("payload").asText();
                        int cIdx = root.path("chunkIndex").asInt(0);
                        int total = root.path("totalChunks").asInt(1);
                        remoteInputService.handleFileTransferChunk(fName, fData, cIdx + 1 >= total);
                    }
                    break;

                case "DISCONNECT":
                    stopActiveSession(root.path("payload").asText("Admin ended session"));
                    break;

                case "REGISTER_ACK":
                    System.out.println("[AgentDesktopClient] Registered with cloud relay: " + root.path("payload").asText());
                    break;

                case "HEARTBEAT_ACK":
                    break;

                default:
                    System.out.println("[AgentDesktopClient] Received unhandled messageType: " + messageType);
            }

        } catch (Exception e) {
            System.err.println("[AgentDesktopClient] Error processing onText: " + e.getMessage());
        }

        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    private void handleRemoteAccessRequest(JsonNode root) {
        String sessionId = root.path("sessionId").asText();
        String adminName = root.path("adminName").asText("IT Administrator");
        String adminEmail = root.path("adminEmail").asText("");

        this.currentSessionId = sessionId;

        // Run approval in separate thread so WebSocket listener doesn't block
        CompletableFuture.runAsync(() -> {
            System.out.println("[AgentDesktopClient] Prompting employee for approval from " + adminName + " (" + adminEmail + ")...");
            boolean approved = EmployeeApprovalDialog.requestApproval(adminName, adminEmail, serialNumber);

            if (approved) {
                System.out.println("[AgentDesktopClient] Employee APPROVED remote desktop request.");
                startActiveSession(sessionId, adminName);
            } else {
                System.out.println("[AgentDesktopClient] Employee REJECTED remote desktop request.");
                sendApprovalResponse(sessionId, false, 0, 0);
            }
        });
    }

    private synchronized void startActiveSession(String sessionId, String adminName) {
        try {
            if (screenCaptureService == null) {
                screenCaptureService = new ScreenCaptureService();
            }
            if (remoteInputService == null) {
                remoteInputService = new RemoteInputService();
            }

            int sw = screenCaptureService.getScreenWidth();
            int sh = screenCaptureService.getScreenHeight();

            // Send approval response with screen resolution
            sendApprovalResponse(sessionId, true, sw, sh);

            // Show persistent employee indicator with Disconnect button
            ActiveSessionOverlay.showOverlay(adminName, () -> {
                System.out.println("[AgentDesktopClient] Employee clicked disconnect overlay.");
                sendTermination(sessionId, "Employee clicked disconnect");
                stopActiveSession("Employee ended session");
            });

            // Start screen capture loop
            screenCaptureService.start(frameBuffer -> {
                try {
                    if (webSocket != null) {
                        webSocket.sendBinary(frameBuffer, true);
                    }
                } catch (Exception ignored) {}
            });

        } catch (AWTException e) {
            System.err.println("[AgentDesktopClient] AWT error initializing desktop services: " + e.getMessage());
            sendApprovalResponse(sessionId, false, 0, 0);
        }
    }

    private synchronized void stopActiveSession(String reason) {
        if (screenCaptureService != null) {
            screenCaptureService.stop();
        }
        ActiveSessionOverlay.hideOverlay();
        currentSessionId = null;
        System.out.println("[AgentDesktopClient] Active desktop session stopped: " + reason);
    }

    private void handleInputMessage(JsonNode root) {
        if (remoteInputService == null) return;

        String inputType = root.path("inputType").asText(null);
        Double x = root.has("x") ? root.path("x").asDouble() : null;
        Double y = root.has("y") ? root.path("y").asDouble() : null;
        Integer button = root.has("button") ? root.path("button").asInt() : null;
        Integer deltaY = root.has("deltaY") ? root.path("deltaY").asInt() : null;
        Integer keyCode = root.has("keyCode") ? root.path("keyCode").asInt() : null;
        String key = root.path("key").asText(null);

        remoteInputService.handleInput(inputType, x, y, button, deltaY, keyCode, key);
    }

    private void sendApprovalResponse(String sessionId, boolean approved, int width, int height) {
        try {
            Map<String, Object> resp = new HashMap<>();
            resp.put("messageType", "APPROVAL_RESPONSE");
            resp.put("sessionId", sessionId);
            resp.put("payload", approved ? "APPROVED" : "REJECTED");
            resp.put("screenWidth", width);
            resp.put("screenHeight", height);
            resp.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(resp);
            if (webSocket != null) {
                webSocket.sendText(json, true);
            }
        } catch (Exception e) {
            System.err.println("[AgentDesktopClient] Failed to send approval response: " + e.getMessage());
        }
    }

    private void sendTermination(String sessionId, String reason) {
        try {
            Map<String, Object> term = Map.of(
                    "messageType", "SESSION_TERMINATED",
                    "sessionId", sessionId != null ? sessionId : "",
                    "payload", reason,
                    "timestamp", System.currentTimeMillis()
            );
            if (webSocket != null) {
                webSocket.sendText(objectMapper.writeValueAsString(term), true);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("[AgentDesktopClient] WebSocket closed: " + statusCode + " - " + reason);
        cleanupWebSocket();
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("[AgentDesktopClient] WebSocket error: " + error.getMessage());
        cleanupWebSocket();
    }

    private synchronized void cleanupWebSocket() {
        if (webSocket != null) {
            try {
                webSocket.abort();
            } catch (Exception ignored) {}
            webSocket = null;
        }
        notifyAll();
    }
}
