import { createContext, useState } from "react";

export const AuthContext = createContext(null);

const ADMIN = {
    username: "admin",
    password: "admin123",
    role: "ADMIN",
}

const AUTH_STORAGE_KEY = "asset_management_auth";

export function AuthProvider({ children }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [user, setUser] = useState(() => {
    const storedUser = localStorage.getItem(AUTH_STORAGE_KEY);
    return storedUser ? JSON.parse(storedUser) : null;
  });

  const login = (username, password) => {
    setLoading(true);
    setError("");
    try {
      if (username === ADMIN.username && password === ADMIN.password) {
        const authenticatedUser = {
          username: ADMIN.username,
          role: ADMIN.role,
        };
        localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authenticatedUser));
        setUser(authenticatedUser);
        return {
          success: true,
          user: authenticatedUser,
        };
      }
      setError("Invalid username or password");
      return {
        success: false,
        user: null,
      };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    setUser(null);
    setError("");
  };

  const value = {
    user,
    isAuthenticated: !!user,
    loading,
    error,
    login,
    logout,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}
