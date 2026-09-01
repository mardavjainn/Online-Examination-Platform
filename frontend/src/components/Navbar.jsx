import React from 'react';
import { BookOpen, Key, User, LogOut, Shield, FileText, Play, Award } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Navbar({ activeTab, setActiveTab }) {
  const { token, userEmail, userRole, userId, logout, isAuthenticated } = useAuth();

  return (
    <header className="navbar">
      <div className="navbar-brand">
        <div className="logo-icon">
          <BookOpen size={22} color="#ffffff" />
        </div>
        <div>
          <h1 className="navbar-title">ExamPlatform</h1>
          <p className="navbar-subtitle">Microservices Learning Portal</p>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="nav-tabs">
        <button 
          className={`nav-btn ${activeTab === 'auth' ? 'active' : ''}`}
          onClick={() => setActiveTab('auth')}
        >
          <Key size={16} /> Auth (8080)
        </button>
        <button 
          className={`nav-btn ${activeTab === 'exams' ? 'active' : ''}`}
          onClick={() => setActiveTab('exams')}
        >
          <FileText size={16} /> Exam Creator (8082)
        </button>
        <button 
          className={`nav-btn ${activeTab === 'take-exam' ? 'active' : ''}`}
          onClick={() => setActiveTab('take-exam')}
        >
          <Play size={16} /> Take Exam (8083)
        </button>
        <button 
          className={`nav-btn ${activeTab === 'results' ? 'active' : ''}`}
          onClick={() => setActiveTab('results')}
        >
          <Award size={16} /> Results (8084)
        </button>
      </nav>

      {/* User Status / JWT Badge */}
      <div className="user-badge-box">
        {isAuthenticated ? (
          <div className="auth-pill active">
            <Shield size={14} />
            <span>{userEmail} {userId ? `(ID: #${userId})` : ''} [{userRole}]</span>
            <button onClick={logout} className="logout-btn" title="Logout">
              <LogOut size={14} />
            </button>
          </div>
        ) : (
          <div className="auth-pill inactive">
            <User size={14} />
            <span>No JWT Token</span>
          </div>
        )}
      </div>
    </header>
  );
}
