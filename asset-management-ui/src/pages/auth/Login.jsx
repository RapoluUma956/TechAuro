import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, Typography } from "eworldes-ui-toolkit-v1.1.0";
import { useAuth } from "../../hooks/useAuth";

export default function Login() {
  const { login, error: globalError, fieldErrors: contextFieldErrors } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [localError, setLocalError] = useState("");
  const [localFieldErrors, setLocalFieldErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear local error on input change
    if (localFieldErrors[name]) {
      setLocalFieldErrors((prev) => ({
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
    setLocalFieldErrors({});

    // Client-side quick check
    const errors = {};
    if (!formData.email.trim()) {
      errors.email = "Please enter a valid email address";
    }
    if (!formData.password) {
      errors.password = "Password is required";
    }

    if (Object.keys(errors).length > 0) {
      setLocalFieldErrors(errors);
      setLocalError("Please correct the highlighted fields");
      return;
    }

    setSubmitting(true);
    try {
      const result = await login(formData.email.trim(), formData.password);

      if (result.success) {
        if (result.passwordChangeRequired) {
          // Mandatory first login reset flow
          navigate("/reset-password", {
            state: {
              email: formData.email.trim(),
              initialPassword: formData.password,
              message: "Please set a new secure password for your account to continue.",
            },
            replace: true,
          });
        } else {
          navigate("/dashboard", { replace: true });
        }
      } else {
        setLocalError(result.message || "Invalid email or password");
        if (result.errors) {
          setLocalFieldErrors(result.errors);
        }
      }
    } finally {
      setSubmitting(false);
    }
  };

  const activeError = localError || globalError;
  const activeFieldErrors = { ...contextFieldErrors, ...localFieldErrors };

  return (
    <div className="login-container">
      {/* Left Panel: Background Branded Video */}
      <div className="left-panel">
        <video autoPlay muted loop playsInline className="login-video">
          <source src="/loginPage-gif.mp4" type="video/mp4" />
          Your browser does not support the video tag.
        </video>
      </div>

      {/* Right Panel: Clean Authentication Form */}
      <div className="right-panel">
        <Card className="login-card">
          <div className="login-logo flex gap-0.5 items-center justify-center mb-4 p-2">
            <img src="/title.png" alt="TechAura Logo" className="login-logo-img h-20 object-contain" />
          </div>

          <div className="login-form flex flex-col gap-4 justify-center items-center w-full">
            <Typography
              variant="h4"
              className="login-heading !text-sm !font-bold !font-body !text-[#03312E]"
            >
              Welcome Back! Please Sign In to Your Account
            </Typography>

            {/* Form-level Error Alert */}
            {activeError && (
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
                <span className="font-medium leading-relaxed">{activeError}</span>
              </div>
            )}

            <form className="flex flex-col gap-4 w-full" onSubmit={handleSubmit} noValidate>
              {/* Email Field */}
              <div className="flex flex-col gap-1 w-full text-left">
                <label className="text-xs font-semibold text-[#334155]" htmlFor="email-input">
                  Email Address
                </label>
                <div className="relative">
                  <input
                    id="email-input"
                    name="email"
                    type="email"
                    autoComplete="email"
                    placeholder="admin@example.com"
                    value={formData.email}
                    onChange={handleChange}
                    disabled={submitting}
                    className={`w-full rounded-xl border px-3.5 py-2.5 text-xs text-[#0F172A] placeholder-[#94A3B8] transition-all focus:outline-none focus:ring-2 ${
                      activeFieldErrors.email
                        ? "border-red-400 bg-red-50/20 focus:border-red-500 focus:ring-red-200"
                        : "border-[#DCE5E1] bg-white focus:border-[#1976D2] focus:ring-[#1976D2]/20"
                    }`}
                  />
                </div>
                {activeFieldErrors.email && (
                  <span className="text-[11px] font-medium text-red-600 mt-0.5">
                    {activeFieldErrors.email}
                  </span>
                )}
              </div>

              {/* Password Field */}
              <div className="flex flex-col gap-1 w-full text-left">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-semibold text-[#334155]" htmlFor="password-input">
                    Password
                  </label>
                </div>
                <div className="relative">
                  <input
                    id="password-input"
                    name="password"
                    type={showPassword ? "text" : "password"}
                    autoComplete="current-password"
                    placeholder="Enter your password"
                    value={formData.password}
                    onChange={handleChange}
                    disabled={submitting}
                    className={`w-full rounded-xl border px-3.5 py-2.5 pr-10 text-xs text-[#0F172A] placeholder-[#94A3B8] transition-all focus:outline-none focus:ring-2 ${
                      activeFieldErrors.password
                        ? "border-red-400 bg-red-50/20 focus:border-red-500 focus:ring-red-200"
                        : "border-[#DCE5E1] bg-white focus:border-[#1976D2] focus:ring-[#1976D2]/20"
                    }`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((prev) => !prev)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#334155] transition-colors focus:outline-none"
                    aria-label={showPassword ? "Hide password" : "Show password"}
                  >
                    {showPassword ? (
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
                {activeFieldErrors.password && (
                  <span className="text-[11px] font-medium text-red-600 mt-0.5">
                    {activeFieldErrors.password}
                  </span>
                )}
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={submitting}
                className="mt-2 w-full rounded-xl bg-[#1976D2] py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-[#1565C0] active:scale-[0.99] disabled:opacity-60 disabled:cursor-not-allowed cursor-pointer flex items-center justify-center gap-2"
              >
                {submitting ? (
                  <>
                    <svg className="h-4 w-4 animate-spin text-white" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                    </svg>
                    <span>Signing in...</span>
                  </>
                ) : (
                  <span>Sign In</span>
                )}
              </button>
            </form>
          </div>
        </Card>
      </div>
    </div>
  );
}
