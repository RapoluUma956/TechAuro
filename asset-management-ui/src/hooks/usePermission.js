import { useAuth } from "./useAuth";

export function usePermission() {
  const { user } = useAuth();

  const roles = user?.roles || [];
  const isAdmin = roles.some((r) => String(r).toUpperCase() === "ADMIN");
  const permissions = user?.permissions || {};

  /**
   * Returns the access level for a given module code: 'HIDE' | 'READ' | 'WRITE'
   */
  const getAccessLevel = (moduleCode) => {
    if (isAdmin) return "WRITE";
    return permissions[moduleCode] || "HIDE";
  };

  /**
   * Returns true if user has at least READ access to the module
   */
  const canRead = (moduleCode) => {
    if (isAdmin) return true;
    const level = permissions[moduleCode];
    return level === "READ" || level === "WRITE";
  };

  /**
   * Returns true if user has WRITE access to the module
   */
  const canWrite = (moduleCode) => {
    if (isAdmin) return true;
    return permissions[moduleCode] === "WRITE";
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

