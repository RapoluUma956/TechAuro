import { api } from "./api";
import navigation from "../components/layout/navigation";

/**
 * Universal Search Service
 * Aggregates search across:
 * 1. Navigation Pages
 * 2. Employees (name, employeeCode, email, designation)
 * 3. Devices (assetTag, serialNumber, deviceName, brand, model)
 * 4. Accessories & Laptops (accessoryName, brand, model, category)
 * 5. Licenses (licenseName, softwareName, publisher, licenseKey)
 * 6. Notifications (title, message, severity)
 */
export const searchService = {
  async universalSearch(query) {
    if (!query || !query.trim()) {
      return {
        pages: [],
        employees: [],
        devices: [],
        accessories: [],
        licenses: [],
        notifications: [],
        total: 0,
      };
    }

    const cleanQuery = query.trim().toLowerCase();

    // 1. Search Pages / Navigation routes
    const matchedPages = navigation
      .filter((nav) => {
        const titleMatch = (nav.title || "").toLowerCase().includes(cleanQuery);
        const segmentMatch = (nav.segment || "").toLowerCase().includes(cleanQuery);
        return titleMatch || segmentMatch;
      })
      .map((nav) => ({
        id: `page-${nav.segment}`,
        type: "Page",
        title: nav.title,
        subtitle: `Navigate to ${nav.link}`,
        link: nav.link,
      }));

    // 2. Query Backend endpoints in parallel
    const [
      employeesRes,
      devicesRes,
      accessoriesRes,
      laptopConfigsRes,
      licensesRes,
      notificationsRes,
    ] = await Promise.allSettled([
      // Employees
      api.get(`/employees?search=${encodeURIComponent(cleanQuery)}&size=8&page=0`),
      // Devices
      api.get(`/devices`),
      // Accessories
      api.get(`/accessories?search=${encodeURIComponent(cleanQuery)}&size=8&page=0`),
      // Laptop Configurations
      api.get(`/accessories/laptops/configurations?search=${encodeURIComponent(cleanQuery)}&size=8&page=0`),
      // Licenses
      api.get(`/licenses?search=${encodeURIComponent(cleanQuery)}&size=8&page=0`),
      // Notifications
      api.get(`/notifications?search=${encodeURIComponent(cleanQuery)}&size=8&page=0`),
    ]);

    // Process Employees
    let matchedEmployees = [];
    if (employeesRes.status === "fulfilled" && employeesRes.value) {
      const data = employeesRes.value;
      const list = Array.isArray(data)
        ? data
        : Array.isArray(data.content)
        ? data.content
        : [];
      matchedEmployees = list.slice(0, 5).map((emp) => {
        const fullName =
          emp.fullName ||
          [emp.firstName, emp.lastName].filter(Boolean).join(" ") ||
          emp.employeeName ||
          "Employee";
        return {
          id: `emp-${emp.employeeId || emp.id}`,
          type: "Employee",
          title: fullName,
          subtitle: `${emp.employeeCode || `EMP-${emp.employeeId}`} · ${emp.designation || emp.role || "Staff"} · ${emp.email || ""}`,
          link: `/employees/${emp.employeeId || emp.id}`,
          meta: emp.department || emp.designation,
        };
      });
    }

    // Process Devices
    let matchedDevices = [];
    if (devicesRes.status === "fulfilled" && Array.isArray(devicesRes.value)) {
      matchedDevices = devicesRes.value
        .filter((d) => {
          const serial = (d.serialNumber || "").toLowerCase();
          const tag = (d.assetTag || "").toLowerCase();
          const name = (d.deviceName || "").toLowerCase();
          const brand = (d.brand || "").toLowerCase();
          const model = (d.model || "").toLowerCase();
          return (
            serial.includes(cleanQuery) ||
            tag.includes(cleanQuery) ||
            name.includes(cleanQuery) ||
            brand.includes(cleanQuery) ||
            model.includes(cleanQuery)
          );
        })
        .slice(0, 5)
        .map((d) => ({
          id: `dev-${d.deviceId || d.id}`,
          type: "Device",
          title: d.deviceName || `${d.brand || ""} ${d.model || ""}`.trim() || d.serialNumber || "Hardware Device",
          subtitle: `Tag: ${d.assetTag || "N/A"} · SN: ${d.serialNumber || "N/A"} · Status: ${d.deviceStatus || "ACTIVE"}`,
          link: `/employees`,
          meta: d.assignmentStatus || d.deviceStatus,
        }));
    }

    // Process Accessories & Laptops
    let matchedAccessories = [];
    if (accessoriesRes.status === "fulfilled" && accessoriesRes.value) {
      const data = accessoriesRes.value;
      const list = Array.isArray(data)
        ? data
        : Array.isArray(data.content)
        ? data.content
        : [];
      matchedAccessories.push(
        ...list.slice(0, 4).map((acc) => ({
          id: `acc-${acc.accessoryId || acc.id}`,
          type: "Accessory",
          title: acc.accessoryName || acc.productName || "Peripheral Accessory",
          subtitle: `${acc.category || "General"} · Available: ${acc.availableQuantity ?? 0} · Total: ${acc.totalQuantity ?? 0}`,
          link: `/accessories`,
          meta: acc.brand || acc.category,
        }))
      );
    }
    if (laptopConfigsRes.status === "fulfilled" && laptopConfigsRes.value) {
      const data = laptopConfigsRes.value;
      const list = Array.isArray(data)
        ? data
        : Array.isArray(data.content)
        ? data.content
        : [];
      matchedAccessories.push(
        ...list.slice(0, 3).map((l) => ({
          id: `lap-${l.brand}-${l.model}-${l.ram}-${l.ssd}`,
          type: "Laptop Stock",
          title: `${l.brand} ${l.model}`,
          subtitle: `${l.ram || ""} RAM / ${l.ssd || ""} SSD · Total Stock: ${l.total ?? 0} · Available: ${l.remaining ?? 0}`,
          link: `/accessories`,
          meta: `${l.total ?? 0} units`,
        }))
      );
    }

    // Process Licenses
    let matchedLicenses = [];
    if (licensesRes.status === "fulfilled" && licensesRes.value) {
      const data = licensesRes.value;
      const list = Array.isArray(data)
        ? data
        : Array.isArray(data.content)
        ? data.content
        : [];
      matchedLicenses = list.slice(0, 4).map((lic) => ({
        id: `lic-${lic.licenseId || lic.id}`,
        type: "License",
        title: lic.licenseName || lic.softwareName || "Software License",
        subtitle: `${lic.softwareName || ""} · Publisher: ${lic.publisher || "—"} · Available Seats: ${lic.availableQuantity ?? 0}`,
        link: `/licenses`,
        meta: lic.status || "ACTIVE",
      }));
    }

    // Process Notifications
    let matchedNotifications = [];
    if (notificationsRes.status === "fulfilled" && notificationsRes.value) {
      const data = notificationsRes.value;
      const list = Array.isArray(data)
        ? data
        : Array.isArray(data.content)
        ? data.content
        : [];
      matchedNotifications = list.slice(0, 3).map((n) => ({
        id: `notif-${n.notificationId}`,
        type: "Alert",
        title: n.title,
        subtitle: `${n.notificationType || "SYSTEM"} · Severity: ${n.severity || "INFO"} · ${n.message || ""}`,
        link: `/notifications`,
        meta: n.severity,
      }));
    }

    const total =
      matchedPages.length +
      matchedEmployees.length +
      matchedDevices.length +
      matchedAccessories.length +
      matchedLicenses.length +
      matchedNotifications.length;

    return {
      pages: matchedPages,
      employees: matchedEmployees,
      devices: matchedDevices,
      accessories: matchedAccessories,
      licenses: matchedLicenses,
      notifications: matchedNotifications,
      total,
    };
  },
};

export default searchService;

