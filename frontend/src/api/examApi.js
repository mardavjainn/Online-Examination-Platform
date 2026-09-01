import { SERVICES, fetchApi } from './apiConfig';

/**
 * Exam Service API Calls (Port 8082)
 */
export const examApi = {
  // Create a new exam
  createExam: async (examData) => {
    return fetchApi(`${SERVICES.EXAM}/api/exams`, 'POST', examData, true);
  },

  // Get all exams (paginated response)
  getAllExams: async () => {
    return fetchApi(`${SERVICES.EXAM}/api/exams`, 'GET', null, true);
  },

  // Get all published exams
  getPublishedExams: async () => {
    return fetchApi(`${SERVICES.EXAM}/api/exams/published`, 'GET', null, true);
  },

  // Get specific exam details by ID
  getExamById: async (examId) => {
    return fetchApi(`${SERVICES.EXAM}/api/exams/${examId}`, 'GET', null, true);
  },

  // Delete an exam by ID
  deleteExam: async (examId) => {
    return fetchApi(`${SERVICES.EXAM}/api/exams/${examId}`, 'DELETE', null, true);
  },

  // Add a question to an exam
  addQuestion: async (examId, questionData) => {
    return fetchApi(`${SERVICES.EXAM}/api/exams/${examId}/questions`, 'POST', questionData, true);
  },

  // Add an option to a question
  addOption: async (questionId, optionData) => {
    return fetchApi(`${SERVICES.EXAM}/api/questions/${questionId}/options`, 'POST', optionData, true);
  }
};
