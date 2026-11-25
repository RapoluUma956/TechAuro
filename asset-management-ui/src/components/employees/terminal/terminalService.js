import { getAccessToken, getWsBaseUrl } from "../../../services/api";

export { getWsBaseUrl };

export function buildTerminalWsUrl({ deviceId, serialNumber, shell = "powershell" }) {
  const wsBase = getWsBaseUrl();
  const token = getAccessToken();

  const params = new URLSearchParams();
  if (token) params.append("token", token);
  if (deviceId) params.append("deviceId", String(deviceId));
  if (serialNumber) params.append("serialNumber", String(serialNumber));
  params.append("shell", shell);

  return `${wsBase}/ws/terminal/admin?${params.toString()}`;
}

export function createTerminalConnection({
  deviceId,
  serialNumber,
  shell = "powershell",
  onOutput,
  onStatusChange,
  onError,
}) {
  let ws = null;
  let isClosed = false;
  let currentSessionId = null;

  function connect() {
    onStatusChange("connecting", "Connecting to employee monitoring agent...");

    try {
      const url = buildTerminalWsUrl({ deviceId, serialNumber, shell });
      ws = new WebSocket(url);

      ws.onopen = () => {
        onStatusChange("connected", "Connected to terminal relay");
      };

      ws.onmessage = (event) => {
        try {
          const msg = JSON.parse(event.data);
          if (msg.sessionId) {
            currentSessionId = msg.sessionId;
          }

          switch (msg.messageType) {
            case "SESSION_INITIALIZING":
              onStatusChange("initializing", msg.payload || "Waiting for agent shell...");
              if (onOutput && msg.payload) {
                onOutput(`\r\n\x1b[36m[INFO] ${msg.payload}\x1b[0m\r\n`);
              }
              break;

            case "TERMINAL_ACCEPTED":
              onStatusChange("active", "Active Shell Session");
              if (onOutput) {
                const banner = msg.payload || "Session established.\r\n";
                onOutput(`\x1b[32m[OK] Remote session authorized.\x1b[0m\r\n\r\n${banner}`);
              }
              break;

            case "TERMINAL_OUTPUT":
              if (onOutput && msg.payload) {
                onOutput(msg.payload);
              }
              break;

            case "TERMINAL_ERROR":
              onStatusChange("error", msg.payload || "Terminal error");
              if (onError) onError(msg.payload);
              if (onOutput && msg.payload) {
                onOutput(`\r\n\x1b[31m[ERROR] ${msg.payload}\x1b[0m\r\n`);
              }
              break;

            case "TERMINAL_CLOSED":
              onStatusChange("disconnected", "Session closed by agent");
              if (onOutput) {
                onOutput("\r\n\x1b[33m[Disconnected] Session ended.\x1b[0m\r\n");
              }
              break;

            default:
              break;
          }
        } catch {
          // If raw text
          if (onOutput && event.data) {
            onOutput(event.data);
          }
        }
      };

      ws.onerror = () => {
        onStatusChange("error", "WebSocket communication error with relay");
        if (onError) onError("WebSocket connection error");
      };

      ws.onclose = (event) => {
        if (!isClosed) {
          const reason = event.reason || (event.code === 4004 ? "Agent offline or not connected" : "Connection closed");
          onStatusChange("disconnected", `Disconnected (Code: ${event.code})`);
          if (onOutput) {
            onOutput(`\r\n\x1b[33m[Disconnected] Session closed (${reason})\x1b[0m\r\n`);
          }
        }
      };
    } catch (err) {
      onStatusChange("error", err.message || "Failed to initialize WebSocket");
      if (onError) onError(err.message);
    }
  }

  function sendInput(text) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(
        JSON.stringify({
          messageType: "TERMINAL_INPUT",
          sessionId: currentSessionId,
          deviceId: deviceId,
          payload: text,
          timestamp: Date.now(),
        })
      );
    }
  }

  function sendResize(cols, rows) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(
        JSON.stringify({
          messageType: "TERMINAL_RESIZE",
          sessionId: currentSessionId,
          deviceId: deviceId,
          cols: cols,
          rows: rows,
          timestamp: Date.now(),
        })
      );
    }
  }

  function disconnect() {
    isClosed = true;
    if (ws) {
      if (ws.readyState === WebSocket.OPEN) {
        try {
          ws.send(
            JSON.stringify({
              messageType: "TERMINAL_DISCONNECT",
              sessionId: currentSessionId,
              deviceId: deviceId,
              timestamp: Date.now(),
            })
          );
        } catch {}
      }
      try {
        ws.close(1000, "Admin closed session");
      } catch {}
      ws = null;
    }
  }

  connect();

  return {
    sendInput,
    sendResize,
    disconnect,
    reconnect: () => {
      disconnect();
      isClosed = false;
      connect();
    },
  };
}
