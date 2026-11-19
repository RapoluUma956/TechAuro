import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { usePermission } from "../hooks/usePermission";
import Unauthorized from "../pages/error/Unauthorized.jsx";

export default function ProtectedRoute({ moduleCode, moduleName }) {
  const { isAuthenticated, passwordChangeRequired, loading } = useAuth();
  const { canRead } = usePermission();

  if (loading) {
    return (
      <div className="flex h-screen w-screen bg-[#F5F8F7] animate-pulse">
        {/* Sidebar skeleton */}
        <div className="hidden md:flex w-64 shrink-0 flex-col border-r border-[#DCE5E1] bg-white p-6 space-y-6">
          <div className="h-10 w-36 rounded-xl bg-slate-100" />
          <div className="space-y-3 pt-6">
            <div className="h-10 w-full rounded-xl bg-slate-100" />
            <div className="h-10 w-full rounded-xl bg-slate-100" />
            <div className="h-10 w-full rounded-xl bg-slate-100" />
            <div className="h-10 w-full rounded-xl bg-slate-100" />
            <div className="h-10 w-full rounded-xl bg-slate-100" />
          </div>
        </div>
        {/* Main content skeleton */}
        <div className="flex-1 flex flex-col min-w-0">
          <div className="h-16 border-b border-[#DCE5E1] bg-white px-8 flex items-center justify-between">
            <div className="h-8 w-64 rounded-xl bg-slate-100" />
            <div className="h-10 w-32 rounded-xl bg-slate-100" />
          </div>
          <div className="p-8 space-y-6">
            <div className="h-8 w-48 rounded bg-slate-100" />
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="h-24 rounded-2xl bg-white border border-[#DCE5E1]" />
              <div className="h-24 rounded-2xl bg-white border border-[#DCE5E1]" />
              <div className="h-24 rounded-2xl bg-white border border-[#DCE5E1]" />
              <div className="h-24 rounded-2xl bg-white border border-[#DCE5E1]" />
            </div>
            <div className="h-96 rounded-2xl bg-white border border-[#DCE5E1]" />
          </div>
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

  // Check RBAC permission for route if module is specified
  if (moduleCode && !canRead(moduleCode)) {
    return <Unauthorized moduleName={moduleName || moduleCode} />;
  }

  return <Outlet />;
}
