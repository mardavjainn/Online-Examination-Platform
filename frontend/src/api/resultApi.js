import { SERVICES, fetchApi } from './apiConfig';

/**
 * Result Service API Calls (Port 8084)
 */
export const resultApi = {
  // Test direct grade payload endpoint
  gradeAttemptDirectly: async (gradeData) => {
    return fetchApi(`${SERVICES.RESULT}/results/grade`, 'POST', gradeData);
  },

  // Retrieve result by attempt ID
  getResultByAttempt: async (attemptId) => {
    return fetchApi(`${SERVICES.RESULT}/results/attempt/${attemptId}`, 'GET', null, true);
  },

  // Retrieve all results for a student ID (Requires STUDENT role JWT)
  getResultsByStudent: async (studentId) => {
    return fetchApi(`${SERVICES.RESULT}/results/student/${studentId}`, 'GET', null, true);
  },

  // Retrieve all results stored in Result Service
  getAllResults: async () => {
    return fetchApi(`${SERVICES.RESULT}/results`, 'GET', null, true);
  },

  // Retrieve all results for an exam ID (Requires TEACHER or ADMIN role JWT)
  getResultsByExam: async (examId) => {
    return fetchApi(`${SERVICES.RESULT}/results/exam/${examId}`, 'GET', null, true);
  }
};
