package org.example.agent.terminal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.agent.config.AgentConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Maintains an outbound WebSocket connection from the employee laptop to the central
 * Spring Boot terminal relay. Works behind NAT, firewalls, and across different Wi-Fi networks.
 */
public class AgentTerminalClient implements WebSocket.Listener {

    private final String serialNumber;
    private final String hostname;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile WebSocket webSocket;
    private volatile TerminalManager activeTerminalManager;
    private volatile String currentSessionId;

    private final AtomicBoolean shouldRun = new AtomicBoolean(true);
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AgentTerminal-Heartbeat");
        t.setDaemon(true);
        return t;
    });

    public AgentTerminalClient(String serialNumber, String hostname) {
        this.serialNumber = serialNumber;
        this.hostname = hostname;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static void startInBackground(String serialNumber, String hostname) {
        Thread worker = new Thread(() -> {
            AgentTerminalClient client = new AgentTerminalClient(serialNumber, hostname);
            client.runLoop();
        }, "AgentTerminalClient-Worker");
        worker.setDaemon(true);
        worker.start();
    }

    public void runLoop() {
        System.out.println("[AgentTerminalClient] Starting background terminal relay client...");

        int backoffSeconds = 5;

        while (shouldRun.get()) {
            try {
                String wsUrl = AgentConfig.getWebSocketUrl("/ws/terminal/agent");
                System.out.println("[AgentTerminalClient] Connecting outbound to: " + wsUrl);

                CompletableFuture<WebSocket> wsFuture = httpClient.newWebSocketBuilder()
                        .connectTimeout(Duration.ofSeconds(15))
                        .buildAsync(URI.create(wsUrl), this);

                this.webSocket = wsFuture.get(20, TimeUnit.SECONDS);
                System.out.println("[AgentTerminalClient] WebSocket connected successfully to terminal relay!");

                // Connection established -> reset backoff
                backoffSeconds = 5;

                // Send initial registration handshake
                sendRegisterMessage();

                // Wait until disconnected
                synchronized (this) {
                    while (this.webSocket != null && shouldRun.get()) {
                        this.wait(30_000);
                        // Send periodic heartbeat to keep the TCP socket open through proxies
                        sendHeartbeat();
                    }
                }

            } catch (Exception e) {
                System.err.println("[AgentTerminalClient] Connection attempt failed: " + e.getMessage());
            }

            // Clean up any stale terminal if connection dropped
            closeCurrentTerminal();

            if (shouldRun.get()) {
                System.out.println("[AgentTerminalClient] Will retry connection in " + backoffSeconds + " seconds...");
                try {
                    Thread.sleep(backoffSeconds * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                // Exponential backoff up to 30s
                backoffSeconds = Math.min(backoffSeconds * 2, 30);
            }
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            String json = data.toString();
            JsonNode root = objectMapper.readTree(json);
            String messageType = root.path("messageType").asText("");
            String sessionId = root.path("sessionId").asText("");

            switch (messageType) {
                case "REGISTER_OK":
                    System.out.println("[AgentTerminalClient] Registered with relay. Device ID acknowledged: "
                            + root.path("deviceId").asText());
                    break;

                case "HEARTBEAT_ACK":
                    // Normal keep-alive acknowledgment
                    break;

                case "TERMINAL_REQUEST":
                    handleTerminalRequest(sessionId, root.path("payload").asText("powershell"));
                    break;

                case "TERMINAL_INPUT":
                    if (activeTerminalManager != null && activeTerminalManager.isRunning()) {
                        activeTerminalManager.writeInput(root.path("payload").asText(""));
                    }
                    break;

                case "TERMINAL_DISCONNECT":
                    System.out.println("[AgentTerminalClient] Received TERMINAL_DISCONNECT for session: " + sessionId);
                    closeCurrentTerminal();
                    sendJson(Map.of(
                            "messageType", "TERMINAL_CLOSED",
                            "sessionId", sessionId,
                            "timestamp", System.currentTimeMillis()
                    ));
                    break;

                default:
                    System.out.println("[AgentTerminalClient] Received message: " + messageType);
            }
        } catch (Exception e) {
            System.err.println("[AgentTerminalClient] Error processing incoming WebSocket message: " + e.getMessage());
        }

        webSocket.request(1);
        return null;
    }

    private synchronized void handleTerminalRequest(String sessionId, String shellType) {
        System.out.println("[AgentTerminalClient] Handling TERMINAL_REQUEST for session '" + sessionId + "' (shell=" + shellType + ")");

        // Close any pre-existing terminal session
        closeCurrentTerminal();

        this.currentSessionId = sessionId;

        this.activeTerminalManager = new TerminalManager(
                shellType,
                output -> {
                    // Send terminal output chunk to relay
                    sendJson(Map.of(
                            "messageType", "TERMINAL_OUTPUT",
                            "sessionId", sessionId,
                            "payload", output,
                            "timestamp", System.currentTimeMillis()
                    ));
                },
                () -> {
                    // On shell process exit
                    System.out.println("[AgentTerminalClient] Shell process exited for session: " + sessionId);
                    sendJson(Map.of(
                            "messageType", "TERMINAL_CLOSED",
                            "sessionId", sessionId,
                            "timestamp", System.currentTimeMillis()
                    ));
                    this.currentSessionId = null;
                    this.activeTerminalManager = null;
                }
        );

        boolean started = activeTerminalManager.start();
        if (started) {
            String banner = "Connected to " + hostname + " (" + shellType.toUpperCase() + ")\r\n";
            sendJson(Map.of(
                    "messageType", "TERMINAL_ACCEPTED",
                    "sessionId", sessionId,
                    "payload", banner,
                    "timestamp", System.currentTimeMillis()
            ));
        } else {
            sendJson(Map.of(
                    "messageType", "TERMINAL_ERROR",
                    "sessionId", sessionId,
                    "payload", "Failed to start shell process on employee laptop.",
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    private synchronized void closeCurrentTerminal() {
        if (activeTerminalManager != null) {
            try {
                activeTerminalManager.close();
            } catch (Exception ignored) {}
            activeTerminalManager = null;
        }
        currentSessionId = null;
    }

    private void sendRegisterMessage() {
        sendJson(Map.of(
                "messageType", "REGISTER",
                "serialNumber", serialNumber != null ? serialNumber : "",
                "payload", hostname != null ? hostname : "",
                "timestamp", System.currentTimeMillis()
        ));
    }

    private void sendHeartbeat() {
        sendJson(Map.of(
                "messageType", "HEARTBEAT",
                "timestamp", System.currentTimeMillis()
        ));
    }

    private synchronized void sendJson(Map<String, Object> map) {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            try {
                String json = objectMapper.writeValueAsString(map);
                webSocket.sendText(json, true);
            } catch (Exception e) {
                System.err.println("[AgentTerminalClient] Failed to send JSON message: " + e.getMessage());
            }
        }
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("[AgentTerminalClient] WebSocket closed (code: " + statusCode + ", reason: " + reason + ")");
        synchronized (this) {
            this.webSocket = null;
            this.notifyAll();
        }
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("[AgentTerminalClient] WebSocket error: " + error.getMessage());
        synchronized (this) {
            this.webSocket = null;
            this.notifyAll();
        }
    }
}

