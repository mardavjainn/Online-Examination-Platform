package com.example.submission_service.service;

import com.example.submission_service.dto.client.ExamDTO;
import com.example.submission_service.dto.client.QuestionDTO;
import com.example.submission_service.dto.request.StartExamRequest;
import com.example.submission_service.dto.request.SubmitAnswerRequest;
import com.example.submission_service.dto.request.SubmitExamRequest;
import com.example.submission_service.dto.response.AnswerResponse;
import com.example.submission_service.dto.response.AttemptResponse;
import com.example.submission_service.dto.response.SubmitExamResponse;
import com.example.submission_service.entity.Answer;
import com.example.submission_service.entity.Attempt;
import com.example.submission_service.exception.AttemptNotFoundException;
import com.example.submission_service.exception.ExamAlreadySubmittedException;
import com.example.submission_service.exception.ExamNotAvailableException;
import com.example.submission_service.repository.AnswerRepository;
import com.example.submission_service.repository.AttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final AttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final RestTemplate restTemplate;

    @Value("${exam.service.url:http://localhost:8082}")
    private String examServiceUrl;

    @Value("${result.service.url:http://localhost:8084}")
    private String resultServiceUrl;

    public SubmissionService(AttemptRepository attemptRepository,
                             AnswerRepository answerRepository,
                             RestTemplate restTemplate) {
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public AttemptResponse startExam(StartExamRequest request, String authHeader) {
        // Validate exam status with Exam Service
        validateExamAvailable(request.getExamId(), authHeader);

        Optional<Attempt> existingAttempt = attemptRepository.findByStudentIdAndExamIdAndStatus(
                request.getStudentId(), request.getExamId(), "IN_PROGRESS");

        Attempt attempt;
        if (existingAttempt.isPresent()) {
            attempt = existingAttempt.get();
        } else {
            attempt = Attempt.builder()
                    .studentId(request.getStudentId())
                    .examId(request.getExamId())
                    .startTime(LocalDateTime.now())
                    .status("IN_PROGRESS")
                    .build();
            attempt = attemptRepository.save(attempt);
        }

        return mapToAttemptResponse(attempt);
    }

    private void validateExamAvailable(Long examId, String authHeader) {
        String url = examServiceUrl + "/api/exams/" + examId;
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
        }

        try {
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<ExamDTO> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ExamDTO.class);
            ExamDTO exam = response.getBody();
            if (exam == null || !Boolean.TRUE.equals(exam.getIsPublished())) {
                throw new ExamNotAvailableException("Exam with ID " + examId + " is not published or unavailable for attempt.");
            }
        } catch (ExamNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            // If Exam Service is unreachable or returns 404/403, fail gracefully with ExamNotAvailableException
            throw new ExamNotAvailableException("Unable to verify exam availability for ID " + examId + ": " + e.getMessage());
        }
    }

    public List<QuestionDTO> getQuestionsForAttempt(Long attemptId, String authHeader) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AttemptNotFoundException("Attempt not found with ID: " + attemptId));

        String url = examServiceUrl + "/api/questions/exam/" + attempt.getExamId();
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<List<QuestionDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<QuestionDTO>>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }


    @Transactional
    public AnswerResponse saveAnswer(SubmitAnswerRequest request) {
        Attempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new AttemptNotFoundException("Attempt not found with ID: " + request.getAttemptId()));

        if ("SUBMITTED".equalsIgnoreCase(attempt.getStatus()) || "TIMED_OUT".equalsIgnoreCase(attempt.getStatus())) {
            throw new ExamAlreadySubmittedException("Cannot change answers for an exam that has already been submitted.");
        }

        Optional<Answer> existingAnswer = answerRepository.findByAttemptIdAndQuestionId(
                request.getAttemptId(), request.getQuestionId());

        Answer answer;
        if (existingAnswer.isPresent()) {
            answer = existingAnswer.get();
            answer.setSelectedOptionId(request.getSelectedOptionId());
        } else {
            answer = Answer.builder()
                    .attemptId(request.getAttemptId())
                    .questionId(request.getQuestionId())
                    .selectedOptionId(request.getSelectedOptionId())
                    .build();
        }

        answer = answerRepository.save(answer);

        return AnswerResponse.builder()
                .questionId(answer.getQuestionId())
                .selectedOptionId(answer.getSelectedOptionId())
                .isCorrect(answer.getIsCorrect())
                .build();
    }

    @Transactional
    public SubmitExamResponse submitExam(SubmitExamRequest request) {
        Attempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new AttemptNotFoundException("Attempt not found with ID: " + request.getAttemptId()));

        if ("SUBMITTED".equalsIgnoreCase(attempt.getStatus())) {
            throw new ExamAlreadySubmittedException("Exam attempt " + request.getAttemptId() + " has already been submitted.");
        }

        attempt.setStatus("SUBMITTED");
        attempt.setEndTime(LocalDateTime.now());
        attemptRepository.save(attempt);

        // Notify Result Service asynchronously / via REST if needed
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("attemptId", attempt.getId());
            payload.put("studentId", attempt.getStudentId());
            payload.put("examId", attempt.getExamId());
            restTemplate.postForObject(resultServiceUrl + "/results/grade", payload, Object.class);
        } catch (Exception e) {
            // Result service call logged silently if not yet running
        }

        return SubmitExamResponse.builder()
                .attemptId(attempt.getId())
                .message("Exam submitted successfully")
                .status("SUBMITTED")
                .build();
    }

    public List<AttemptResponse> getAttemptsByStudent(Long studentId) {
        List<Attempt> attempts = attemptRepository.findByStudentId(studentId);
        return attempts.stream()
                .map(this::mapToAttemptResponse)
                .collect(Collectors.toList());
    }

    public AttemptResponse getAttemptDetails(Long attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AttemptNotFoundException("Attempt not found with ID: " + attemptId));
        return mapToAttemptResponse(attempt);
    }

    private AttemptResponse mapToAttemptResponse(Attempt attempt) {
        List<Answer> answers = answerRepository.findByAttemptId(attempt.getId());
        return AttemptResponse.builder()
                .attemptId(attempt.getId())
                .examId(attempt.getExamId())
                .studentId(attempt.getStudentId())
                .startTime(attempt.getStartTime())
                .endTime(attempt.getEndTime())
                .status(attempt.getStatus())
                .answeredQuestions(answers.size())
                .build();
    }
}
