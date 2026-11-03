import { BrowserRouter, Navigate, Routes, Route } from "react-router-dom";

import Login from "../pages/auth/Login.jsx";
import ResetPassword from "../pages/auth/ResetPassword.jsx";
import Dashboard from "../pages/dashboard/Dashboard.jsx";
import Employees from "../pages/employees/Employees.jsx";
import EmployeeDetail from "../pages/employees/EmployeeDetail.jsx";
import ProtectedRoute from "./ProtectedRoute.jsx";
import AppLayout from "../components/layout/AppLayout.jsx";
import Accessories from "../pages/accessories/Accessories.jsx";
import Licenses from "../pages/licenses/Licenses.jsx";
import Notifications from "../pages/notifications/Notifications.jsx";
import RolePermissions from "../pages/roles/RolePermissions.jsx";
import NotFound from "../pages/error/NotFound.jsx";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        {/* Protected Application Routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route element={<ProtectedRoute moduleCode="DASHBOARD" moduleName="Dashboard" />}>
              <Route path="/dashboard" element={<Dashboard />} />
            </Route>

            <Route element={<ProtectedRoute moduleCode="EMPLOYEES_DEVICES" moduleName="Employees & Devices" />}>
              <Route path="/employees" element={<Employees />} />
              <Route path="/employees/:id" element={<EmployeeDetail />} />
            </Route>

            <Route element={<ProtectedRoute moduleCode="ACCESSORIES" moduleName="Accessories" />}>
              <Route path="/accessories" element={<Accessories />} />
            </Route>

            <Route element={<ProtectedRoute moduleCode="LICENSE_MANAGEMENT" moduleName="License Management" />}>
              <Route path="/licenses" element={<Licenses />} />
            </Route>

            <Route element={<ProtectedRoute moduleCode="NOTIFICATIONS_VULNERABILITIES" moduleName="Notifications & Security" />}>
              <Route path="/notifications" element={<Notifications />} />
            </Route>

            <Route element={<ProtectedRoute moduleCode="ROLE_PERMISSIONS" moduleName="Role Permissions" />}>
              <Route path="/roles" element={<RolePermissions />} />
            </Route>
          </Route>
        </Route>

        {/* Dedicated 404 Error Page for unmatched routes */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}
