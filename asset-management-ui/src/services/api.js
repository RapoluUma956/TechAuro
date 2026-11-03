const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

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

/**
 * Returns a polite, professional error message suitable for end-user display.
 * Replaces raw technical exceptions, network traces, and unhelpful error strings.
 */
function getProfessionalErrorMessage(status, serverMessage) {
  if (
    serverMessage &&
    typeof serverMessage === "string" &&
    !serverMessage.toLowerCase().includes("exception") &&
    !serverMessage.toLowerCase().includes("stacktrace") &&
    !serverMessage.toLowerCase().includes("failed to fetch") &&
    !serverMessage.toLowerCase().includes("sql") &&
    !serverMessage.toLowerCase().includes("internal server error") &&
    !serverMessage.toLowerCase().includes("nullpointer")
  ) {
    return serverMessage;
  }

  switch (status) {
    case 0:
      return "Unable to connect to the server. Please check your network connection or verify that services are running.";
    case 400:
      return "The request contains invalid parameters. Please check your input and try again.";
    case 401:
      return "Your session has expired or authentication is required. Please log in again.";
    case 403:
      return "You do not have administrative permission to perform this action.";
    case 404:
      return "The requested record or resource could not be found.";
    case 409:
      return "A conflict occurred with an existing asset, user, or record.";
    case 422:
      return "Unable to process the request due to data validation constraints.";
    case 500:
      return "The server encountered an unexpected situation. Please try again in a few moments.";
    case 502:
    case 503:
    case 504:
      return "The application service is temporarily unavailable. Please try again shortly.";
    default:
      return "An unexpected issue occurred while communicating with the server. Please try again.";
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

  let url = `${API_BASE_URL}${endpoint}`;
  if (options.params && typeof options.params === "object") {
    const searchParams = new URLSearchParams();
    Object.entries(options.params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        searchParams.append(key, value);
      }
    });
    const qs = searchParams.toString();
    if (qs) {
      url += (url.includes("?") ? "&" : "?") + qs;
    }
  }

  let response;
  try {
    response = await fetch(url, {
      ...options,
      headers,
      credentials: "include", // Ensure cookies are sent (for HttpOnly refreshToken)
    });
  } catch (err) {
    throw new ApiError(
      0,
      "Unable to connect to the server. Please check your network connection or verify that services are active."
    );
  }

  if (response.status === 204) {
    return null;
  }

  let body = null;
  const contentType = response.headers.get("content-type");
  if (contentType && contentType.includes("application/json")) {
    try {
      body = await response.json();
    } catch {
      body = null;
    }
  } else {
    const text = await response.text();
    body = text ? { message: text } : null;
  }

  if (!response.ok) {
    const rawMsg = body?.message || "";
    const professionalMsg = getProfessionalErrorMessage(response.status, rawMsg);
    const fieldErrors = body?.errors || [];
    throw new ApiError(response.status, professionalMsg, fieldErrors, body?.data);
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
  patch: (endpoint, data, options) =>
    request(endpoint, {
      ...options,
      method: "PATCH",
      body: data !== undefined ? JSON.stringify(data) : undefined,
    }),
  delete: (endpoint, options) =>
    request(endpoint, { ...options, method: "DELETE" }),
};
