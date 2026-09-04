import React, { useState, useEffect } from 'react';
import { FileText, Plus, Trash2, List, CheckCircle2, AlertCircle, HelpCircle } from 'lucide-react';
import { examApi } from '../api/examApi';
import { useAuth } from '../context/AuthContext';

export default function ExamManager() {
  const { isAuthenticated, userId, userRole } = useAuth();

  // State for exam list & forms
  const [exams, setExams] = useState([]);
  const [selectedExamId, setSelectedExamId] = useState('');
  const [selectedQuestionId, setSelectedQuestionId] = useState('');

  // Form fields: Create Exam
  const [title, setTitle] = useState('Java & Spring Boot Core');
  const [description, setDescription] = useState('Comprehensive microservices exam');
  const [durationMinutes, setDurationMinutes] = useState(30);

  // Form fields: Add Question
  const [questionContent, setQuestionContent] = useState('What is Dependency Injection in Spring?');
  const [marks, setMarks] = useState(1);

  // Form fields: Add Option
  const [optionText, setOptionText] = useState('Design pattern used to pass dependencies into objects');
  const [isCorrect, setIsCorrect] = useState(true);

  // Feedback
  const [status, setStatus] = useState({ type: '', message: '' });
  const [loading, setLoading] = useState(false);

  const fetchExams = async () => {
    try {
      const data = await examApi.getPublishedExams();
      setExams(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      console.log('Failed to fetch exams:', err.message);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchExams();
    }
  }, [isAuthenticated]);

  const handleCreateExam = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });

    try {
      const res = await examApi.createExam({ 
        title, 
        description, 
        duration: parseInt(durationMinutes, 10),
        durationMinutes: parseInt(durationMinutes, 10),
        createdBy: parseInt(userId, 10) || 1,
        isPublished: true 
      });
      setStatus({ type: 'success', message: `Exam created & published! Exam ID: #${res.id}` });
      setSelectedExamId(res.id.toString());
      fetchExams();
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleAddQuestion = async (e) => {
    e.preventDefault();
    if (!selectedExamId) return setStatus({ type: 'error', message: 'Please select or create an Exam first' });

    setLoading(true);
    try {
      const res = await examApi.addQuestion(selectedExamId, {
        questionText: questionContent,
        content: questionContent,
        marks: parseInt(marks, 10)
      });
      setStatus({ type: 'success', message: `Question added successfully! Question ID: ${res.id}` });
      setSelectedQuestionId(res.id.toString());
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleAddOption = async (e) => {
    e.preventDefault();
    if (!selectedQuestionId) return setStatus({ type: 'error', message: 'Please select or create a Question first' });

    setLoading(true);
    try {
      await examApi.addOption(selectedQuestionId, { optionText, isCorrect });
      setStatus({ type: 'success', message: 'Option added successfully!' });
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteExam = async (id) => {
    if (!window.confirm(`Delete Exam #${id}?`)) return;
    try {
      await examApi.deleteExam(id);
      setStatus({ type: 'success', message: `Exam #${id} deleted.` });
      fetchExams();
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">
          <FileText size={20} color="#10b981" /> Exam Service (Port 8082)
        </h2>
        <span className="badge badge-emerald">Microservice 2</span>
      </div>

      <p className="card-description">
        Manage exam templates, configure questions, and set options. (Requires TEACHER or ADMIN role JWT)
      </p>

      {!isAuthenticated && (
        <div className="alert-box alert-warning">
          <AlertCircle size={18} />
          <span>Please log in via the Auth tab first to create or manage exams.</span>
        </div>
      )}

      {isAuthenticated && userRole === 'STUDENT' && (
        <div className="alert-box alert-warning">
          <AlertCircle size={18} />
          <span>You are logged in as a STUDENT. Creating or modifying exams requires a TEACHER or ADMIN account.</span>
        </div>
      )}

      {status.message && (
        <div className={`alert-box alert-${status.type}`}>
          {status.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span>{status.message}</span>
        </div>
      )}

      <div className="grid-2col">
        {/* Left Column: Form Controls */}
        <div>
          {/* Section 1: Create Exam */}
          <div className="section-box">
            <h3 className="section-title"><Plus size={16} /> 1. Create New Exam</h3>
            <form onSubmit={handleCreateExam} className="form-container">
              <div className="form-group">
                <label className="form-label">Exam Title</label>
                <input className="form-input" value={title} onChange={(e) => setTitle(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Description</label>
                <input className="form-input" value={description} onChange={(e) => setDescription(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Duration (Minutes)</label>
                <input type="number" className="form-input" value={durationMinutes} onChange={(e) => setDurationMinutes(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary" disabled={loading || !isAuthenticated}>
                Create & Publish Exam
              </button>
            </form>
          </div>

          {/* Section 2: Add Question */}
          <div className="section-box">
            <h3 className="section-title"><HelpCircle size={16} /> 2. Add Question to Exam #{selectedExamId || 'None'}</h3>
            <form onSubmit={handleAddQuestion} className="form-container">
              <div className="form-group">
                <label className="form-label">Target Exam ID</label>
                <input className="form-input" value={selectedExamId} onChange={(e) => setSelectedExamId(e.target.value)} placeholder="e.g. 1" required />
              </div>
              <div className="form-group">
                <label className="form-label">Question Text</label>
                <input className="form-input" value={questionContent} onChange={(e) => setQuestionContent(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-secondary" disabled={loading || !isAuthenticated}>
                Add Question
              </button>
            </form>
          </div>

          {/* Section 3: Add Option */}
          <div className="section-box">
            <h3 className="section-title"><CheckCircle2 size={16} /> 3. Add Option to Question #{selectedQuestionId || 'None'}</h3>
            <form onSubmit={handleAddOption} className="form-container">
              <div className="form-group">
                <label className="form-label">Target Question ID</label>
                <input className="form-input" value={selectedQuestionId} onChange={(e) => setSelectedQuestionId(e.target.value)} placeholder="e.g. 1" required />
              </div>
              <div className="form-group">
                <label className="form-label">Option Text</label>
                <input className="form-input" value={optionText} onChange={(e) => setOptionText(e.target.value)} required />
              </div>
              <div className="form-checkbox">
                <input type="checkbox" id="isCorrectCheck" checked={isCorrect} onChange={(e) => setIsCorrect(e.target.checked)} />
                <label htmlFor="isCorrectCheck">Mark as Correct Answer</label>
              </div>
              <button type="submit" className="btn btn-secondary" disabled={loading || !isAuthenticated}>
                Add Option
              </button>
            </form>
          </div>
        </div>

        {/* Right Column: Existing Exams List */}
        <div>
          <div className="section-box" style={{ height: '100%' }}>
            <div className="flex-between">
              <h3 className="section-title"><List size={16} /> Available Exams</h3>
              <button onClick={fetchExams} className="btn btn-secondary btn-sm">Refresh</button>
            </div>

            {exams.length === 0 ? (
              <p className="empty-state-text">No exams found. Use the form on the left to create an exam.</p>
            ) : (
              <div className="item-list">
                {exams.map((exam) => (
                  <div key={exam.id} className="item-card">
                    <div className="flex-between">
                      <strong className="item-title">#{exam.id} - {exam.title}</strong>
                      <button onClick={() => handleDeleteExam(exam.id)} className="btn-icon-danger" title="Delete Exam">
                        <Trash2 size={14} />
                      </button>
                    </div>
                    <p className="item-subtext">{exam.description}</p>
                    <div className="item-meta">
                      <span>⏱ {exam.durationMinutes} mins</span>
                      <button 
                        onClick={() => setSelectedExamId(exam.id.toString())} 
                        className="btn-link"
                      >
                        Select for Questions
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
