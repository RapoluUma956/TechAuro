import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export default function ProtectedRoute() {
  const { isAuthenticated, passwordChangeRequired, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex h-screen w-screen items-center justify-center bg-[#F5F8F7]">
        <div className="flex flex-col items-center gap-3">
          <div className="h-8 w-8 animate-spin rounded-full border-3 border-[#1976D2] border-t-transparent" />
          <p className="text-xs font-semibold text-[#64748B]">Verifying session...</p>
        </div>
      </div>
    );
  }

  // If password change is required on first login, block access to all protected app modules
  if (passwordChangeRequired) {
    return <Navigate to="/reset-password" replace />;
  }

  // If not authenticated, redirect to login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}