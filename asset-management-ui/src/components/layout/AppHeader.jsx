import { useState, useRef, useEffect, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import notificationService from "../../services/notificationService";
import searchService from "../../services/searchService";
import useAutoRefresh from "../../hooks/useAutoRefresh";

const DEFAULT_PROFILE = {
  name: "Admin User",
  role: "IT Administrator",
  initials: "AU",
};

export default function AppHeader() {
  const [searchQuery, setSearchQuery] = useState("");
  const [isSearching, setIsSearching] = useState(false);
  const [searchResults, setSearchResults] = useState(null);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [unreadNotifications, setUnreadNotifications] = useState(0);

  const desktopSearchContainerRef = useRef(null);
  const mobileSearchContainerRef = useRef(null);
  const mobileInputRef = useRef(null);
  const profileDropdownRef = useRef(null);
  const searchDebounceRef = useRef(null);

  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const profileName = user
    ? `${user.firstName || ""} ${user.lastName || ""}`.trim() || user.username || "User"
    : DEFAULT_PROFILE.name;

  const profileRole =
    user?.designation ||
    (user?.roles?.[0] ? (user.roles[0] === "ADMIN" ? "IT Administrator" : user.roles[0]) : (user?.role || DEFAULT_PROFILE.role));

  const profileInitials = user
    ? `${user.firstName?.[0] || ""}${user.lastName?.[0] || ""}`.toUpperCase() ||
      (user.username ? user.username.slice(0, 2).toUpperCase() : "AD")
    : DEFAULT_PROFILE.initials;

  const profileEmail = user?.email || "admin@example.com";

  // Fetch unread notification count
  const fetchNotificationCount = useCallback(async () => {
    try {
      const summary = await notificationService.getSummary();
      if (summary && typeof summary.unreadCount === "number") {
        setUnreadNotifications(summary.unreadCount);
      }
    } catch {
      // Non-blocking
    }
  }, []);

  useEffect(() => {
    fetchNotificationCount();
  }, [fetchNotificationCount]);

  useAutoRefresh(fetchNotificationCount, 30000);

  const handleLogout = () => {
    setIsProfileMenuOpen(false);
    logout();
    navigate("/login", { replace: true });
  };

  // Perform search with 250ms debounce
  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults(null);
      setIsSearching(false);
      return;
    }

    setIsSearching(true);
    if (searchDebounceRef.current) {
      clearTimeout(searchDebounceRef.current);
    }

    searchDebounceRef.current = setTimeout(async () => {
      try {
        const results = await searchService.universalSearch(searchQuery);
        setSearchResults(results);
        setIsDropdownOpen(true);
      } catch (err) {
        console.error("Universal search failed:", err);
      } finally {
        setIsSearching(false);
      }
    }, 250);

    return () => {
      if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    };
  }, [searchQuery]);

  // Handle clicking a search result item
  const handleSelectResult = (item) => {
    setIsDropdownOpen(false);
    setIsMobileSearchOpen(false);
    setSearchQuery("");
    if (item.link) {
      navigate(item.link);
    }
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

  // Click outside listener for search dropdown and profile menu
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (
        desktopSearchContainerRef.current &&
        !desktopSearchContainerRef.current.contains(e.target)
      ) {
        setIsDropdownOpen(false);
      }
      if (
        profileDropdownRef.current &&
        !profileDropdownRef.current.contains(e.target)
      ) {
        setIsProfileMenuOpen(false);
      }
      if (
        mobileSearchContainerRef.current &&
        !mobileSearchContainerRef.current.contains(e.target)
      ) {
        setIsMobileSearchOpen(false);
      }
    };

    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        setIsDropdownOpen(false);
        setIsProfileMenuOpen(false);
        setIsMobileSearchOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, []);

  // Helper for rendering search result groups
  const renderResultsList = () => {
    if (!searchResults) return null;

    if (searchResults.total === 0) {
      return (
        <div className="p-6 text-center text-xs text-[#64748B]">
          <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-500 mb-2">
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
          </div>
          <p className="font-bold text-[#03312E]">No matching items found</p>
          <p className="mt-0.5">Try searching with a different employee name, device asset tag, serial number, license, or page title.</p>
        </div>
      );
    }

    const categories = [
      { key: "pages", label: "Pages & Navigation", items: searchResults.pages, color: "text-[#1976D2] bg-blue-50" },
      { key: "employees", label: "Employees", items: searchResults.employees, color: "text-emerald-700 bg-emerald-50" },
      { key: "devices", label: "Devices & Hardware", items: searchResults.devices, color: "text-indigo-700 bg-indigo-50" },
      { key: "accessories", label: "Accessories & Stock", items: searchResults.accessories, color: "text-amber-700 bg-amber-50" },
      { key: "licenses", label: "Software Licenses", items: searchResults.licenses, color: "text-purple-700 bg-purple-50" },
      { key: "notifications", label: "Alerts & Security", items: searchResults.notifications, color: "text-rose-700 bg-rose-50" },
    ];

    return (
      <div className="max-h-[420px] overflow-y-auto divide-y divide-[#F1F5F9] p-2">
        {categories.map((cat) => {
          if (!cat.items || cat.items.length === 0) return null;
          return (
            <div key={cat.key} className="py-2 first:pt-1 last:pb-1">
              <div className="px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-[#64748B]">
                {cat.label} ({cat.items.length})
              </div>
              <div className="mt-1 space-y-0.5">
                {cat.items.map((item) => (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => handleSelectResult(item)}
                    className="w-full flex items-center justify-between gap-3 px-3 py-2 rounded-xl text-left hover:bg-[#F8FAF9] transition-colors cursor-pointer group"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span className={`px-1.5 py-0.5 text-[9px] font-bold rounded uppercase ${cat.color}`}>
                          {item.type}
                        </span>
                        <p className="text-xs font-bold text-[#03312E] group-hover:text-[#1976D2] truncate transition-colors">
                          {item.title}
                        </p>
                      </div>
                      <p className="text-[11px] text-[#64748B] truncate mt-0.5">
                        {item.subtitle}
                      </p>
                    </div>
                    {item.meta && (
                      <span className="shrink-0 text-[10px] font-semibold text-[#64748B] px-2 py-0.5 rounded bg-slate-100">
                        {item.meta}
                      </span>
                    )}
                  </button>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  return (
    <header className="relative flex w-full items-center justify-between gap-3 sm:gap-4 py-1.5 sm:py-2 px-1">
      {/* Left: App Logo & Universal Search Bar */}
      <div className="flex items-center gap-3 lg:gap-5 min-w-0 flex-1 max-w-2xl">
        {/* App Logo */}
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

        {/* Big screens: Universal Search Bar */}
        <div ref={desktopSearchContainerRef} className="hidden md:block relative flex-1">
          <div className="relative">
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
              placeholder="Search across employees, devices, licenses, accessories, and pages..."
              value={searchQuery}
              onFocus={() => {
                if (searchQuery.trim()) setIsDropdownOpen(true);
              }}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full rounded-xl border border-[#DCE5E1] bg-white py-2 pl-10 pr-9 text-xs text-[#03312E] shadow-sm placeholder:text-[#94A3B8] focus:border-[#1976D2] focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 transition-all"
            />
            {isSearching ? (
              <div className="absolute right-3 top-1/2 -translate-y-1/2">
                <div className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent" />
              </div>
            ) : searchQuery ? (
              <button
                type="button"
                onClick={() => {
                  setSearchQuery("");
                  setIsDropdownOpen(false);
                }}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#03312E] transition-colors"
                aria-label="Clear search"
              >
                <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            ) : null}
          </div>

          {/* Universal Search Results Popover */}
          {isDropdownOpen && searchQuery.trim() && (
            <div className="absolute left-0 right-0 top-full mt-1.5 z-50 rounded-2xl border border-[#DCE5E1] bg-white shadow-xl animate-in fade-in slide-in-from-top-2 duration-150 overflow-hidden">
              <div className="bg-[#F8FAF9] px-3.5 py-2 border-b border-[#DCE5E1] flex items-center justify-between text-xs font-semibold text-[#03312E]">
                <span>Universal Search Results</span>
                <span className="text-[11px] font-medium text-[#64748B]">
                  {searchResults?.total ?? 0} found
                </span>
              </div>
              {renderResultsList()}
            </div>
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
          title={`Notifications (${unreadNotifications} unread)`}
          className="relative flex h-9 w-9 sm:h-10 sm:w-10 items-center justify-center rounded-xl border border-[#DCE5E1] bg-white text-[#64748B] shadow-sm hover:bg-[#F8FAFC] hover:text-[#1976D2] transition-colors cursor-pointer"
        >
          <svg className="h-4 w-4 sm:h-5 sm:w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          {unreadNotifications > 0 && (
            <span className="absolute -right-1 -top-1 flex h-4 min-w-4 px-1 items-center justify-center rounded-full bg-[#EF4444] text-[10px] font-bold text-white shadow-xs">
              {unreadNotifications > 99 ? "99+" : unreadNotifications}
            </span>
          )}
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

          {/* User Profile Menu Popover */}
          {isProfileMenuOpen && (
            <div
              role="menu"
              aria-orientation="vertical"
              aria-labelledby="user-profile-menu-button"
              className="absolute right-0 top-full mt-2 w-64 origin-top-right rounded-2xl border border-[#DCE5E1] bg-white p-2 shadow-xl animate-in fade-in zoom-in-95 duration-150 z-50"
            >
              <div className="px-3 py-2.5 border-b border-[#F1F5F9]">
                <p className="text-xs font-bold text-[#03312E]">{profileName}</p>
                <p className="text-[11px] text-[#64748B] truncate">{profileEmail}</p>
                <div className="mt-1.5 inline-flex items-center gap-1 rounded-md bg-[#E3F2FD] px-2 py-0.5 text-[10px] font-bold text-[#1976D2]">
                  {profileRole}
                </div>
              </div>

              <div className="py-1">
                <Link
                  to="/roles"
                  onClick={() => setIsProfileMenuOpen(false)}
                  className="flex items-center gap-2.5 rounded-xl px-3 py-2 text-xs font-semibold text-[#03312E] hover:bg-[#F8FAF9] transition-colors"
                >
                  <svg className="h-4 w-4 text-[#64748B]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  </svg>
                  <span>Permissions & Roles</span>
                </Link>

                <Link
                  to="/reset-password"
                  onClick={() => setIsProfileMenuOpen(false)}
                  className="flex items-center gap-2.5 rounded-xl px-3 py-2 text-xs font-semibold text-[#03312E] hover:bg-[#F8FAF9] transition-colors"
                >
                  <svg className="h-4 w-4 text-[#64748B]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect width="18" height="11" x="3" y="11" rx="2" ry="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                  </svg>
                  <span>Change Password</span>
                </Link>
              </div>

              <div className="border-t border-[#F1F5F9] pt-1">
                <button
                  type="button"
                  onClick={handleLogout}
                  className="w-full flex items-center gap-2.5 rounded-xl px-3 py-2 text-xs font-semibold text-[#EF4444] hover:bg-[#FEECEB] transition-colors cursor-pointer"
                >
                  <svg className="h-4 w-4 text-[#EF4444]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                  <span>Log Out</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Small screens: Full Overlay Search */}
      {isMobileSearchOpen && (
        <div
          ref={mobileSearchContainerRef}
          className="absolute inset-0 z-50 flex flex-col bg-white px-2 py-1 gap-2 rounded-xl shadow-xl border border-[#1976D2]/30 md:hidden animate-in fade-in zoom-in-95 duration-150"
        >
          <div className="flex items-center gap-2">
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
                placeholder="Universal Search..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full rounded-xl border border-[#DCE5E1] bg-[#F8FAFC] py-2 pl-9 pr-8 text-xs text-[#03312E] placeholder:text-[#94A3B8] focus:border-[#1976D2] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#1976D2]/20 transition-all"
              />
              {isSearching ? (
                <div className="absolute right-2.5 top-1/2 -translate-y-1/2">
                  <div className="h-3 w-3 animate-spin rounded-full border-2 border-[#1976D2] border-t-transparent" />
                </div>
              ) : searchQuery ? (
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
              ) : null}
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

          {searchQuery.trim() && (
            <div className="max-h-80 overflow-y-auto border-t border-[#F1F5F9] mt-1 pt-1">
              {renderResultsList()}
            </div>
          )}
        </div>
      )}
    </header>
  );
}
