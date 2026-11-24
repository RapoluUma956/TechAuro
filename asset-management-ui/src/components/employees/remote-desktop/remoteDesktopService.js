import { getAccessToken } from "../../../services/api";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "https://asset-management-backend-o8dh.onrender.com/api/v1";

export function getWsBaseUrl() {
  let base = API_BASE_URL;

  // Strip /api/v1 or /api
  base = base.replace(/\/api(\/v1)?\/?$/, "");

  // Convert HTTP -> WS, HTTPS -> WSS
  if (base.startsWith("https://")) {
    return "wss://" + base.substring("https://".length());
  } else if (base.startsWith("http://")) {
    return "ws://" + base.substring("http://".length());
  }

  // Fallback to current host
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}`;
}

export function buildRemoteDesktopWsUrl({ deviceId, serialNumber }) {
  const wsBase = getWsBaseUrl();
  const token = getAccessToken();

  const params = new URLSearchParams();
  if (token) params.append("token", token);
  if (deviceId) params.append("deviceId", String(deviceId));
  if (serialNumber) params.append("serialNumber", String(serialNumber));

  return `${wsBase}/ws/remote-desktop/admin?${params.toString()}`;
}

export function createRemoteDesktopConnection({
  deviceId,
  serialNumber,
  onFrame,
  onStatusChange,
  onError,
  onResolution,
  onClipboard,
  onFileProgress,
}) {
  let ws = null;
  let isClosed = false;
  let currentSessionId = null;
  let pingInterval = null;

  function connect() {
    onStatusChange("connecting", "Connecting to employee monitoring agent...");

    try {
      const url = buildRemoteDesktopWsUrl({ deviceId, serialNumber });
      ws = new WebSocket(url);
      ws.binaryType = "blob"; // Receive JPEG screen frames directly as binary Blob

      ws.onopen = () => {
        onStatusChange("connected", "Connected to remote desktop relay");
        // Start keepalive ping every 15s
        pingInterval = setInterval(() => {
          if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ messageType: "PING", timestamp: Date.now() }));
          }
        }, 15000);
      };

      ws.onmessage = async (event) => {
        // 1. Binary Frame handling (High Performance Screen Stream)
        if (event.data instanceof Blob) {
          try {
            const bitmap = await createImageBitmap(event.data);
            if (onFrame) {
              onFrame(bitmap);
            }
          } catch (err) {
            console.error("[RemoteDesktop] Error decoding frame bitmap:", err);
          }
          return;
        }

        // 2. JSON Control Messages
        try {
          const msg = JSON.parse(event.data);
          if (msg.sessionId) {
            currentSessionId = msg.sessionId;
          }

          switch (msg.messageType) {
            case "REQUEST_PENDING":
            case "PENDING_APPROVAL":
              onStatusChange(
                "pending_approval",
                msg.payload || "Waiting for employee approval on target device..."
              );
              break;

            case "APPROVED":
              onStatusChange("active", "Employee granted access. Streaming remote screen...");
              if (onResolution && msg.screenWidth && msg.screenHeight) {
                onResolution(msg.screenWidth, msg.screenHeight);
              }
              break;

            case "REJECTED":
              onStatusChange("rejected", msg.payload || "Employee rejected remote access request.");
              break;

            case "RESOLUTION":
              if (onResolution && msg.screenWidth && msg.screenHeight) {
                onResolution(msg.screenWidth, msg.screenHeight);
              }
              break;

            case "CLIPBOARD":
              if (onClipboard && msg.payload) {
                onClipboard(msg.payload);
              }
              break;

            case "FILE_TRANSFER_PROGRESS":
              if (onFileProgress) {
                onFileProgress(msg);
              }
              break;

            case "DISCONNECTED":
              onStatusChange("disconnected", msg.payload || "Remote session ended.");
              break;

            case "ERROR":
              if (onError) onError(msg.payload || "Remote desktop relay error");
              onStatusChange("error", msg.payload || "Connection error");
              break;

            case "PONG":
              break;

            default:
              break;
          }
        } catch (parseErr) {
          console.warn("[RemoteDesktop] Non-JSON text message received:", event.data);
        }
      };

      ws.onerror = () => {
        if (!isClosed) {
          onError("WebSocket connection failed. Please verify that the employee laptop is online.");
          onStatusChange("error", "Connection error");
        }
      };

      ws.onclose = (event) => {
        if (pingInterval) clearInterval(pingInterval);
        if (!isClosed) {
          const reason = event.reason || (event.code === 1000 ? "Session closed" : `Connection closed (Code: ${event.code})`);
          onStatusChange("disconnected", reason);
        }
      };
    } catch (err) {
      onError(err.message || "Failed to establish remote desktop connection");
      onStatusChange("error", err.message);
    }
  }

  function sendInput(inputObj) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(
        JSON.stringify({
          messageType: "INPUT",
          sessionId: currentSessionId,
          ...inputObj,
          timestamp: Date.now(),
        })
      );
    }
  }

  function sendClipboard(text) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(
        JSON.stringify({
          messageType: "CLIPBOARD",
          sessionId: currentSessionId,
          payload: text,
          timestamp: Date.now(),
        })
      );
    }
  }

  async function sendFile(file, onProgress) {
    if (!ws || ws.readyState !== WebSocket.OPEN) return;

    const CHUNK_SIZE = 64 * 1024; // 64KB chunks
    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);

    // Send metadata first
    ws.send(
      JSON.stringify({
        messageType: "FILE_TRANSFER_META",
        sessionId: currentSessionId,
        fileName: file.name,
        fileSize: file.size,
        totalChunks,
        timestamp: Date.now(),
      })
    );

    // Read and send chunks
    for (let i = 0; i < totalChunks; i++) {
      if (ws.readyState !== WebSocket.OPEN) break;
      const start = i * CHUNK_SIZE;
      const end = Math.min(start + CHUNK_SIZE, file.size);
      const slice = file.slice(start, end);
      const buffer = await slice.arrayBuffer();
      const base64Data = btoa(
        new Uint8Array(buffer).reduce((data, byte) => data + String.fromCharCode(byte), "")
      );

      ws.send(
        JSON.stringify({
          messageType: "FILE_TRANSFER_CHUNK",
          sessionId: currentSessionId,
          fileName: file.name,
          chunkIndex: i,
          totalChunks,
          payload: base64Data,
          timestamp: Date.now(),
        })
      );

      if (onProgress) {
        onProgress(Math.round(((i + 1) / totalChunks) * 100));
      }
    }
  }

  function disconnect() {
    isClosed = true;
    if (pingInterval) clearInterval(pingInterval);
    if (ws) {
      try {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(
            JSON.stringify({
              messageType: "DISCONNECT",
              sessionId: currentSessionId,
              timestamp: Date.now(),
            })
          );
        }
        ws.close(1000, "Admin closed session");
      } catch (ignored) {}
      ws = null;
    }
  }

  connect();

  return {
    sendInput,
    sendClipboard,
    sendFile,
    disconnect,
    getSessionId: () => currentSessionId,
  };
}
