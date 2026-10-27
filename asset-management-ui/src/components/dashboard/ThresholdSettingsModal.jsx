import { useState, useEffect } from "react";
import { dashboardService } from "../../services/dashboardService";

export default function ThresholdSettingsModal({ isOpen, onClose, onThresholdsUpdated }) {
  const [formData, setFormData] = useState({
    cpuWarningPercent: 90,
    ramWarningPercent: 90,
    diskCriticalPercent: 90,
    offlineTimeoutMinutes: 5,
    warrantyWarningDays: 30,
    licenseWarningDays: 30,
  });

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [updatedAt, setUpdatedAt] = useState(null);

  // Fetch current thresholds when modal is opened
  useEffect(() => {
    if (!isOpen) {
      setSuccessMsg(null);
      setError(null);
      return;
    }

    let isMounted = true;
    async function loadThresholds() {
      setLoading(true);
      setError(null);
      try {
        const res = await dashboardService.getThresholds();
        if (isMounted && res) {
          setFormData({
            cpuWarningPercent: res.cpuWarningPercent ?? 90,
            ramWarningPercent: res.ramWarningPercent ?? 90,
            diskCriticalPercent: res.diskCriticalPercent ?? 90,
            offlineTimeoutMinutes: res.offlineTimeoutMinutes ?? 5,
            warrantyWarningDays: res.warrantyWarningDays ?? 30,
            licenseWarningDays: res.licenseWarningDays ?? 30,
          });
          setUpdatedAt(res.updatedAt);
        }
      } catch (err) {
        if (isMounted) {
          setError(err.message || "Failed to load threshold settings");
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadThresholds();

    return () => {
      isMounted = false;
    };
  }, [isOpen]);

  // Handle escape key
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape" && isOpen && !saving) {
        onClose();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, saving, onClose]);

  const handleChange = (field, value) => {
    const num = parseInt(value, 10);
    setFormData((prev) => ({
      ...prev,
      [field]: isNaN(num) ? "" : num,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccessMsg(null);

    // Basic validation
    const payload = {
      cpuWarningPercent: Number(formData.cpuWarningPercent),
      ramWarningPercent: Number(formData.ramWarningPercent),
      diskCriticalPercent: Number(formData.diskCriticalPercent),
      offlineTimeoutMinutes: Number(formData.offlineTimeoutMinutes),
      warrantyWarningDays: Number(formData.warrantyWarningDays),
      licenseWarningDays: Number(formData.licenseWarningDays),
    };

    if (
      payload.cpuWarningPercent < 1 || payload.cpuWarningPercent > 100 ||
      payload.ramWarningPercent < 1 || payload.ramWarningPercent > 100 ||
      payload.diskCriticalPercent < 1 || payload.diskCriticalPercent > 100 ||
      payload.offlineTimeoutMinutes < 1 ||
      payload.warrantyWarningDays < 1 ||
      payload.licenseWarningDays < 1
    ) {
      setError("Please ensure percentages are between 1 and 100, and durations are at least 1.");
      setSaving(false);
      return;
    }

    try {
      const updated = await dashboardService.updateThresholds(payload);
      setSuccessMsg("Health thresholds successfully updated!");
      setUpdatedAt(updated?.updatedAt || new Date().toISOString());
      if (onThresholdsUpdated) {
        onThresholdsUpdated(updated);
      }
      setTimeout(() => {
        onClose();
      }, 1200);
    } catch (err) {
      setError(err.message || "Failed to update thresholds. Please try again.");
    } finally {
      setSaving(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-fadeIn">
      <div
        className="w-full max-w-xl overflow-hidden rounded-2xl border border-[#DCE5E1] bg-white shadow-2xl transition-all"
        role="dialog"
        aria-labelledby="threshold-modal-title"
        aria-modal="true"
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[#F1F5F9] px-6 py-4 bg-[#F8FAFC]">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#EBF5FF] text-[#1976D2]">
              <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="4" y1="21" x2="4" y2="14" />
                <line x1="4" y1="10" x2="4" y2="3" />
                <line x1="12" y1="21" x2="12" y2="12" />
                <line x1="12" y1="8" x2="12" y2="3" />
                <line x1="20" y1="21" x2="20" y2="16" />
                <line x1="20" y1="12" x2="20" y2="3" />
                <line x1="1" y1="14" x2="7" y2="14" />
                <line x1="9" y1="8" x2="15" y2="8" />
                <line x1="17" y1="16" x2="23" y2="16" />
              </svg>
            </div>
            <div>
              <h2 id="threshold-modal-title" className="text-base font-bold text-[#03312E]">
                Configurable Health Thresholds
              </h2>
              <p className="text-xs text-[#64748B]">
                Configure rules used by the backend to calculate device health and alerts
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            disabled={saving}
            className="rounded-lg p-1 text-[#94A3B8] transition-colors hover:bg-[#E2E8F0] hover:text-[#03312E]"
            aria-label="Close modal"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6">
          {error && (
            <div className="mb-4 rounded-xl border border-[#FEE2E2] bg-[#FEF2F2] p-3 text-xs font-medium text-[#DC2626]">
              {error}
            </div>
          )}

          {successMsg && (
            <div className="mb-4 rounded-xl border border-[#D1FAE5] bg-[#ECFDF5] p-3 text-xs font-semibold text-[#059669]">
              ✓ {successMsg}
            </div>
          )}

          {loading ? (
            <div className="flex flex-col items-center justify-center py-12 text-[#64748B]">
              <div className="h-8 w-8 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent" />
              <p className="mt-3 text-xs font-medium">Loading current threshold configuration...</p>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {/* CPU Warning */}
                <div>
                  <label className="block text-xs font-bold text-[#03312E]">
                    CPU Warning Threshold (%)
                  </label>
                  <p className="mb-1 text-[11px] text-[#64748B]">
                    Flag device as warning if CPU exceeds
                  </p>
                  <div className="relative">
                    <input
                      type="number"
                      min="1"
                      max="100"
                      required
                      value={formData.cpuWarningPercent}
                      onChange={(e) => handleChange("cpuWarningPercent", e.target.value)}
                      className="w-full rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-2 text-xs font-semibold text-[#03312E] focus:border-[#1976D2] focus:bg-white focus:outline-none"
                    />
                    <span className="absolute right-3 top-2 text-xs text-[#94A3B8]">%</span>
                  </div>
                </div>

                {/* RAM Warning */}
                <div>
                  <label className="block text-xs font-bold text-[#03312E]">
                    RAM Warning Threshold (%)
                  </label>
                  <p className="mb-1 text-[11px] text-[#64748B]">
                    Flag device as warning if RAM exceeds
                  </p>
                  <div className="relative">
                    <input
                      type="number"
                      min="1"
                      max="100"
                      required
                      value={formData.ramWarningPercent}
                      onChange={(e) => handleChange("ramWarningPercent", e.target.value)}
                      className="w-full rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-2 text-xs font-semibold text-[#03312E] focus:border-[#1976D2] focus:bg-white focus:outline-none"
                    />
                    <span className="absolute right-3 top-2 text-xs text-[#94A3B8]">%</span>
                  </div>
                </div>

                {/* Disk Critical */}
                <div>
                  <label className="block text-xs font-bold text-[#03312E]">
                    Disk Critical Threshold (%)
                  </label>
                  <p className="mb-1 text-[11px] text-[#64748B]">
                    Flag device as critical if Disk exceeds
                  </p>
                  <div className="relative">
                    <input
                      type="number"
                      min="1"
                      max="100"
                      required
                      value={formData.diskCriticalPercent}
                      onChange={(e) => handleChange("diskCriticalPercent", e.target.value)}
                      className="w-full rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-2 text-xs font-semibold text-[#03312E] focus:border-[#1976D2] focus:bg-white focus:outline-none"
                    />
                    <span className="absolute right-3 top-2 text-xs text-[#94A3B8]">%</span>
                  </div>
                </div>

                {/* Offline Timeout */}
                <div>
                  <label className="block text-xs font-bold text-[#03312E]">
                    Offline Timeout (Minutes)
                  </label>
                  <p className="mb-1 text-[11px] text-[#64748B]">
                    Flag device as offline if not seen for
                  </p>
                  <div className="relative">
                    <input
                      type="number"
                      min="1"
                      required
                      value={formData.offlineTimeoutMinutes}
                      onChange={(e) => handleChange("offlineTimeoutMinutes", e.target.value)}
                      className="w-full rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-2 text-xs font-semibold text-[#03312E] focus:border-[#1976D2] focus:bg-white focus:outline-none"
                    />
                    <span className="absolute right-3 top-2 text-xs text-[#94A3B8]">min</span>
                  </div>
                </div>

                {/* Warranty Warning */}
                <div>
                  <label className="block text-xs font-bold text-[#03312E]">
                    Warranty Warning Window (Days)
                  </label>
                  <p className="mb-1 text-[11px] text-[#64748B]">
                    Alert when device warranty expires within
                  </p>
                  <div className="relative">
                    <input
                      type="number"
                      min="1"
                      required
                      value={formData.warrantyWarningDays}
                      onChange={(e) => handleChange("warrantyWarningDays", e.target.value)}
                      className="w-full rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-2 text-xs font-semibold text-[#03312E] focus:border-[#1976D2] focus:bg-white focus:outline-none"
                    />
                    <span className="absolute right-3 top-2 text-xs text-[#94A3B8]">days</span>
                  </div>
                </div>

                {/* License Warning */}
                <div>
                  <label className="block text-xs font-bold text-[#03312E]">
                    License Warning Window (Days)
                  </label>
                  <p className="mb-1 text-[11px] text-[#64748B]">
                    Alert when software license expires within
                  </p>
                  <div className="relative">
                    <input
                      type="number"
                      min="1"
                      required
                      value={formData.licenseWarningDays}
                      onChange={(e) => handleChange("licenseWarningDays", e.target.value)}
                      className="w-full rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-2 text-xs font-semibold text-[#03312E] focus:border-[#1976D2] focus:bg-white focus:outline-none"
                    />
                    <span className="absolute right-3 top-2 text-xs text-[#94A3B8]">days</span>
                  </div>
                </div>
              </div>

              {updatedAt && (
                <p className="pt-2 text-right text-[11px] text-[#94A3B8]">
                  Last configured: {new Date(updatedAt).toLocaleString()}
                </p>
              )}
            </div>
          )}

          {/* Footer Controls */}
          <div className="mt-6 flex items-center justify-end gap-3 border-t border-[#F1F5F9] pt-4">
            <button
              type="button"
              disabled={saving}
              onClick={onClose}
              className="rounded-lg border border-[#DCE5E1] bg-white px-4 py-2 text-xs font-semibold text-[#64748B] hover:bg-[#F8FAFC]"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || saving}
              className="inline-flex items-center gap-2 rounded-lg bg-[#1976D2] px-5 py-2 text-xs font-bold text-white shadow-sm hover:bg-[#1565C0] disabled:opacity-50"
            >
              {saving ? (
                <>
                  <div className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent" />
                  <span>Saving...</span>
                </>
              ) : (
                "Save Configuration"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
