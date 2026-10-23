const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

let currentAccessToken = null;
let currentCsrfToken = null;
let onAuthFailureCallback = null;
let isRefreshing = false;
let refreshSubscribers = [];

export class ApiError extends Error {
  constructor(status, message, errors = [], data = null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.errors = errors;
    this.data = data;
  }
}

export function setAccessToken(token) {
  currentAccessToken = token;
}

export function getAccessToken() {
  return currentAccessToken;
}

export function setCsrfToken(token) {
  currentCsrfToken = token;
}

export function getCsrfToken() {
  return currentCsrfToken;
}

export function setOnAuthFailure(callback) {
  onAuthFailureCallback = callback;
}

function onRefreshed(newAccessToken) {
  refreshSubscribers.forEach((callback) => callback(newAccessToken));
  refreshSubscribers = [];
}

function addRefreshSubscriber(callback) {
  refreshSubscribers.push(callback);
}

async function executeFetch(endpoint, options = {}, tokenOverride = null) {
  const token = tokenOverride || currentAccessToken;
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const method = (options.method || "GET").toUpperCase();
  if (["POST", "PUT", "DELETE", "PATCH"].includes(method) && currentCsrfToken) {
    headers["X-CSRF-TOKEN"] = currentCsrfToken;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
    credentials: "include", // Ensure cookies are sent (for HttpOnly refreshToken)
  });

  if (response.status === 204) {
    return null;
  }

  let body = null;
  const contentType = response.headers.get("content-type");
  if (contentType && contentType.includes("application/json")) {
    body = await response.json();
  } else {
    const text = await response.text();
    body = text ? { message: text } : null;
  }

  if (!response.ok) {
    const errorMsg = body?.message || `Request failed with status ${response.status}`;
    const fieldErrors = body?.errors || [];
    throw new ApiError(response.status, errorMsg, fieldErrors, body?.data);
  }

  return body;
}

async function request(endpoint, options = {}) {
  try {
    return await executeFetch(endpoint, options);
  } catch (error) {
    // If 401 on protected request, attempt a single refresh
    const isAuthEndpoint =
      endpoint.startsWith("/auth/login") ||
      endpoint.startsWith("/auth/refresh") ||
      endpoint.startsWith("/auth/csrf");

    if (error instanceof ApiError && error.status === 401 && !isAuthEndpoint) {
      if (!isRefreshing) {
        isRefreshing = true;

        try {
          // Fetch new access token using HttpOnly cookie
          const refreshHeaders = {};
          if (currentCsrfToken) {
            refreshHeaders["X-CSRF-TOKEN"] = currentCsrfToken;
          }

          const refreshRes = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: "POST",
            headers: refreshHeaders,
            credentials: "include",
          });

          if (!refreshRes.ok) {
            isRefreshing = false;
            setAccessToken(null);
            if (onAuthFailureCallback) onAuthFailureCallback();
            throw error;
          }

          const refreshData = await refreshRes.json();
          const newAccessToken = refreshData?.data?.accessToken;
          setAccessToken(newAccessToken);
          isRefreshing = false;
          onRefreshed(newAccessToken);

          // Retry failed request with new access token
          return await executeFetch(endpoint, options, newAccessToken);
        } catch (refreshErr) {
          isRefreshing = false;
          setAccessToken(null);
          if (onAuthFailureCallback) onAuthFailureCallback();
          throw error;
        }
      } else {
        // Queue this request until refresh completes
        return new Promise((resolve, reject) => {
          addRefreshSubscriber((newAccessToken) => {
            executeFetch(endpoint, options, newAccessToken)
              .then(resolve)
              .catch(reject);
          });
        });
      }
    }

    throw error;
  }
}

export const api = {
  get: (endpoint, options) => request(endpoint, { ...options, method: "GET" }),
  post: (endpoint, data, options) =>
    request(endpoint, {
      ...options,
      method: "POST",
      body: data !== undefined ? JSON.stringify(data) : undefined,
    }),
  put: (endpoint, data, options) =>
    request(endpoint, {
      ...options,
      method: "PUT",
      body: data !== undefined ? JSON.stringify(data) : undefined,
    }),
  delete: (endpoint, options) =>
    request(endpoint, { ...options, method: "DELETE" }),
};
