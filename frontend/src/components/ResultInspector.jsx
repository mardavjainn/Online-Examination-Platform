import React, { useState } from 'react';
import { Award, Search, CheckCircle2, AlertCircle, Send } from 'lucide-react';
import { resultApi } from '../api/resultApi';
import { useAuth } from '../context/AuthContext';

export default function ResultInspector() {
  const { isAuthenticated } = useAuth();

  // Search input
  const [queryId, setQueryId] = useState('1');
  const [searchType, setSearchType] = useState('attempt'); // 'attempt' | 'student' | 'exam'
  const [resultsData, setResultsData] = useState(null);

  // Direct Payload Grading state
  const [gradeAttemptId, setGradeAttemptId] = useState('1');
  const [gradeStudentId, setGradeStudentId] = useState('1');
  const [gradeExamId, setGradeExamId] = useState('1');
  const [totalQuestions, setTotalQuestions] = useState(5);
  const [correctAnswers, setCorrectAnswers] = useState(4);

  // Feedback
  const [status, setStatus] = useState({ type: '', message: '' });
  const [loading, setLoading] = useState(false);

  const handleSearch = async () => {
    if (!queryId) return;
    setLoading(true);
    setStatus({ type: '', message: '' });
    setResultsData(null);

    try {
      let data = null;
      if (searchType === 'attempt') {
        data = await resultApi.getResultByAttempt(queryId);
      } else if (searchType === 'student') {
        data = await resultApi.getResultsByStudent(queryId);
      } else if (searchType === 'exam') {
        data = await resultApi.getResultsByExam(queryId);
      }
      setResultsData(data);
      setStatus({ type: 'success', message: 'Result data fetched successfully!' });
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleDirectGrade = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });

    try {
      const data = await resultApi.gradeAttemptDirectly({
        attemptId: parseInt(gradeAttemptId, 10),
        studentId: parseInt(gradeStudentId, 10),
        examId: parseInt(gradeExamId, 10),
        totalQuestions: parseInt(totalQuestions, 10),
        correctAnswers: parseInt(correctAnswers, 10)
      });
      setResultsData(data);
      setStatus({ type: 'success', message: 'Direct grading payload evaluated successfully!' });
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleFetchAllResults = async () => {
    setLoading(true);
    setStatus({ type: '', message: '' });
    setResultsData(null);

    try {
      const data = await resultApi.getAllResults();
      setResultsData(data);
      setStatus({ type: 'success', message: `Fetched ${Array.isArray(data) ? data.length : 0} graded result(s).` });
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">
          <Award size={20} color="#ec4899" /> Result Service (Port 8084)
        </h2>
        <span className="badge badge-pink">Microservice 4</span>
      </div>

      <p className="card-description">
        Inspect evaluation outputs, query graded exam results by Attempt/Student/Exam, or test the calculation engine.
      </p>

      {status.message && (
        <div className={`alert-box alert-${status.type}`}>
          {status.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span>{status.message}</span>
        </div>
      )}

      <div className="grid-2col">
        {/* Left: Query Form */}
        <div>
          <div className="section-box">
            <div className="flex-between">
              <h3 className="section-title"><Search size={16} /> Lookup Exam Results</h3>
              <button onClick={handleFetchAllResults} className="btn btn-secondary btn-sm" disabled={loading}>
                Fetch All Results
              </button>
            </div>
            <div className="form-group" style={{ marginTop: '0.5rem' }}>
              <label className="form-label">Search By</label>
              <select className="form-select" value={searchType} onChange={(e) => setSearchType(e.target.value)}>
                <option value="attempt">Attempt ID (Public/Student)</option>
                <option value="student">Student ID (Requires Student JWT)</option>
                <option value="exam">Exam ID (Requires Teacher/Admin JWT)</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Target ID</label>
              <input className="form-input" value={queryId} onChange={(e) => setQueryId(e.target.value)} required />
            </div>
            <button onClick={handleSearch} className="btn btn-primary" disabled={loading}>
              Fetch Results
            </button>
          </div>

          {/* Direct Grading Tester */}
          <div className="section-box">
            <h3 className="section-title"><Send size={16} /> Direct Grade Payload Tester</h3>
            <form onSubmit={handleDirectGrade} className="form-container">
              <div className="grid-3col">
                <div className="form-group">
                  <label className="form-label">Attempt ID</label>
                  <input className="form-input" value={gradeAttemptId} onChange={(e) => setGradeAttemptId(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Student ID</label>
                  <input className="form-input" value={gradeStudentId} onChange={(e) => setGradeStudentId(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Exam ID</label>
                  <input className="form-input" value={gradeExamId} onChange={(e) => setGradeExamId(e.target.value)} required />
                </div>
              </div>

              <div className="grid-2col">
                <div className="form-group">
                  <label className="form-label">Total Qs</label>
                  <input type="number" className="form-input" value={totalQuestions} onChange={(e) => setTotalQuestions(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Correct Qs</label>
                  <input type="number" className="form-input" value={correctAnswers} onChange={(e) => setCorrectAnswers(e.target.value)} required />
                </div>
              </div>

              <button type="submit" className="btn btn-secondary" disabled={loading}>
                Evaluate Grade Payload
              </button>
            </form>
          </div>
        </div>

        {/* Right: Results Output Display */}
        <div>
          <div className="section-box" style={{ height: '100%' }}>
            <h3 className="section-title">Result Data Output</h3>
            {!resultsData ? (
              <p className="empty-state-text">No result fetched yet. Execute a search or grade evaluation on the left.</p>
            ) : (
              <div className="result-display-box">
                <pre>{JSON.stringify(resultsData, null, 2)}</pre>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
