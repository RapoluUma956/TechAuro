import { BrowserRouter, Navigate, Routes, Route } from "react-router-dom";

import Login from "../pages/auth/Login.jsx";
import Dashboard from "../pages/dashboard/Dashboard.jsx";
import Employees from "../pages/employees/Employees.jsx";
import EmployeeDetail from "../pages/employees/EmployeeDetail.jsx";
import ProtectedRoute from "./ProtectedRoute.jsx";
import AppLayout from "../components/layout/AppLayout.jsx";
import {
  roleCatalogHeaderData,
  accessoriesHeaderData,
  licensesHeaderData,
  notificationsHeaderData,
} from "../data";

function FeaturePage({ headerData }) {
  return (
    <div className="space-y-6 text-[var(--color-text)]">
      <header>
        <h1 className="mt-2 text-3xl font-bold text-[var(--color-evergreen)]">
          {headerData?.title}
        </h1>
        <p className="mt-2 text-sm text-[var(--color-blue)]">
          {headerData?.subtitle}
        </p>
      </header>

      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 shadow-sm">
        <p className="text-sm text-[var(--color-muted)]">
          Feature is ready for API integration.
        </p>
      </div>
    </div>
  );
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/employees" element={<Employees />} />
            <Route path="/employees/:id" element={<EmployeeDetail />} />
            <Route
              path="/role-catalog"
              element={<FeaturePage headerData={roleCatalogHeaderData} />}
            />
            <Route
              path="/accessories"
              element={<FeaturePage headerData={accessoriesHeaderData} />}
            />
            <Route
              path="/licenses"
              element={<FeaturePage headerData={licensesHeaderData} />}
            />
            <Route
              path="/notifications"
              element={<FeaturePage headerData={notificationsHeaderData} />}
            />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
