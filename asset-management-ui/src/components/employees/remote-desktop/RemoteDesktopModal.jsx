import React, { useState, useEffect, useRef } from "react";
import ScreenCanvas from "./ScreenCanvas";
import { createRemoteDesktopConnection } from "./remoteDesktopService";

export default function RemoteDesktopModal({ isOpen, onClose, device, employee }) {
  const [status, setStatus] = useState("connecting"); // connecting, pending_approval, active, rejected, disconnected, error
  const [statusMessage, setStatusMessage] = useState("Connecting to agent...");
  const [currentFrame, setCurrentFrame] = useState(null);
  const [resolution, setResolution] = useState({ width: 1920, height: 1080 });
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isClipboardOpen, setIsClipboardOpen] = useState(false);
  const [clipboardText, setClipboardText] = useState("");
  const [fileProgress, setFileProgress] = useState(null); // { fileName, percent }
  const [fps, setFps] = useState(0);

  const modalRef = useRef(null);
  const connRef = useRef(null);
  const fileInputRef = useRef(null);
  const frameCountRef = useRef(0);
  const fpsTimerRef = useRef(null);

  const deviceId = device?.deviceId || device?.device_id || device?.id;
  const serialNumber = device?.serialNumber || device?.serial_number || "";
  const deviceName = device?.deviceName || device?.device_name || "Windows Laptop";
  const hostname = device?.hostname || deviceName;
  const employeeName = employee?.employeeName || employee?.name || "Employee";

  // Measure FPS
  useEffect(() => {
    if (status === "active") {
      fpsTimerRef.current = setInterval(() => {
        setFps(frameCountRef.current);
        frameCountRef.current = 0;
      }, 1000);
    } else {
      if (fpsTimerRef.current) clearInterval(fpsTimerRef.current);
      setFps(0);
    }
    return () => {
      if (fpsTimerRef.current) clearInterval(fpsTimerRef.current);
    };
  }, [status]);

  // Connect on modal open
  useEffect(() => {
    if (!isOpen || !serialNumber) {
      if (connRef.current) {
        connRef.current.disconnect();
        connRef.current = null;
      }
      return;
    }

    setStatus("connecting");
    setStatusMessage("Connecting to employee monitoring agent...");
    setCurrentFrame(null);

    const conn = createRemoteDesktopConnection({
      deviceId,
      serialNumber,
      onFrame: (bitmap) => {
        setCurrentFrame(bitmap);
        frameCountRef.current += 1;
      },
      onStatusChange: (newStatus, msg) => {
        setStatus(newStatus);
        if (msg) setStatusMessage(msg);
      },
      onError: (errMsg) => {
        setStatus("error");
        setStatusMessage(errMsg);
      },
      onResolution: (width, height) => {
        setResolution({ width, height });
      },
      onClipboard: (text) => {
        setClipboardText(text);
      },
      onFileProgress: (info) => {
        setFileProgress(info);
      },
    });

    connRef.current = conn;

    return () => {
      if (connRef.current) {
        connRef.current.disconnect();
        connRef.current = null;
      }
    };
  }, [isOpen, deviceId, serialNumber]);

  // Handle Fullscreen toggle
  const toggleFullscreen = () => {
    if (!modalRef.current) return;
    if (!document.fullscreenElement) {
      modalRef.current.requestFullscreen().catch(() => {});
      setIsFullscreen(true);
    } else {
      document.exitFullscreen().catch(() => {});
      setIsFullscreen(false);
    }
  };

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(Boolean(document.fullscreenElement));
    };
    document.addEventListener("fullscreenchange", handleFullscreenChange);
    return () => {
      document.removeEventListener("fullscreenchange", handleFullscreenChange);
    };
  }, []);

  if (!isOpen) return null;

  const handleInput = (inputObj) => {
    if (connRef.current && status === "active") {
      connRef.current.sendInput(inputObj);
    }
  };

  const handleSendClipboard = () => {
    if (connRef.current && clipboardText) {
      connRef.current.sendClipboard(clipboardText);
      setIsClipboardOpen(false);
    }
  };

  const handleFileUpload = (e) => {
    const file = e.target.files?.[0];
    if (file && connRef.current) {
      setFileProgress({ fileName: file.name, percent: 0 });
      connRef.current.sendFile(file, (percent) => {
        setFileProgress({ fileName: file.name, percent });
        if (percent >= 100) {
          setTimeout(() => setFileProgress(null), 3000);
        }
      });
    }
  };

  const handleSendCtrlAltDel = () => {
    if (connRef.current && status === "active") {
      // Simulate Ctrl + Alt + Delete
      connRef.current.sendInput({ inputType: "KEY_DOWN", keyCode: 17, key: "Control" });
      connRef.current.sendInput({ inputType: "KEY_DOWN", keyCode: 18, key: "Alt" });
      connRef.current.sendInput({ inputType: "KEY_DOWN", keyCode: 46, key: "Delete" });
      setTimeout(() => {
        connRef.current?.sendInput({ inputType: "KEY_UP", keyCode: 46, key: "Delete" });
        connRef.current?.sendInput({ inputType: "KEY_UP", keyCode: 18, key: "Alt" });
        connRef.current?.sendInput({ inputType: "KEY_UP", keyCode: 17, key: "Control" });
      }, 100);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-2 sm:p-4 animate-in fade-in duration-150">
      <div
        ref={modalRef}
        className={`flex flex-col bg-[#0F172A] border border-slate-700 shadow-2xl rounded-2xl overflow-hidden transition-all ${
          isFullscreen ? "fixed inset-0 w-screen h-screen rounded-none border-none" : "w-full max-w-6xl h-[88vh]"
        }`}
      >
        {/* Header Bar */}
        <div className="flex items-center justify-between px-5 py-3.5 bg-slate-900 border-b border-slate-800 text-white select-none">
          {/* Left: Device Info */}
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-blue-600/20 text-blue-400 border border-blue-500/30">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2" />
                <line x1="8" y1="21" x2="16" y2="21" />
                <line x1="12" y1="17" x2="12" y2="21" />
              </svg>
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-bold text-sm tracking-wide text-white">{deviceName}</span>
                <span className="font-mono text-xs px-2 py-0.5 rounded-md bg-slate-800 text-slate-300 border border-slate-700">
                  {serialNumber}
                </span>
                <span className="text-xs text-slate-400">({employeeName})</span>
              </div>
              <div className="flex items-center gap-3 mt-0.5 text-xs text-slate-400 font-mono">
                <span>Host: {hostname}</span>
                {status === "active" && (
                  <>
                    <span>•</span>
                    <span className="text-emerald-400">
                      {resolution.width}×{resolution.height}
                    </span>
                    <span>•</span>
                    <span className="text-blue-400 font-semibold">{fps} FPS</span>
                  </>
                )}
              </div>
            </div>
          </div>

          {/* Center: Live Status Indicator */}
          <div className="hidden md:flex items-center gap-2">
            {status === "connecting" && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/30">
                <span className="h-2 w-2 rounded-full bg-amber-400 animate-pulse" />
                Connecting...
              </span>
            )}
            {status === "pending_approval" && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-500/15 text-amber-300 border border-amber-500/40 animate-pulse">
                <span className="h-2 w-2 rounded-full bg-amber-400" />
                Awaiting Employee Consent
              </span>
            )}
            {status === "active" && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                <span className="h-2 w-2 rounded-full bg-emerald-400 animate-ping" />
                Live Remote Desktop
              </span>
            )}
            {status === "rejected" && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/30">
                <span className="h-2 w-2 rounded-full bg-rose-400" />
                Declined by Employee
              </span>
            )}
            {status === "disconnected" && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-slate-700/50 text-slate-300 border border-slate-600">
                Session Disconnected
              </span>
            )}
            {status === "error" && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-rose-500/15 text-rose-400 border border-rose-500/30">
                Connection Failed
              </span>
            )}
          </div>

          {/* Right: Actions */}
          <div className="flex items-center gap-2">
            {status === "active" && (
              <>
                <button
                  type="button"
                  onClick={handleSendCtrlAltDel}
                  className="px-2.5 py-1 text-xs font-medium rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 border border-slate-700 transition-colors"
                  title="Send Ctrl+Alt+Del macro"
                >
                  Ctrl+Alt+Del
                </button>

                <button
                  type="button"
                  onClick={() => setIsClipboardOpen(true)}
                  className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
                  title="Share Clipboard Text"
                >
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  </svg>
                </button>

                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleFileUpload}
                  className="hidden"
                />
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
                  title="Send File to Employee Laptop"
                >
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                  </svg>
                </button>

                <button
                  type="button"
                  onClick={toggleFullscreen}
                  className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
                  title={isFullscreen ? "Exit Fullscreen" : "Fullscreen View"}
                >
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    {isFullscreen ? (
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                    ) : (
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
                    )}
                  </svg>
                </button>
              </>
            )}

            <button
              type="button"
              onClick={() => {
                if (connRef.current) connRef.current.disconnect();
                onClose();
              }}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold shadow-xs transition-colors"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
              <span>Disconnect</span>
            </button>
          </div>
        </div>

        {/* File Transfer Toast */}
        {fileProgress && (
          <div className="bg-blue-600 text-white text-xs px-4 py-2 flex items-center justify-between">
            <span className="font-semibold">
              Uploading {fileProgress.fileName}: {fileProgress.percent}%
            </span>
            <div className="w-32 bg-blue-800 rounded-full h-2 overflow-hidden">
              <div
                className="bg-white h-full transition-all duration-200"
                style={{ width: `${fileProgress.percent}%` }}
              />
            </div>
          </div>
        )}

        {/* Content Area */}
        <div className="flex-1 relative bg-black flex items-center justify-center overflow-hidden">
          {status === "active" ? (
            <ScreenCanvas
              frame={currentFrame}
              screenWidth={resolution.width}
              screenHeight={resolution.height}
              onInput={handleInput}
              isFullscreen={isFullscreen}
            />
          ) : (
            <div className="max-w-md w-full mx-4 p-8 bg-slate-900 border border-slate-800 rounded-2xl shadow-xl text-center text-slate-300">
              {status === "connecting" && (
                <div className="flex flex-col items-center">
                  <div className="w-12 h-12 rounded-full border-4 border-blue-500/20 border-t-blue-500 animate-spin mb-4" />
                  <h3 className="text-base font-bold text-white mb-1">Connecting to Remote Agent</h3>
                  <p className="text-xs text-slate-400">{statusMessage}</p>
                </div>
              )}

              {status === "pending_approval" && (
                <div className="flex flex-col items-center">
                  <div className="relative w-16 h-16 flex items-center justify-center mb-4">
                    <span className="absolute inset-0 rounded-full bg-amber-400/20 animate-ping" />
                    <div className="relative w-12 h-12 rounded-full bg-amber-500/20 border border-amber-500 flex items-center justify-center text-amber-400">
                      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                      </svg>
                    </div>
                  </div>
                  <h3 className="text-base font-bold text-white mb-2">Awaiting Employee Approval</h3>
                  <p className="text-xs text-slate-300 leading-relaxed mb-4">
                    A consent dialog has appeared on the employee's screen ({serialNumber}).
                    The employee must click <strong className="text-blue-400">"Allow Access"</strong> before remote control begins.
                  </p>
                  <div className="text-[11px] text-slate-500 italic">Auto-declines in 60s if unattended</div>
                </div>
              )}

              {status === "rejected" && (
                <div className="flex flex-col items-center">
                  <div className="w-12 h-12 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center mb-4">
                    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
                    </svg>
                  </div>
                  <h3 className="text-base font-bold text-white mb-2">Access Request Declined</h3>
                  <p className="text-xs text-slate-400 mb-6">
                    {statusMessage || "The employee declined the remote desktop request."}
                  </p>
                  <button
                    type="button"
                    onClick={() => {
                      setStatus("connecting");
                      connRef.current?.disconnect();
                      // Re-trigger connection
                      const newConn = createRemoteDesktopConnection({
                        deviceId,
                        serialNumber,
                        onFrame: (bitmap) => {
                          setCurrentFrame(bitmap);
                          frameCountRef.current += 1;
                        },
                        onStatusChange: (newStatus, msg) => {
                          setStatus(newStatus);
                          if (msg) setStatusMessage(msg);
                        },
                        onError: (errMsg) => {
                          setStatus("error");
                          setStatusMessage(errMsg);
                        },
                        onResolution: (w, h) => setResolution({ width: w, height: h }),
                      });
                      connRef.current = newConn;
                    }}
                    className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold transition-colors"
                  >
                    Request Access Again
                  </button>
                </div>
              )}

              {(status === "disconnected" || status === "error") && (
                <div className="flex flex-col items-center">
                  <div className="w-12 h-12 rounded-full bg-slate-800 text-slate-400 flex items-center justify-center mb-4">
                    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <h3 className="text-base font-bold text-white mb-2">
                    {status === "error" ? "Remote Relay Error" : "Session Disconnected"}
                  </h3>
                  <p className="text-xs text-slate-400 mb-6 leading-relaxed">
                    {statusMessage || "The remote session has ended."}
                  </p>
                  <div className="flex items-center gap-3">
                    <button
                      type="button"
                      onClick={onClose}
                      className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold transition-colors"
                    >
                      Close Window
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setStatus("connecting");
                        connRef.current?.disconnect();
                        const newConn = createRemoteDesktopConnection({
                          deviceId,
                          serialNumber,
                          onFrame: (bitmap) => {
                            setCurrentFrame(bitmap);
                            frameCountRef.current += 1;
                          },
                          onStatusChange: (newStatus, msg) => {
                            setStatus(newStatus);
                            if (msg) setStatusMessage(msg);
                          },
                          onError: (errMsg) => {
                            setStatus("error");
                            setStatusMessage(errMsg);
                          },
                          onResolution: (w, h) => setResolution({ width: w, height: h }),
                        });
                        connRef.current = newConn;
                      }}
                      className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold transition-colors"
                    >
                      Reconnect
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Clipboard Sync Modal */}
        {isClipboardOpen && (
          <div className="fixed inset-0 z-60 flex items-center justify-center bg-black/60 p-4">
            <div className="bg-slate-900 border border-slate-700 rounded-xl p-5 max-w-md w-full shadow-2xl text-white">
              <h4 className="font-bold text-sm mb-2">Sync Clipboard with Remote Device</h4>
              <p className="text-xs text-slate-400 mb-3">
                Paste or type text below to set it into the employee's system clipboard:
              </p>
              <textarea
                value={clipboardText}
                onChange={(e) => setClipboardText(e.target.value)}
                rows={4}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-xs text-white font-mono focus:border-blue-500 focus:outline-none mb-4"
                placeholder="Type or paste text..."
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsClipboardOpen(false)}
                  className="px-3 py-1.5 text-xs rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleSendClipboard}
                  className="px-3 py-1.5 text-xs rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700"
                >
                  Send to Clipboard
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
