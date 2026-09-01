import { SERVICES, fetchApi } from './apiConfig';

/**
 * Submission Service API Calls (Port 8083)
 */
export const submissionApi = {
  // Start an exam attempt for a student
  startExamAttempt: async (studentId, examId) => {
    return fetchApi(`${SERVICES.SUBMISSION}/api/submissions/start`, 'POST', {
      studentId: parseInt(studentId, 10),
      examId: parseInt(examId, 10)
    }, true);
  },

  // Fetch questions for an active exam attempt
  getQuestionsForAttempt: async (attemptId) => {
    return fetchApi(`${SERVICES.SUBMISSION}/api/submissions/${attemptId}/questions`, 'GET', null, true);
  },

  // Save student's answer for a specific question
  saveAnswer: async (attemptId, questionId, selectedOptionId) => {
    return fetchApi(`${SERVICES.SUBMISSION}/api/submissions/answer`, 'POST', {
      attemptId: parseInt(attemptId, 10),
      questionId: parseInt(questionId, 10),
      selectedOptionId: parseInt(selectedOptionId, 10)
    }, true);
  },

  // Finalize & submit exam attempt (Triggers grading call to Result Service)
  submitExam: async (attemptId) => {
    return fetchApi(`${SERVICES.SUBMISSION}/api/submissions/submit`, 'POST', {
      attemptId: parseInt(attemptId, 10)
    }, true);
  },

  // View past exam attempts for a student
  getStudentAttempts: async (studentId) => {
    return fetchApi(`${SERVICES.SUBMISSION}/api/submissions/student/${studentId}`, 'GET', null, true);
  }
};
