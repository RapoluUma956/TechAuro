import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { useState, useEffect, useCallback } from "react";
import { createTheme } from "@mui/material/styles";

import {
  AppLayout as ToolkitAppLayout,
  AppLayoutProvider,
} from "eworldes-ui-toolkit-v1.1.0";

import navigation from "./navigation.jsx";
import AppHeader from "./AppHeader.jsx";

// App Logo Icon
const AppLogo = () => (
  <img src="/logo.png" alt="TechAura Logo" className="h-8 w-8 object-contain" />
);

const techAuraTheme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#1976D2",
      dark: "#1565C0",
      contrastText: "#FFFFFF",
    },
    secondary: {
      main: "#60B994",
      contrastText: "#24211D",
    },
    background: {
      default: "#F5F8F7",
      paper: "#FFFFFF",
    },
    text: {
      primary: "#24211D",
      secondary: "#64748B",
    },
    divider: "#DCE5E1",
  },
  typography: {
    fontFamily: '"Manrope", sans-serif',
    button: {
      textTransform: "none",
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 10,
  },
  components: {
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          margin: "4px 8px",
          padding: "8px 12px",
          color: "#03312E",
          "&:hover": {
            backgroundColor: "#E3F2FD",
          },
          "&.Mui-selected": {
            backgroundColor: "#1976D2 !important",
            color: "#FFFFFF !important",
            "& .MuiTypography-root": {
              color: "#FFFFFF !important",
              fontWeight: 700,
            },
            "& .MuiListItemIcon-root": {
              color: "#FFFFFF !important",
            },
            "& .MuiListItemIcon-root svg": {
              color: "#FFFFFF !important",
              stroke: "#FFFFFF",
            },
            "&:hover": {
              backgroundColor: "#1565C0 !important",
            },
          },
        },
      },
    },
  },
});

export default function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();

  // Persist sidebar collapsed state across page reloads
  const [initialSidebarCollapsed] = useState(() => {
    return localStorage.getItem("asset_management_sidebar_collapsed") === "true";
  });

  // Intercept sidebar link clicks so React Router performs client-side SPA navigation without full page reload
  useEffect(() => {
    const handleNavClick = (e) => {
      const anchor = e.target.closest(".MuiDrawer-paper a[href]");
      if (anchor) {
        const href = anchor.getAttribute("href");
        if (href && href.startsWith("/") && !href.startsWith("//")) {
          e.preventDefault();
          navigate(href);
        }
      }
    };
    document.addEventListener("click", handleNavClick);
    return () => document.removeEventListener("click", handleNavClick);
  }, [navigate]);

  // Keep localStorage in sync whenever the hamburger toggle button is clicked
  const handleToggle = useCallback(() => {
    const btn = document.querySelector('button[aria-label*="navigation menu"]');
    const label = btn?.getAttribute("aria-label") || "";
    // If the button says "Collapse navigation menu", clicking it will collapse it (true).
    // If the button says "Expand navigation menu", clicking it will expand it (false).
    if (label.includes("Collapse")) {
      localStorage.setItem("asset_management_sidebar_collapsed", "true");
    } else if (label.includes("Expand")) {
      localStorage.setItem("asset_management_sidebar_collapsed", "false");
    }

    // Confirm after transition completes (350ms)
    setTimeout(() => {
      const drawers = document.querySelectorAll(".MuiDrawer-paper");
      const visibleDrawer = Array.from(drawers).find((d) => d.offsetWidth > 0);
      if (visibleDrawer) {
        const isCollapsed = visibleDrawer.offsetWidth <= 100;
        localStorage.setItem("asset_management_sidebar_collapsed", String(isCollapsed));
      }
    }, 350);
  }, []);

  const router = {
    pathname: location.pathname,
    searchParams: new URLSearchParams(location.search),
    navigate: (url) => navigate(url),
  };

  const branding = {
    appTitle: "IT Asset Management",
    appLogo: <AppLogo />,
    appHomeUrl: "/dashboard",
  };

  return (
    <AppLayoutProvider
      theme={techAuraTheme}
      navigation={navigation}
      router={router}
      branding={branding}
    >
      <ToolkitAppLayout
        defaultSidebarCollapsed={initialSidebarCollapsed}
        navExpandedToggleHandler={handleToggle}
        sidebarExpandedWidth={260}
        toolbarView={<AppHeader />}
      >
        <div className="app-content-background min-h-screen p-6">
          <Outlet />
        </div>
      </ToolkitAppLayout>
    </AppLayoutProvider>
  );
}
