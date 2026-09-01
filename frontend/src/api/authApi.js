import { SERVICES, fetchApi } from './apiConfig';

/**
 * Auth Service API Calls (Port 8080)
 */
export const authApi = {
  // Register a new user (STUDENT / TEACHER / ADMIN)
  register: async (userData) => {
    return fetchApi(`${SERVICES.AUTH}/auth/register`, 'POST', userData);
  },

  // Login with email and password to receive JWT token
  login: async (credentials) => {
    return fetchApi(`${SERVICES.AUTH}/auth/login`, 'POST', credentials);
  }
};
