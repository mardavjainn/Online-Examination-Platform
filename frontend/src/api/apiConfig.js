// Centralized Configuration for Microservices Endpoints & HTTP Client
export const SERVICES = {
  AUTH: 'http://localhost:8080',
  EXAM: 'http://localhost:8082',
  SUBMISSION: 'http://localhost:8083',
  RESULT: 'http://localhost:8084'
};

/**
 * Reusable HTTP helper for microservice API calls.
 * Automatically injects JWT Bearer token if present in localStorage.
 */
export async function fetchApi(url, method = 'GET', body = null, requireAuth = false) {
  const headers = { 'Content-Type': 'application/json' };
  
  const token = localStorage.getItem('jwt_token');
  if (requireAuth && token) {
    headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  }

  const options = { method, headers };
  if (body) {
    options.body = JSON.stringify(body);
  }

  try {
    const res = await fetch(url, options);
    const text = await res.text();
    let data = null;

    try {
      data = text ? JSON.parse(text) : null;
    } catch {
      data = text;
    }

    if (!res.ok) {
      const errorMessage = typeof data === 'object' && data?.message 
        ? data.message 
        : `HTTP Error ${res.status}: ${res.statusText}`;
      throw new Error(errorMessage);
    }

    return data;
  } catch (err) {
    console.error(`[API Error] ${method} ${url}:`, err.message);
    throw err;
  }
}
