import React, { useRef, useEffect, useCallback, useState } from "react";

export default function ScreenCanvas({
  frame,
  screenWidth = 1920,
  screenHeight = 1080,
  onInput,
  isFullscreen,
}) {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const [isFocused, setIsFocused] = useState(false);
  const lastMoveTimeRef = useRef(0);
  const animFrameIdRef = useRef(null);

  // Render incoming frame bitmap to canvas
  useEffect(() => {
    if (!frame || !canvasRef.current) return;
    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    if (canvas.width !== frame.width || canvas.height !== frame.height) {
      canvas.width = frame.width;
      canvas.height = frame.height;
    }

    ctx.drawImage(frame, 0, 0, canvas.width, canvas.height);
  }, [frame]);

  // Helper to convert mouse event to normalized coordinates (0.0 to 1.0)
  const getNormalizedCoordinates = useCallback((e) => {
    if (!canvasRef.current) return { x: 0, y: 0 };
    const rect = canvasRef.current.getBoundingClientRect();
    const x = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    const y = Math.max(0, Math.min(1, (e.clientY - rect.top) / rect.height));
    return { x, y };
  }, []);

  const handleMouseMove = useCallback(
    (e) => {
      const now = performance.now();
      if (now - lastMoveTimeRef.current < 30) return; // ~30 FPS mouse throttling
      lastMoveTimeRef.current = now;

      const { x, y } = getNormalizedCoordinates(e);
      if (onInput) {
        onInput({
          inputType: "MOUSE_MOVE",
          x,
          y,
        });
      }
    },
    [getNormalizedCoordinates, onInput]
  );

  const handleMouseDown = useCallback(
    (e) => {
      const { x, y } = getNormalizedCoordinates(e);
      if (onInput) {
        onInput({
          inputType: "MOUSE_DOWN",
          x,
          y,
          button: e.button,
        });
      }
    },
    [getNormalizedCoordinates, onInput]
  );

  const handleMouseUp = useCallback(
    (e) => {
      const { x, y } = getNormalizedCoordinates(e);
      if (onInput) {
        onInput({
          inputType: "MOUSE_UP",
          x,
          y,
          button: e.button,
        });
      }
    },
    [getNormalizedCoordinates, onInput]
  );

  const handleClick = useCallback(
    (e) => {
      const { x, y } = getNormalizedCoordinates(e);
      if (onInput) {
        onInput({
          inputType: "CLICK",
          x,
          y,
          button: e.button,
        });
      }
    },
    [getNormalizedCoordinates, onInput]
  );

  const handleDoubleClick = useCallback(
    (e) => {
      const { x, y } = getNormalizedCoordinates(e);
      if (onInput) {
        onInput({
          inputType: "DOUBLE_CLICK",
          x,
          y,
          button: e.button,
        });
      }
    },
    [getNormalizedCoordinates, onInput]
  );

  const handleContextMenu = useCallback(
    (e) => {
      e.preventDefault();
      const { x, y } = getNormalizedCoordinates(e);
      if (onInput) {
        onInput({
          inputType: "CLICK",
          x,
          y,
          button: 2, // Right click
        });
      }
    },
    [getNormalizedCoordinates, onInput]
  );

  const handleWheel = useCallback(
    (e) => {
      e.preventDefault();
      const { x, y } = getNormalizedCoordinates(e);
      if (onInput) {
        onInput({
          inputType: "SCROLL",
          x,
          y,
          deltaY: e.deltaY > 0 ? 1 : -1,
        });
      }
    },
    [getNormalizedCoordinates, onInput]
  );

  const handleKeyDown = useCallback(
    (e) => {
      // Prevent browser shortcuts when canvas is focused
      if (
        ["Tab", "ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight", "Space", "Backspace"].includes(
          e.code
        )
      ) {
        e.preventDefault();
      }

      if (onInput) {
        onInput({
          inputType: "KEY_DOWN",
          keyCode: e.keyCode,
          key: e.key,
        });
      }
    },
    [onInput]
  );

  const handleKeyUp = useCallback(
    (e) => {
      if (onInput) {
        onInput({
          inputType: "KEY_UP",
          keyCode: e.keyCode,
          key: e.key,
        });
      }
    },
    [onInput]
  );

  return (
    <div
      ref={containerRef}
      className={`relative w-full h-full flex items-center justify-center bg-black overflow-hidden select-none outline-none ${
        isFullscreen ? "p-0" : "p-2"
      }`}
      tabIndex={0}
      onFocus={() => setIsFocused(true)}
      onBlur={() => setIsFocused(false)}
      onKeyDown={handleKeyDown}
      onKeyUp={handleKeyUp}
    >
      <canvas
        ref={canvasRef}
        onMouseMove={handleMouseMove}
        onMouseDown={handleMouseDown}
        onMouseUp={handleMouseUp}
        onClick={handleClick}
        onDoubleClick={handleDoubleClick}
        onContextMenu={handleContextMenu}
        onWheel={handleWheel}
        className="max-w-full max-h-full object-contain cursor-crosshair shadow-2xl transition-all"
        style={{
          aspectRatio: `${screenWidth} / ${screenHeight}`,
        }}
      />

      {/* Focus Pill Notification */}
      {!isFocused && (
        <div className="absolute bottom-4 left-1/2 -translate-x-1/2 pointer-events-none bg-slate-900/80 backdrop-blur-xs text-slate-300 text-xs px-3.5 py-1.5 rounded-full border border-slate-700/60 shadow-lg">
          Click on screen to enable remote keyboard input
        </div>
      )}
    </div>
  );
}
