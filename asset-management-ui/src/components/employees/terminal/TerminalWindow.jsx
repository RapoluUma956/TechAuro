import React, { useEffect, useRef, forwardRef, useImperativeHandle } from "react";
import { Terminal } from "@xterm/xterm";
import { FitAddon } from "@xterm/addon-fit";
import "@xterm/xterm/css/xterm.css";

const TerminalWindow = forwardRef(function TerminalWindow(
  { onInput, onResize, className = "" },
  ref
) {
  const containerRef = useRef(null);
  const termRef = useRef(null);
  const fitAddonRef = useRef(null);

  useImperativeHandle(ref, () => ({
    write: (data) => {
      if (termRef.current) {
        termRef.current.write(data);
      }
    },
    clear: () => {
      if (termRef.current) {
        termRef.current.clear();
      }
    },
    focus: () => {
      if (termRef.current) {
        termRef.current.focus();
      }
    },
    getSelection: () => {
      if (termRef.current) {
        return termRef.current.getSelection();
      }
      return "";
    },
  }));

  useEffect(() => {
    if (!containerRef.current) return;

    const term = new Terminal({
      cursorBlink: true,
      cursorStyle: "block",
      fontSize: 13,
      lineHeight: 1.25,
      fontFamily: 'Consolas, "Fira Code", "Courier New", monospace',
      scrollback: 5000,
      theme: {
        background: "#0A0E17",
        foreground: "#E2E8F0",
        cursor: "#38BDF8",
        selectionBackground: "rgba(56, 189, 248, 0.3)",
        black: "#0A0E17",
        red: "#EF4444",
        green: "#10B981",
        yellow: "#F59E0B",
        blue: "#3B82F6",
        magenta: "#A855F7",
        cyan: "#06B6D4",
        white: "#F8FAFC",
        brightBlack: "#475569",
        brightRed: "#F87171",
        brightGreen: "#34D399",
        brightYellow: "#FBBF24",
        brightBlue: "#60A5FA",
        brightMagenta: "#C084FC",
        brightCyan: "#22D3EE",
        brightWhite: "#FFFFFF",
      },
      convertEol: true,
    });

    const fitAddon = new FitAddon();
    term.loadAddon(fitAddon);

    term.open(containerRef.current);
    termRef.current = term;
    fitAddonRef.current = fitAddon;

    // Small timeout to allow DOM layout before fitting
    const timer = setTimeout(() => {
      try {
        fitAddon.fit();
        if (onResize) onResize(term.cols, term.rows);
      } catch {}
      term.focus();
    }, 100);

    // Keystroke listener
    const disposable = term.onData((data) => {
      if (onInput) {
        onInput(data);
      }
    });

    // Resize observer to keep xterm canvas fitted to modal container
    const resizeObserver = new ResizeObserver(() => {
      try {
        fitAddon.fit();
        if (onResize) onResize(term.cols, term.rows);
      } catch {}
    });
    resizeObserver.observe(containerRef.current);

    return () => {
      clearTimeout(timer);
      disposable.dispose();
      resizeObserver.disconnect();
      term.dispose();
      termRef.current = null;
      fitAddonRef.current = null;
    };
  }, []);

  return (
    <div
      ref={containerRef}
      className={`h-full w-full overflow-hidden rounded-b-xl bg-[#0A0E17] p-2 text-left select-text ${className}`}
      style={{ minHeight: "440px" }}
      tabIndex={0}
      onClick={() => termRef.current?.focus()}
    />
  );
});

export default TerminalWindow;

