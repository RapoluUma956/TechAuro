import { createContext, useState, useEffect, useCallback } from "react";
import { authService } from "../services/authService";
import { setOnAuthFailure, setAccessToken } from "../services/api";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [pendingUser, setPendingUser] = useState(null);
  const [passwordChangeRequired, setPasswordChangeRequired] = useState(false);
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  // Central handler when automatic token refresh fails
  const handleAuthFailure = useCallback(() => {
    setUser(null);
    setPendingUser(null);
    setPasswordChangeRequired(false);
    setAccessToken(null);
  }, []);

  useEffect(() => {
    setOnAuthFailure(handleAuthFailure);
  }, [handleAuthFailure]);

  // Initialize CSRF and attempt session rehydration via HttpOnly refresh cookie on page reload
  useEffect(() => {
    let isMounted = true;

    async function initializeSession() {
      try {
        await authService.getCsrf();
        const refreshRes = await authService.refresh();
        if (refreshRes?.data?.accessToken) {
          const profile = await authService.getMe();
          if (isMounted && profile) {
            setUser(profile);
            setPasswordChangeRequired(false);
          }
        }
      } catch (err) {
        // No active refresh session or expired; user remains logged out
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    initializeSession();

    return () => {
      isMounted = false;
    };
  }, []);

  const login = async (email, password) => {
    setAuthError("");
    setFieldErrors({});
    try {
      // Ensure CSRF token is present before login
      await authService.getCsrf();

      const res = await authService.login({ email, password });
      const data = res?.data;

      if (data?.passwordChangeRequired) {
        setPendingUser(data.user);
        setPasswordChangeRequired(true);
        setUser(null);
        return {
          success: true,
          passwordChangeRequired: true,
          user: data.user,
        };
      }

      setUser(data.user);
      setPendingUser(null);
      setPasswordChangeRequired(false);

      return {
        success: true,
        passwordChangeRequired: false,
        user: data.user,
      };
    } catch (err) {
      const fieldErrorMap = {};
      if (Array.isArray(err.errors)) {
        err.errors.forEach((e) => {
          if (e.field) {
            fieldErrorMap[e.field] = e.message;
          }
        });
      }

      setAuthError(err.message || "Unable to log in right now. Please try again later.");
      setFieldErrors(fieldErrorMap);

      return {
        success: false,
        status: err.status,
        message: err.message,
        errors: fieldErrorMap,
      };
    }
  };

  const changePassword = async ({ currentPassword, newPassword, confirmPassword }) => {
    setAuthError("");
    setFieldErrors({});
    try {
      const res = await authService.changePassword({
        currentPassword,
        newPassword,
        confirmPassword,
      });

      // Clear restricted session state; user must now log in with their new credentials
      setPasswordChangeRequired(false);
      setPendingUser(null);
      setUser(null);
      setAccessToken(null);

      // Re-initialize CSRF token for the next login
      await authService.getCsrf();

      return {
        success: true,
        message: res?.message || "Password changed successfully. Please log in again.",
      };
    } catch (err) {
      const fieldErrorMap = {};
      if (Array.isArray(err.errors)) {
        err.errors.forEach((e) => {
          if (e.field) {
            fieldErrorMap[e.field] = e.message;
          }
        });
      }

      setAuthError(err.message || "Unable to change your password right now.");
      setFieldErrors(fieldErrorMap);

      return {
        success: false,
        status: err.status,
        message: err.message,
        errors: fieldErrorMap,
      };
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch (err) {
      // Idempotent logout
    } finally {
      setUser(null);
      setPendingUser(null);
      setPasswordChangeRequired(false);
      setAuthError("");
      setFieldErrors({});
      authService.getCsrf().catch(() => {});
    }
  };

  const value = {
    user,
    pendingUser,
    isAuthenticated: !!user && !passwordChangeRequired,
    passwordChangeRequired,
    loading,
    error: authError,
    fieldErrors,
    login,
    changePassword,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
