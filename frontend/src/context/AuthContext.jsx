import React, { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('jwt_token') || '');
  const [userEmail, setUserEmail] = useState(localStorage.getItem('user_email') || '');
  const [userRole, setUserRole] = useState(localStorage.getItem('user_role') || 'STUDENT');
  const [userId, setUserId] = useState(localStorage.getItem('user_id') || '');

  const login = (jwtToken, email, role = 'STUDENT', id = '') => {
    setToken(jwtToken);
    setUserEmail(email);
    setUserRole(role);
    setUserId(id ? id.toString() : '');
    localStorage.setItem('jwt_token', jwtToken);
    localStorage.setItem('user_email', email);
    localStorage.setItem('user_role', role);
    if (id) localStorage.setItem('user_id', id.toString());
  };

  const logout = () => {
    setToken('');
    setUserEmail('');
    setUserRole('STUDENT');
    setUserId('');
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_email');
    localStorage.removeItem('user_role');
    localStorage.removeItem('user_id');
  };

  return (
    <AuthContext.Provider value={{ token, userEmail, userRole, userId, login, logout, isAuthenticated: Boolean(token) }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
