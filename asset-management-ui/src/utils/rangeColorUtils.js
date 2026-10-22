/**
 * Status & Metric Color Palette and Range Mapping Utilities
 * Directly connects to CSS tokens defined in `src/global.css`:
 * --color-red, --color-orange, --color-yellow, --color-blue, --color-green
 */

export const THEME_COLORS = {
  red: "var(--color-red)",
  orange: "var(--color-orange)",
  yellow: "var(--color-yellow)",
  blue: "var(--color-blue)",
  green: "var(--color-green)",
};

/**
 * Resolves color string or token to stylesheet CSS variable
 * Supports: 'red', 'orange', 'yellow', 'blue', 'green', or existing var/hex.
 */
export function resolveColor(color) {
  if (!color) return THEME_COLORS.blue;
  const key = String(color).toLowerCase().trim();
  if (THEME_COLORS[key]) return THEME_COLORS[key];
  if (key === "critical" || key === "danger" || key === "expired") return THEME_COLORS.red;
  if (key === "high" || key === "expiring-7-days") return THEME_COLORS.orange;
  if (key === "medium" || key === "warning" || key === "expiring-30-days") return THEME_COLORS.yellow;
  if (key === "low" || key === "info") return THEME_COLORS.blue;
  if (key === "safe" || key === "healthy" || key === "active" || key === "online") return THEME_COLORS.green;
  return color;
}

/**
 * Range evaluator for metrics (e.g., CPU, RAM, Disk, Load, Errors).
 * Default convention (Load / Risk):
 * - Safe range (Low): 0 to 50% => GREEN
 * - Moderate / Warning: 51 to 75% => ORANGE / YELLOW
 * - High / Critical: 76% to 100% => RED
 *
 * @param {number} value - The numeric value to evaluate (0 - 100)
 * @param {Object} [options]
 * @param {number} [options.safeMax=50] - Threshold below which is safe (green)
 * @param {number} [options.warnMax=75] - Threshold below which is warning (orange)
 * @param {boolean} [options.useYellowForWarn=false] - Use yellow instead of orange for warning
 * @returns {{ color: string, name: string, status: string, label: string }}
 */
export function getSafeRangeColor(value, options = {}) {
  const { safeMax = 50, warnMax = 75, useYellowForWarn = false } = options;
  const numeric = Number(value) || 0;

  if (numeric <= safeMax) {
    return {
      color: THEME_COLORS.green,
      name: "green",
      status: "safe",
      label: "Safe Range",
      cssClass: "text-status-green",
      bgClass: "bg-status-green",
    };
  }

  if (numeric <= warnMax) {
    const warnColor = useYellowForWarn ? THEME_COLORS.yellow : THEME_COLORS.orange;
    const warnName = useYellowForWarn ? "yellow" : "orange";
    return {
      color: warnColor,
      name: warnName,
      status: "warning",
      label: "Warning Range",
      cssClass: `text-status-${warnName}`,
      bgClass: `bg-status-${warnName}`,
    };
  }

  return {
    color: THEME_COLORS.red,
    name: "red",
    status: "critical",
    label: "Critical / High Range",
    cssClass: "text-status-red",
    bgClass: "bg-status-red",
  };
}

/**
 * Range evaluator where HIGH is safe and LOW is critical (e.g., Battery, Uptime, Health Score).
 * - Safe range: >= 80% => GREEN
 * - Warning range: 50% - 79% => YELLOW / ORANGE
 * - Critical range: < 50% => RED
 *
 * @param {number} value
 * @param {Object} [options]
 * @param {number} [options.safeMin=80]
 * @param {number} [options.warnMin=50]
 * @returns {{ color: string, name: string, status: string, label: string }}
 */
export function getHealthRangeColor(value, options = {}) {
  const { safeMin = 80, warnMin = 50 } = options;
  const numeric = Number(value) || 0;

  if (numeric >= safeMin) {
    return {
      color: THEME_COLORS.green,
      name: "green",
      status: "safe",
      label: "Healthy",
      cssClass: "text-status-green",
      bgClass: "bg-status-green",
    };
  }

  if (numeric >= warnMin) {
    return {
      color: THEME_COLORS.orange,
      name: "orange",
      status: "warning",
      label: "Warning",
      cssClass: "text-status-orange",
      bgClass: "bg-status-orange",
    };
  }

  return {
    color: THEME_COLORS.red,
    name: "red",
    status: "critical",
    label: "Critical",
    cssClass: "text-status-red",
    bgClass: "bg-status-red",
  };
}

/**
 * Generalized custom range evaluator.
 * Evaluates against an array of custom range definitions:
 * [
 *   { min: 0, max: 30, color: 'green', label: 'Optimal' },
 *   { min: 31, max: 70, color: 'yellow', label: 'Average' },
 *   { min: 71, max: 100, color: 'red', label: 'Severe' }
 * ]
 */
export function getColorByRange(value, customRanges = []) {
  const numeric = Number(value) || 0;
  for (const range of customRanges) {
    const min = range.min ?? -Infinity;
    const max = range.max ?? Infinity;
    if (numeric >= min && numeric <= max) {
      return {
        color: resolveColor(range.color),
        name: range.color,
        label: range.label || `${min}-${max}`,
        status: range.status || "custom",
      };
    }
  }
  // Default to safe range function if no range match
  return getSafeRangeColor(value);
}
