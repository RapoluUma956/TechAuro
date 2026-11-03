import { useEffect, useRef, useCallback } from "react";

/**
 * Custom React hook for auto-refreshing active page data every 30 seconds.
 * 
 * - Only runs when the tab is currently visible and active (document.visibilityState === "visible").
 * - Pauses automatically when tab is hidden or minimized to prevent redundant network traffic.
 * - Immediately re-synchronizes data when the user switches back to the tab if 30+ seconds elapsed.
 * - Passes `isBackground: true` to the callback so the UI can update silently without full-screen spinners.
 *
 * @param {Function} refreshCallback - Function to execute on each refresh cycle.
 * @param {number} [intervalMs=30000] - Polling interval in milliseconds (default: 30,000ms / 30s).
 * @param {boolean} [enabled=true] - Whether auto-refresh is active.
 */
export function useAutoRefresh(refreshCallback, intervalMs = 30000, enabled = true) {
  const savedCallback = useRef(refreshCallback);
  const lastExecutionTimeRef = useRef(Date.now());
  const timerIdRef = useRef(null);

  // Keep latest callback reference
  useEffect(() => {
    savedCallback.current = refreshCallback;
  }, [refreshCallback]);

  const executeRefresh = useCallback((isBackground = true) => {
    lastExecutionTimeRef.current = Date.now();
    if (typeof savedCallback.current === "function") {
      try {
        savedCallback.current(isBackground);
      } catch (err) {
        console.error("AutoRefresh execution failed:", err);
      }
    }
  }, []);

  useEffect(() => {
    if (!enabled || intervalMs <= 0) {
      if (timerIdRef.current) {
        clearInterval(timerIdRef.current);
        timerIdRef.current = null;
      }
      return;
    }

    const startTimer = () => {
      if (timerIdRef.current) {
        clearInterval(timerIdRef.current);
      }
      timerIdRef.current = setInterval(() => {
        if (typeof document !== "undefined" && document.visibilityState === "visible") {
          executeRefresh(true);
        }
      }, intervalMs);
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible") {
        const timeElapsed = Date.now() - lastExecutionTimeRef.current;
        if (timeElapsed >= intervalMs) {
          executeRefresh(true);
        }
        startTimer();
      } else {
        if (timerIdRef.current) {
          clearInterval(timerIdRef.current);
          timerIdRef.current = null;
        }
      }
    };

    if (typeof document !== "undefined" && document.visibilityState === "visible") {
      startTimer();
    }

    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      if (timerIdRef.current) {
        clearInterval(timerIdRef.current);
        timerIdRef.current = null;
      }
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [intervalMs, enabled, executeRefresh]);

  return {
    triggerRefresh: () => executeRefresh(false),
  };
}

export default useAutoRefresh;

