import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Card, Typography } from "eworldes-ui-toolkit-v1.1.0";
import { useAuth } from "../../hooks/useAuth";

export default function ResetPassword() {
  const { changePassword, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const initialPasswordFromLogin = location.state?.initialPassword || "";
  const userEmail = location.state?.email || "";

  const [formData, setFormData] = useState({
    currentPassword: initialPasswordFromLogin,
    newPassword: "",
    confirmPassword: "",
  });

  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [localError, setLocalError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  // Password rules validation
  const rules = {
    length: formData.newPassword.length >= 12,
    uppercase: /[A-Z]/.test(formData.newPassword),
    lowercase: /[a-z]/.test(formData.newPassword),
    number: /[0-9]/.test(formData.newPassword),
    special: /[@$!%*?&#^_-]/.test(formData.newPassword),
    match: formData.newPassword.length > 0 && formData.newPassword === formData.confirmPassword,
  };

  const isFormValid =
    formData.currentPassword &&
    rules.length &&
    rules.uppercase &&
    rules.lowercase &&
    rules.number &&
    rules.special &&
    rules.match;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
    if (localError) {
      setLocalError("");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLocalError("");
    setFieldErrors({});

    const errors = {};
    if (!formData.currentPassword) {
      errors.currentPassword = "Current password is required.";
    }
    if (!formData.newPassword) {
      errors.newPassword = "New password is required.";
    }
    if (!formData.confirmPassword) {
      errors.confirmPassword = "Confirm password is required.";
    } else if (formData.newPassword !== formData.confirmPassword) {
      errors.confirmPassword = "New password and confirm password do not match";
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      setLocalError("Please correct the highlighted fields");
      return;
    }

    setSubmitting(true);
    try {
      const result = await changePassword({
        currentPassword: formData.currentPassword,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword,
      });

      if (result.success) {
        setSuccessMessage(result.message || "Password changed successfully. Please log in again.");
        setTimeout(() => {
          navigate("/login", { replace: true });
        }, 2500);
      } else {
        setLocalError(result.message || "Unable to change your password right now.");
        if (result.errors) {
          setFieldErrors(result.errors);
        }
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="login-container">
      {/* Left Panel: Video */}
      <div className="left-panel">
        <video autoPlay muted loop playsInline className="login-video">
          <source src="/loginPage-gif.mp4" type="video/mp4" />
          Your browser does not support the video tag.
        </video>
      </div>

      {/* Right Panel: Reset Password Form */}
      <div className="right-panel">
        <Card className="login-card max-w-[480px]">
          <div className="login-logo flex gap-0.5 items-center justify-center mb-3 p-1">
            <img src="/title.png" alt="TechAura Logo" className="login-logo-img h-16 object-contain" />
          </div>

          <div className="flex flex-col gap-3 justify-center items-center w-full">
            <div className="text-center">
              <Typography
                variant="h4"
                className="login-heading !text-base !font-bold !font-body !text-[#03312E]"
              >
                Set Your New Password
              </Typography>
              <p className="text-xs text-[#64748B] mt-1">
                {userEmail ? (
                  <span>
                    Account: <strong className="text-[#03312E]">{userEmail}</strong>.{" "}
                  </span>
                ) : null}
                For security, you must update your temporary password before accessing the system.
              </p>
            </div>

            {/* Success Banner */}
            {successMessage && (
              <div
                className="w-full flex items-center gap-2.5 rounded-xl border border-emerald-200 bg-emerald-50 p-3.5 text-xs text-emerald-800 animate-in fade-in duration-200"
                role="alert"
              >
                <svg
                  className="h-5 w-5 shrink-0 text-emerald-600"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                  <polyline points="22 4 12 14.01 9 11.01" />
                </svg>
                <div>
                  <p className="font-bold">{successMessage}</p>
                  <p className="text-[11px] text-emerald-700 mt-0.5">Redirecting to login page...</p>
                </div>
              </div>
            )}

            {/* Error Banner */}
            {localError && !successMessage && (
              <div
                className="w-full flex items-start gap-2.5 rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700 animate-in fade-in duration-200"
                role="alert"
              >
                <svg
                  className="h-4 w-4 shrink-0 text-red-500 mt-0.5"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                <span className="font-medium leading-relaxed">{localError}</span>
              </div>
            )}

            {!successMessage && (
              <form className="flex flex-col gap-3.5 w-full mt-1" onSubmit={handleSubmit} noValidate>
                {/* Current Password Field */}
                <div className="flex flex-col gap-1 w-full text-left">
                  <label className="text-xs font-semibold text-[#334155]" htmlFor="currentPassword">
                    Current (Temporary) Password
                  </label>
                  <div className="relative">
                    <input
                      id="currentPassword"
                      name="currentPassword"
                      type={showCurrentPassword ? "text" : "password"}
                      autoComplete="current-password"
                      placeholder="Enter current password"
                      value={formData.currentPassword}
                      onChange={handleChange}
                      disabled={submitting}
                      className={`w-full rounded-xl border px-3 py-2 text-xs text-[#0F172A] placeholder-[#94A3B8] transition-all focus:outline-none focus:ring-2 ${
                        fieldErrors.currentPassword
                          ? "border-red-400 bg-red-50/20 focus:border-red-500 focus:ring-red-200"
                          : "border-[#DCE5E1] bg-white focus:border-[#1976D2] focus:ring-[#1976D2]/20"
                      }`}
                    />
                    <button
                      type="button"
                      onClick={() => setShowCurrentPassword((prev) => !prev)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#334155] focus:outline-none"
                    >
                      {showCurrentPassword ? (
                        <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                          <line x1="1" y1="1" x2="23" y2="23" />
                        </svg>
                      ) : (
                        <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                          <circle cx="12" cy="12" r="3" />
                        </svg>
                      )}
                    </button>
                  </div>
                  {fieldErrors.currentPassword && (
                    <span className="text-[11px] font-medium text-red-600 mt-0.5">
                      {fieldErrors.currentPassword}
                    </span>
                  )}
                </div>

                {/* New Password Field */}
                <div className="flex flex-col gap-1 w-full text-left">
                  <label className="text-xs font-semibold text-[#334155]" htmlFor="newPassword">
                    New Password
                  </label>
                  <div className="relative">
                    <input
                      id="newPassword"
                      name="newPassword"
                      type={showNewPassword ? "text" : "password"}
                      autoComplete="new-password"
                      placeholder="Enter new strong password"
                      value={formData.newPassword}
                      onChange={handleChange}
                      disabled={submitting}
                      className={`w-full rounded-xl border px-3 py-2 text-xs text-[#0F172A] placeholder-[#94A3B8] transition-all focus:outline-none focus:ring-2 ${
                        fieldErrors.newPassword
                          ? "border-red-400 bg-red-50/20 focus:border-red-500 focus:ring-red-200"
                          : "border-[#DCE5E1] bg-white focus:border-[#1976D2] focus:ring-[#1976D2]/20"
                      }`}
                    />
                    <button
                      type="button"
                      onClick={() => setShowNewPassword((prev) => !prev)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#334155] focus:outline-none"
                    >
                      {showNewPassword ? (
                        <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                          <line x1="1" y1="1" x2="23" y2="23" />
                        </svg>
                      ) : (
                        <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                          <circle cx="12" cy="12" r="3" />
                        </svg>
                      )}
                    </button>
                  </div>
                  {fieldErrors.newPassword && (
                    <span className="text-[11px] font-medium text-red-600 mt-0.5">
                      {fieldErrors.newPassword}
                    </span>
                  )}
                </div>

                {/* Confirm Password Field */}
                <div className="flex flex-col gap-1 w-full text-left">
                  <label className="text-xs font-semibold text-[#334155]" htmlFor="confirmPassword">
                    Confirm New Password
                  </label>
                  <div className="relative">
                    <input
                      id="confirmPassword"
                      name="confirmPassword"
                      type={showConfirmPassword ? "text" : "password"}
                      autoComplete="new-password"
                      placeholder="Confirm your new password"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      disabled={submitting}
                      className={`w-full rounded-xl border px-3 py-2 text-xs text-[#0F172A] placeholder-[#94A3B8] transition-all focus:outline-none focus:ring-2 ${
                        fieldErrors.confirmPassword
                          ? "border-red-400 bg-red-50/20 focus:border-red-500 focus:ring-red-200"
                          : "border-[#DCE5E1] bg-white focus:border-[#1976D2] focus:ring-[#1976D2]/20"
                      }`}
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword((prev) => !prev)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#334155] focus:outline-none"
                    >
                      {showConfirmPassword ? (
                        <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                          <line x1="1" y1="1" x2="23" y2="23" />
                        </svg>
                      ) : (
                        <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                          <circle cx="12" cy="12" r="3" />
                        </svg>
                      )}
                    </button>
                  </div>
                  {fieldErrors.confirmPassword && (
                    <span className="text-[11px] font-medium text-red-600 mt-0.5">
                      {fieldErrors.confirmPassword}
                    </span>
                  )}
                </div>

                {/* Password Policy Requirements Checklist */}
                <div className="w-full rounded-xl border border-[#E2E8F0] bg-[#F8FAFC] p-3 text-left">
                  <p className="text-[11px] font-bold text-[#334155] mb-2">Password Requirements:</p>
                  <ul className="grid grid-cols-2 gap-1.5 text-[10px]">
                    <li className={`flex items-center gap-1.5 ${rules.length ? "text-emerald-700 font-semibold" : "text-[#64748B]"}`}>
                      <span className={`flex h-3.5 w-3.5 items-center justify-center rounded-full text-[9px] ${rules.length ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-500"}`}>
                        ✓
                      </span>
                      12+ Characters
                    </li>
                    <li className={`flex items-center gap-1.5 ${rules.uppercase ? "text-emerald-700 font-semibold" : "text-[#64748B]"}`}>
                      <span className={`flex h-3.5 w-3.5 items-center justify-center rounded-full text-[9px] ${rules.uppercase ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-500"}`}>
                        ✓
                      </span>
                      Uppercase (A-Z)
                    </li>
                    <li className={`flex items-center gap-1.5 ${rules.lowercase ? "text-emerald-700 font-semibold" : "text-[#64748B]"}`}>
                      <span className={`flex h-3.5 w-3.5 items-center justify-center rounded-full text-[9px] ${rules.lowercase ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-500"}`}>
                        ✓
                      </span>
                      Lowercase (a-z)
                    </li>
                    <li className={`flex items-center gap-1.5 ${rules.number ? "text-emerald-700 font-semibold" : "text-[#64748B]"}`}>
                      <span className={`flex h-3.5 w-3.5 items-center justify-center rounded-full text-[9px] ${rules.number ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-500"}`}>
                        ✓
                      </span>
                      Number (0-9)
                    </li>
                    <li className={`flex items-center gap-1.5 ${rules.special ? "text-emerald-700 font-semibold" : "text-[#64748B]"}`}>
                      <span className={`flex h-3.5 w-3.5 items-center justify-center rounded-full text-[9px] ${rules.special ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-500"}`}>
                        ✓
                      </span>
                      Special Symbol
                    </li>
                    <li className={`flex items-center gap-1.5 ${rules.match ? "text-emerald-700 font-semibold" : "text-[#64748B]"}`}>
                      <span className={`flex h-3.5 w-3.5 items-center justify-center rounded-full text-[9px] ${rules.match ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-500"}`}>
                        ✓
                      </span>
                      Passwords Match
                    </li>
                  </ul>
                </div>

                {/* Action Buttons */}
                <div className="flex items-center gap-2 mt-1">
                  <button
                    type="button"
                    onClick={handleCancel}
                    disabled={submitting}
                    className="w-1/3 rounded-xl border border-[#CBD5E1] py-2.5 text-xs font-semibold text-[#475569] hover:bg-[#F1F5F9] transition-all cursor-pointer"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting || !isFormValid}
                    className="w-2/3 rounded-xl bg-[#1976D2] py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-[#1565C0] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer flex items-center justify-center gap-2"
                  >
                    {submitting ? "Updating..." : "Update & Proceed"}
                  </button>
                </div>
              </form>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
