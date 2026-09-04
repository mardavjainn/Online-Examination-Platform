import React, { useState } from 'react';
import { Play, Send, CheckCircle2, AlertCircle, HelpCircle, Save } from 'lucide-react';
import { submissionApi } from '../api/submissionApi';
import { useAuth } from '../context/AuthContext';

export default function StudentExam() {
  const { isAuthenticated, userId } = useAuth();

  // Inputs
  const [studentId, setStudentId] = useState(userId || '1');
  const [examId, setExamId] = useState('1');

  // Active session
  const [attemptId, setAttemptId] = useState('');
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({}); // { questionId: selectedOptionId }
  const [attemptsList, setAttemptsList] = useState([]);

  // Feedback
  const [status, setStatus] = useState({ type: '', message: '' });
  const [loading, setLoading] = useState(false);

  // Sync logged in userId to studentId input
  React.useEffect(() => {
    if (userId) {
      setStudentId(userId);
    }
  }, [userId]);

  const handleStartExam = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });

    try {
      const res = await submissionApi.startExamAttempt(studentId, examId);
      const newAttemptId = res.attemptId || res.id;
      setAttemptId(newAttemptId.toString());

      // Automatically fetch questions for this attempt
      const fetchedQuestions = await submissionApi.getQuestionsForAttempt(newAttemptId);
      setQuestions(fetchedQuestions || []);
      if (!fetchedQuestions || fetchedQuestions.length === 0) {
        setStatus({
          type: 'warning',
          message: `Attempt #${newAttemptId} started for Exam #${examId}, but no questions were found for this exam! Go to "Exam Creator (8082)" tab to add questions & options to Exam #${examId}.`
        });
      } else {
        setStatus({ type: 'success', message: `Exam attempt started! Attempt ID: #${newAttemptId}. Loaded ${fetchedQuestions.length} question(s).` });
      }
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleSaveAnswer = async (questionId, optionId) => {
    if (!attemptId) return setStatus({ type: 'error', message: 'No active exam attempt found' });

    try {
      await submissionApi.saveAnswer(attemptId, questionId, optionId);
      setAnswers((prev) => ({ ...prev, [questionId]: optionId }));
      setStatus({ type: 'success', message: `Saved answer for Question #${questionId}` });
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    }
  };

  const handleSubmitExam = async () => {
    if (!attemptId) return setStatus({ type: 'error', message: 'No active attempt to submit' });
    if (!window.confirm('Are you sure you want to submit your exam? This will trigger automated grading.')) return;

    setLoading(true);
    try {
      const res = await submissionApi.submitExam(attemptId);
      setStatus({ 
        type: 'success', 
        message: `Exam submitted successfully! Status: ${res.status || 'SUBMITTED'}. Automatically sent to Result Service.` 
      });
      setQuestions([]);
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleFetchAttempts = async () => {
    try {
      const data = await submissionApi.getStudentAttempts(studentId);
      setAttemptsList(Array.isArray(data) ? data : []);
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">
          <Play size={20} color="#f59e0b" /> Submission Service (Port 8083)
        </h2>
        <span className="badge badge-amber">Microservice 3</span>
      </div>

      <p className="card-description">
        Student exam taking workflow: Start attempt, load questions, save responses, and submit for automated grading.
      </p>

      {!isAuthenticated && (
        <div className="alert-box alert-warning">
          <AlertCircle size={18} />
          <span>Please log in via the Auth tab first to take exams.</span>
        </div>
      )}

      {status.message && (
        <div className={`alert-box alert-${status.type}`}>
          {status.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span>{status.message}</span>
        </div>
      )}

      {/* Start Exam Session Form */}
      <div className="section-box">
        <h3 className="section-title"><Play size={16} /> Start New Exam Attempt</h3>
        <form onSubmit={handleStartExam} className="grid-3col">
          <div className="form-group">
            <label className="form-label">Student ID {userId ? `(Auto-filled: #${userId})` : ''}</label>
            <input className="form-input" value={studentId} onChange={(e) => setStudentId(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Exam ID</label>
            <input className="form-input" value={examId} onChange={(e) => setExamId(e.target.value)} required />
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: '0.5rem' }}>
            <button type="submit" className="btn btn-primary" style={{ flex: 1 }} disabled={loading || !isAuthenticated}>
              Start Exam
            </button>
            <button type="button" onClick={handleFetchAttempts} className="btn btn-secondary" disabled={!isAuthenticated}>
              History
            </button>
          </div>
        </form>
      </div>

      {/* Active Exam Questions View */}
      {attemptId && (
        <div className="section-box">
          <div className="flex-between">
            <h3 className="section-title"><HelpCircle size={16} /> Exam Questions (Attempt #{attemptId})</h3>
            <button onClick={handleSubmitExam} className="btn btn-accent" disabled={loading}>
              <Send size={16} /> Finish & Submit Exam
            </button>
          </div>

          {questions.length === 0 ? (
            <div className="alert-box alert-warning" style={{ marginTop: '1rem' }}>
              <AlertCircle size={18} />
              <div>
                <strong>No questions loaded for Exam #{examId}.</strong>
                <p style={{ marginTop: '0.25rem', fontSize: '0.85rem' }}>
                  Make sure you have published Exam #{examId} and added questions & options in the <strong>"Exam Creator (8082)"</strong> tab!
                </p>
              </div>
            </div>
          ) : (
            <div className="questions-container" style={{ marginTop: '1rem' }}>
              {questions.map((q, idx) => (
                <div key={q.id || idx} className="question-card">
                  <p className="question-text">
                    <strong>Q{idx + 1}. {q.questionText || q.content || q.text}</strong>
                  </p>
                  <div className="options-list">
                    {(q.options || []).map((opt) => (
                      <label key={opt.id} className="option-item">
                        <input
                          type="radio"
                          name={`q_${q.id}`}
                          checked={answers[q.id] === opt.id}
                          onChange={() => handleSaveAnswer(q.id, opt.id)}
                        />
                        <span>{opt.optionText || opt.text}</span>
                      </label>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Student Attempt History */}
      {attemptsList.length > 0 && (
        <div className="section-box" style={{ marginTop: '1rem' }}>
          <h3 className="section-title">Past Attempts for Student #{studentId}</h3>
          <div className="item-list">
            {attemptsList.map((att) => (
              <div key={att.id} className="item-card flex-between">
                <div>
                  <strong>Attempt #{att.id}</strong> (Exam #{att.examId})
                  <div className="item-subtext">Status: {att.status || 'SUBMITTED'}</div>
                </div>
                <span className="badge">Student #{att.studentId}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
