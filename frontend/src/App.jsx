import React, { useState } from 'react';
import './App.css';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import AuthCard from './components/AuthCard';
import ExamManager from './components/ExamManager';
import StudentExam from './components/StudentExam';
import ResultInspector from './components/ResultInspector';

export default function App() {
  const [activeTab, setActiveTab] = useState('auth');

  return (
    <AuthProvider>
      <div className="app-container">
        {/* Header & Navigation */}
        <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

        {/* Feature Modules */}
        <main>
          {activeTab === 'auth' && <AuthCard />}
          {activeTab === 'exams' && <ExamManager />}
          {activeTab === 'take-exam' && <StudentExam />}
          {activeTab === 'results' && <ResultInspector />}
        </main>
      </div>
    </AuthProvider>
  );
}
