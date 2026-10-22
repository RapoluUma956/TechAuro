import { useState, useRef, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { dashboardHeaderData } from "../../data/dashboardData";

export default function AppHeader() {
  const [searchQuery, setSearchQuery] = useState("");
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const mobileSearchContainerRef = useRef(null);
  const mobileInputRef = useRef(null);
  const profileDropdownRef = useRef(null);
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const profileName = user?.username
    ? user.username === "admin"
      ? "Admin"
      : user.username
    : dashboardHeaderData.userProfile.name;

  const profileRole =
    user?.role === "ADMIN"
      ? "IT Administrator"
      : user?.role || dashboardHeaderData.userProfile.role;

  const profileInitials = user?.username
    ? user.username === "admin"
      ? "AD"
      : user.username.slice(0, 2).toUpperCase()
    : dashboardHeaderData.userProfile.initials;

  const profileEmail = user?.username
    ? `${user.username.toLowerCase()}@techaura.io`
    : "admin@techaura.io";

  const handleLogout = () => {
    setIsProfileMenuOpen(false);
    logout();
    navigate("/login", { replace: true });
  };

  // Focus mobile input when opened
  useEffect(() => {
    if (isMobileSearchOpen) {
      const timer = setTimeout(() => {
        mobileInputRef.current?.focus();
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [isMobileSearchOpen]);

  // Click outside & Escape key listener for mobile search bar
  useEffect(() => {
    if (!isMobileSearchOpen) return;

    const handleClickOutside = (e) => {
      if (
        mobileSearchContainerRef.current &&
        !mobileSearchContainerRef.current.contains(e.target)
      ) {
        setIsMobileSearchOpen(false);
      }
    };

    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        setIsMobileSearchOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isMobileSearchOpen]);

  // Click outside & Escape key listener for user profile dropdown
  useEffect(() => {
    if (!isProfileMenuOpen) return;

    const handleClickOutside = (e) => {
      if (
        profileDropdownRef.current &&
        !profileDropdownRef.current.contains(e.target)
      ) {
        setIsProfileMenuOpen(false);
      }
    };

    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        setIsProfileMenuOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isProfileMenuOpen]);

  // Close mobile search if window is resized to desktop width
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 768 && isMobileSearchOpen) {
        setIsMobileSearchOpen(false);
      }
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, [isMobileSearchOpen]);

  return (
    <header className="relative flex w-full items-center justify-between gap-3 sm:gap-4 py-1.5 sm:py-2 px-1">
      {/* Left: App Logo & Desktop Search */}
      <div className="flex items-center gap-3 lg:gap-5 min-w-0">
        {/* App Logo: Always display the complete TechAura logo with balanced height */}
        <Link
          to="/dashboard"
          className="flex items-center shrink-0 select-none group focus:outline-none py-0.5"
          title="TechAura - IT Asset Management"
        >
          <img
            src="/title.png"
            alt="TechAura Logo"
            className="h-8 sm:h-8.5 md:h-9 w-auto max-w-[155px] sm:max-w-[170px] object-contain transition-transform duration-200 group-hover:scale-[1.02]"
          />
        </Link>

        {/* Long / Big screens: Full Search Bar */}
        <div className="hidden md:block relative w-64 lg:w-80 xl:w-96">
          <svg
            className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            placeholder={dashboardHeaderData.searchPlaceholder}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full rounded-xl border border-[#DCE5E1] bg-white py-2 pl-10 pr-9 text-xs text-[#03312E] shadow-sm placeholder:text-[#94A3B8] focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 transition-all"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => setSearchQuery("")}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#03312E] transition-colors"
              aria-label="Clear search"
            >
              <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          )}
        </div>
      </div>

      {/* Right Section: Mobile Search Circle, Notification Bell, User Profile Dropdown */}
      <div className="flex items-center gap-2 sm:gap-3 shrink-0">
        {/* Small screens: Search circle button */}
        <button
          type="button"
          onClick={() => setIsMobileSearchOpen(true)}
          aria-label="Open search"
          title="Search"
          className="flex md:hidden h-9 w-9 items-center justify-center rounded-full border border-[#DCE5E1] bg-white text-[#64748B] shadow-sm hover:border-[#1976D2] hover:text-[#1976D2] hover:bg-[#F8FAFC] active:scale-95 transition-all"
        >
          <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </button>

        {/* Notification Bell */}
        <button
          type="button"
          onClick={() => navigate("/notifications")}
          aria-label="Notifications"
          title="Notifications"
          className="relative flex h-9 w-9 sm:h-10 sm:w-10 items-center justify-center rounded-xl border border-[#DCE5E1] bg-white text-[#64748B] shadow-sm hover:bg-[#F8FAFC] hover:text-[#1976D2] transition-colors"
        >
          <svg className="h-4 w-4 sm:h-5 sm:w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          <span className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-[#EF4444] text-[10px] font-bold text-white">
            {dashboardHeaderData.unreadNotificationCount}
          </span>
        </button>

        {/* User Profile Dropdown */}
        <div className="relative" ref={profileDropdownRef}>
          <button
            type="button"
            id="user-profile-menu-button"
            aria-expanded={isProfileMenuOpen}
            aria-haspopup="true"
            aria-label="User profile menu"
            onClick={() => setIsProfileMenuOpen((prev) => !prev)}
            className={`flex items-center gap-2 sm:gap-2.5 rounded-xl border border-[#DCE5E1] bg-white px-2 sm:px-2.5 py-1.5 shadow-sm transition-all cursor-pointer hover:bg-[#F8FAFC] hover:border-[#CBD5E1] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/25 select-none ${
              isProfileMenuOpen ? "border-[#1976D2] ring-2 ring-[#1976D2]/20 bg-[#F8FAFC]" : ""
            }`}
            title={`${profileName} (${profileRole})`}
          >
            <div className="flex h-7 w-7 sm:h-8 sm:w-8 items-center justify-center rounded-full bg-[#1E293B] text-xs font-bold text-white shadow-xs">
              {profileInitials}
            </div>
            <div className="hidden text-left text-xs sm:block">
              <p className="font-bold leading-tight text-[#03312E]">{profileName}</p>
              <p className="text-[11px] leading-tight text-[#64748B]">{profileRole}</p>
            </div>
            <svg
              className={`h-4 w-4 text-[#64748B] transition-transform duration-200 ${
                isProfileMenuOpen ? "rotate-180 text-[#1976D2]" : ""
              }`}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </button>

          {/* Profile Dropdown Menu */}
          {isProfileMenuOpen && (
            <div
              className="absolute right-0 top-full mt-2 w-64 origin-top-right rounded-2xl border border-[#DCE5E1] bg-white p-2 shadow-xl shadow-slate-900/10 z-50 animate-in fade-in zoom-in-95 duration-150 ring-1 ring-black/5"
              role="menu"
              aria-orientation="vertical"
              aria-labelledby="user-profile-menu-button"
            >
              {/* User Identity Section */}
              <div className="flex items-center gap-3 p-2.5 rounded-xl bg-[#F8FAFC] border border-[#E2E8F0]">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#1E293B] text-sm font-bold text-white shadow-xs">
                  {profileInitials}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-1.5">
                    <p className="truncate text-xs font-bold text-[#03312E]">
                      {profileName}
                    </p>
                    <span className="inline-flex items-center rounded-full bg-emerald-50 px-1.5 py-0.5 text-[9px] font-semibold text-emerald-700 border border-emerald-200">
                      Active
                    </span>
                  </div>
                  <p className="truncate text-[11px] text-[#64748B] font-medium">
                    {profileRole}
                  </p>
                  <p className="truncate text-[10px] text-[#94A3B8]">
                    {profileEmail}
                  </p>
                </div>
              </div>

              {/* Quick Info */}
              <div className="px-3 py-2 text-[11px] text-[#64748B] flex items-center justify-between">
                <span>Account status</span>
                <span className="font-semibold text-[#03312E]">Authenticated</span>
              </div>

              <div className="my-1 border-t border-[#E2E8F0]" />

              {/* Logout Option */}
              <button
                type="button"
                id="logout-button"
                onClick={handleLogout}
                className="flex w-full items-center gap-2.5 rounded-xl px-2.5 py-2 text-xs font-semibold text-[#DC2626] hover:bg-red-50 active:bg-red-100 transition-colors cursor-pointer group"
                role="menuitem"
              >
                <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-red-100/70 text-[#DC2626] group-hover:bg-red-200/80 transition-colors">
                  <svg
                    className="h-4 w-4 transition-transform group-hover:translate-x-0.5"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                </span>
                <div className="flex flex-col text-left">
                  <span className="leading-tight text-red-600 font-bold">Log Out</span>
                  <span className="text-[10px] font-normal text-red-500/80 leading-tight">
                    Sign out of your session
                  </span>
                </div>
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Small screens: Full Search Bar appears when circle is clicked */}
      {isMobileSearchOpen && (
        <div
          ref={mobileSearchContainerRef}
          className="absolute inset-0 z-30 flex items-center bg-white px-2 py-1 gap-2 rounded-xl shadow-md border border-[#1976D2]/30 md:hidden animate-in fade-in zoom-in-95 duration-150"
        >
          <div className="relative flex-1 flex items-center">
            <svg
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input
              ref={mobileInputRef}
              type="text"
              placeholder={dashboardHeaderData.searchPlaceholder}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full rounded-xl border border-[#DCE5E1] bg-[#F8FAFC] py-2 pl-9 pr-8 text-xs text-[#03312E] placeholder:text-[#94A3B8] focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 transition-all"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery("")}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#03312E] transition-colors"
                aria-label="Clear search"
              >
                <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            )}
          </div>
          <button
            type="button"
            onClick={() => setIsMobileSearchOpen(false)}
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-[#DCE5E1] bg-[#F8FAFC] text-[#64748B] hover:bg-[#F1F5F9] hover:text-[#03312E] shadow-sm transition-colors"
            aria-label="Close search"
            title="Close search"
          >
            <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
      )}
    </header>
  );
}

