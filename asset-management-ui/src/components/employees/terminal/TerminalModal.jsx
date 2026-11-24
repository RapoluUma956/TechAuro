import React, { useState, useEffect, useRef } from "react";
import TerminalWindow from "./TerminalWindow";
import { createTerminalConnection } from "./terminalService";

export default function TerminalModal({ isOpen, onClose, device, employee }) {
  const [shell, setShell] = useState("powershell");
  const [status, setStatus] = useState("connecting");
  const [statusMessage, setStatusMessage] = useState("Connecting to agent...");
  const [terminalSize, setTerminalSize] = useState({ cols: 120, rows: 30 });
  const [copied, setCopied] = useState(false);
  const [sessionError, setSessionError] = useState(null);

  const terminalRef = useRef(null);
  const connRef = useRef(null);

  const deviceId = device?.deviceId || device?.device_id || device?.id;
  const serialNumber = device?.serialNumber || device?.serial_number || "";
  const deviceName = device?.deviceName || device?.device_name || "Windows Laptop";
  const hostname = device?.hostname || deviceName;
  const employeeName = employee?.employeeName || employee?.name || "Employee";
  const employeeId = employee?.employeeId || employee?.emp_id || "";

  useEffect(() => {
    if (!isOpen || !serialNumber) {
      if (connRef.current) {
        connRef.current.disconnect();
        connRef.current = null;
      }
      return;
    }

    setSessionError(null);
    setStatus("connecting");
    setStatusMessage("Connecting to employee monitoring agent...");

    const conn = createTerminalConnection({
      deviceId,
      serialNumber,
      shell,
      onOutput: (chunk) => {
        if (terminalRef.current) {
          terminalRef.current.write(chunk);
        }
      },
      onStatusChange: (newStatus, msg) => {
        setStatus(newStatus);
        if (msg) setStatusMessage(msg);
        if (newStatus === "error" || newStatus === "disconnected") {
          setSessionError(msg);
        } else if (newStatus === "active" || newStatus === "connected") {
          setSessionError(null);
        }
      },
      onError: (err) => {
        setSessionError(err);
      },
    });

    connRef.current = conn;

    return () => {
      if (connRef.current) {
        connRef.current.disconnect();
        connRef.current = null;
      }
    };
  }, [isOpen, deviceId, serialNumber, shell]);

  if (!isOpen) return null;

  const handleInput = (data) => {
    if (connRef.current) {
      connRef.current.sendInput(data);
    }
  };

  const handleResize = (cols, rows) => {
    setTerminalSize({ cols, rows });
    if (connRef.current) {
      connRef.current.sendResize(cols, rows);
    }
  };

  const handleCopy = () => {
    const selection = terminalRef.current?.getSelection();
    if (selection) {
      navigator.clipboard.writeText(selection);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } else {
      // If no text selected, inform user or copy general notice
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    }
  };

  const handleClear = () => {
    if (terminalRef.current) {
      terminalRef.current.clear();
      terminalRef.current.focus();
    }
  };

  const handleReconnect = () => {
    if (terminalRef.current) {
      terminalRef.current.clear();
    }
    setSessionError(null);
    if (connRef.current) {
      connRef.current.reconnect();
    }
  };

  const handleShellChange = (newShell) => {
    if (newShell === shell) return;
    if (terminalRef.current) {
      terminalRef.current.clear();
    }
    setShell(newShell);
  };

  const handleClose = () => {
    if (connRef.current) {
      connRef.current.disconnect();
      connRef.current = null;
    }
    onClose();
  };

  // Status Badge Styling
  const getStatusBadge = () => {
    switch (status) {
      case "active":
        return (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-500/10 px-2.5 py-0.5 text-xs font-semibold text-emerald-400 border border-emerald-500/30">
            <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
            Connected
          </span>
        );
      case "connecting":
      case "initializing":
        return (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-500/10 px-2.5 py-0.5 text-xs font-semibold text-amber-400 border border-amber-500/30">
            <span className="h-1.5 w-1.5 rounded-full bg-amber-400 animate-pulse" />
            Connecting...
          </span>
        );
      case "error":
        return (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-rose-500/10 px-2.5 py-0.5 text-xs font-semibold text-rose-400 border border-rose-500/30">
            <span className="h-1.5 w-1.5 rounded-full bg-rose-400" />
            Error
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-500/10 px-2.5 py-0.5 text-xs font-semibold text-slate-400 border border-slate-500/30">
            <span className="h-1.5 w-1.5 rounded-full bg-slate-400" />
            Disconnected
          </span>
        );
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-5 bg-black/75 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="flex flex-col h-[90vh] w-full max-w-6xl overflow-hidden rounded-2xl border border-slate-700 bg-[#0B0F19] text-slate-100 shadow-2xl">
        {/* Top Header Bar */}
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 bg-[#0F172A] px-5 py-3.5">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-800 text-emerald-400 font-mono font-bold text-sm border border-slate-700">
              &gt;_
            </div>
            <div>
              <div className="flex items-center gap-2.5">
                <h2 className="text-base font-bold text-white tracking-tight">
                  Remote {shell === "powershell" ? "PowerShell" : "Command Prompt"} Terminal
                </h2>
                {getStatusBadge()}
              </div>
              <div className="flex flex-wrap items-center gap-2 text-xs text-slate-400 mt-0.5">
                {employeeId && <span className="font-mono text-slate-300 font-semibold">{employeeId}</span>}
                {employeeName && <span>• {employeeName}</span>}
                <span>• Serial: <span className="font-mono text-cyan-400 font-semibold">{serialNumber}</span></span>
                {hostname && <span>• Hostname: <span className="text-slate-300">{hostname}</span></span>}
              </div>
            </div>
          </div>

          {/* Right Action Controls */}
          <div className="flex items-center gap-2">
            {/* Shell Selector */}
            <div className="flex items-center rounded-lg bg-slate-800 p-0.5 border border-slate-700 text-xs mr-2">
              <button
                type="button"
                onClick={() => handleShellChange("powershell")}
                className={`px-2.5 py-1 rounded-md font-semibold transition-colors cursor-pointer ${
                  shell === "powershell"
                    ? "bg-[#1E293B] text-cyan-400 shadow-xs border border-slate-600"
                    : "text-slate-400 hover:text-slate-200"
                }`}
              >
                PowerShell
              </button>
              <button
                type="button"
                onClick={() => handleShellChange("cmd")}
                className={`px-2.5 py-1 rounded-md font-semibold transition-colors cursor-pointer ${
                  shell === "cmd"
                    ? "bg-[#1E293B] text-cyan-400 shadow-xs border border-slate-600"
                    : "text-slate-400 hover:text-slate-200"
                }`}
              >
                CMD
              </button>
            </div>

            <button
              type="button"
              onClick={handleCopy}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs font-semibold text-slate-200 hover:bg-slate-700 transition-colors cursor-pointer"
              title="Copy selection from terminal"
            >
              <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              <span>{copied ? "Copied!" : "Copy"}</span>
            </button>

            <button
              type="button"
              onClick={handleClear}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs font-semibold text-slate-200 hover:bg-slate-700 transition-colors cursor-pointer"
              title="Clear terminal screen"
            >
              <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
              </svg>
              <span>Clear</span>
            </button>

            <button
              type="button"
              onClick={handleReconnect}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs font-semibold text-emerald-400 hover:bg-emerald-950/40 hover:border-emerald-600 transition-colors cursor-pointer"
              title="Reconnect terminal session"
            >
              <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="23 4 23 10 17 10" />
                <polyline points="1 20 1 14 7 14" />
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
              </svg>
              <span>Reconnect</span>
            </button>

            <button
              type="button"
              onClick={handleClose}
              className="inline-flex items-center justify-center h-8 w-8 rounded-lg bg-rose-500/10 text-rose-400 border border-rose-500/30 hover:bg-rose-500/20 transition-colors cursor-pointer"
              title="Disconnect and close terminal"
            >
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
        </div>

        {/* Info & Notification Banner */}
        <div className="flex flex-wrap items-center justify-between gap-3 bg-[#0A0E1A] px-5 py-2 text-[11px] text-slate-400 border-b border-slate-800/80">
          <div className="flex items-center gap-2">
            <span className="font-semibold text-emerald-400">Session Notice:</span>
            <span>Commands execute live on the selected Windows laptop via persistent {shell.toUpperCase()} session.</span>
          </div>
          <div className="flex items-center gap-4 text-slate-400">
            <span>Size: {terminalSize.cols}×{terminalSize.rows}</span>
            <span>•</span>
            <span>Ctrl+C: Interrupt</span>
            <span>•</span>
            <span>Tab: Auto-complete</span>
          </div>
        </div>

        {/* Terminal Window Canvas & Overlay States */}
        <div className="relative flex-1 bg-[#0A0E17] overflow-hidden">
          <TerminalWindow
            ref={terminalRef}
            onInput={handleInput}
            onResize={handleResize}
          />

          {/* Overlay if agent is offline or disconnected */}
          {(status === "error" || (status === "disconnected" && sessionError)) && (
            <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-[#0B0F19]/90 backdrop-blur-xs p-6 text-center animate-in fade-in duration-300">
              <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/30 mb-4">
                <svg className="h-7 w-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
              </div>

              <h3 className="text-base font-bold text-white mb-1.5">
                Remote Terminal Unavailable
              </h3>
              <p className="max-w-md text-xs text-slate-300 leading-relaxed mb-5">
                {sessionError || "The monitoring agent on this employee laptop is currently offline or not connected to the terminal relay."}
              </p>

              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={handleReconnect}
                  className="rounded-xl bg-[#1976D2] hover:bg-[#1565C0] text-white px-4 py-2 text-xs font-bold transition-colors cursor-pointer shadow-sm"
                >
                  Retry Connection
                </button>
                <button
                  type="button"
                  onClick={handleClose}
                  className="rounded-xl border border-slate-700 bg-slate-800 hover:bg-slate-700 text-slate-200 px-4 py-2 text-xs font-semibold transition-colors cursor-pointer"
                >
                  Close Window
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Bottom Footer Status Bar */}
        <div className="flex items-center justify-between border-t border-slate-800 bg-[#0F172A] px-5 py-2 text-xs text-slate-400">
          <div className="flex items-center gap-2">
            <span className={`h-2 w-2 rounded-full ${status === "active" ? "bg-emerald-400" : status === "connecting" ? "bg-amber-400" : "bg-slate-500"}`} />
            <span className="font-mono text-[11px] text-slate-300">
              {shell === "powershell" ? "Windows PowerShell 5/7" : "Windows Command Prompt (cmd.exe)"}
            </span>
          </div>
          <div className="text-[11px] text-slate-400">
            {statusMessage}
          </div>
        </div>
      </div>
    </div>
  );
}

