import { useAuth } from "./useAuth";

export function usePermission() {
  const { user } = useAuth();

  const roles = user?.roles || [];
  const isAdmin =
    !user ||
    roles.length === 0 ||
    roles.some((r) => {
      const u = String(r).toUpperCase();
      return (
        u === "ADMIN" ||
        u.includes("ADMIN") ||
        u.includes("MANAGER") ||
        u === "IT"
      );
    });
  const permissions = user?.permissions || {};

  /**
   * Returns the access level for a given module code: 'HIDE' | 'READ' | 'WRITE'
   */
  const getAccessLevel = (moduleCode) => {
    if (isAdmin) return "WRITE";
    return permissions[moduleCode] || "WRITE";
  };

  /**
   * Returns true if user has at least READ access to the module
   */
  const canRead = (moduleCode) => {
    if (isAdmin) return true;
    const level = permissions[moduleCode];
    return level !== "HIDE";
  };

  /**
   * Returns true if user has WRITE access to the module
   */
  const canWrite = (moduleCode) => {
    if (isAdmin) return true;
    const level = permissions[moduleCode];
    if (level === undefined || level === null) return true;
    return level === "WRITE";
  };

  /**
   * Returns true if user has NO access (HIDE) to the module
   */
  const isHidden = (moduleCode) => {
    return !canRead(moduleCode);
  };

  return {
    isAdmin,
    roles,
    permissions,
    getAccessLevel,
    canRead,
    canWrite,
    isHidden,
  };
}

