import React, { useState } from 'react';
import { Key, UserCheck, CheckCircle2, AlertCircle } from 'lucide-react';
import { authApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function AuthCard() {
  const { login, isAuthenticated, userEmail, logout } = useAuth();
  const [isLoginMode, setIsLoginMode] = useState(true);

  // Form fields
  const [name, setName] = useState('John Doe');
  const [email, setEmail] = useState('student@example.com');
  const [password, setPassword] = useState('password123');
  const [role, setRole] = useState('STUDENT');

  // Status feedback
  const [status, setStatus] = useState({ type: '', message: '' });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });

    try {
      if (isLoginMode) {
        // Call Auth Service Login endpoint
        const res = await authApi.login({ email, password });
        if (res?.token) {
          login(res.token, res.email || email, res.role || role, res.userId);
          setStatus({ type: 'success', message: `Login successful! Student ID: #${res.userId || 'N/A'}` });
        } else {
          setStatus({ type: 'error', message: 'Token not received from auth service.' });
        }
      } else {
        // Call Auth Service Register endpoint
        await authApi.register({ name, email, password, role });
        setStatus({ type: 'success', message: 'User registered successfully! You can now log in.' });
        setIsLoginMode(true);
      }
    } catch (err) {
      setStatus({ type: 'error', message: err.message || 'Authentication request failed' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">
          <Key size={20} color="#6366f1" /> Auth Service (Port 8080)
        </h2>
        <span className="badge">Microservice 1</span>
      </div>

      <p className="card-description">
        Register new users or log in to obtain a JWT token. The token is automatically saved and passed in the headers to other microservices.
      </p>

      {/* Toggle Register / Login */}
      <div className="tab-switcher">
        <button
          className={`tab-switch-btn ${isLoginMode ? 'active' : ''}`}
          onClick={() => { setIsLoginMode(true); setStatus({ type: '', message: '' }); }}
        >
          Login
        </button>
        <button
          className={`tab-switch-btn ${!isLoginMode ? 'active' : ''}`}
          onClick={() => { setIsLoginMode(false); setStatus({ type: '', message: '' }); }}
        >
          Register
        </button>
      </div>

      {status.message && (
        <div className={`alert-box alert-${status.type}`}>
          {status.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span>{status.message}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="form-container">
        {!isLoginMode && (
          <div className="form-group">
            <label className="form-label">Full Name</label>
            <input
              type="text"
              className="form-input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
        )}

        <div className="form-group">
          <label className="form-label">Email Address</label>
          <input
            type="email"
            className="form-input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Password</label>
          <input
            type="password"
            className="form-input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Role</label>
          <select
            className="form-select"
            value={role}
            onChange={(e) => setRole(e.target.value)}
          >
            <option value="STUDENT">STUDENT</option>
            <option value="TEACHER">TEACHER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Processing...' : isLoginMode ? 'Login & Get Token' : 'Create Account'}
        </button>
      </form>

      {isAuthenticated && (
        <div className="authenticated-info-box">
          <p>Logged in as: <strong>{userEmail}</strong></p>
          <button onClick={logout} className="btn btn-secondary btn-sm" style={{ marginTop: '0.5rem' }}>
            Logout / Clear JWT
          </button>
        </div>
      )}
    </div>
  );
}
