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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

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

    private String resolveAuthHeader(String authHeader) {
        if (authHeader != null && !authHeader.isBlank()) {
            return authHeader;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String header = attributes.getRequest().getHeader("Authorization");
            if (header != null && !header.isBlank()) {
                return header;
            }
        }
        return null;
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
        String resolvedAuth = resolveAuthHeader(authHeader);
        String url = examServiceUrl + "/api/exams/" + examId;
        HttpHeaders headers = new HttpHeaders();
        if (resolvedAuth != null && !resolvedAuth.isBlank()) {
            headers.set("Authorization", resolvedAuth);
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

        String resolvedAuth = resolveAuthHeader(authHeader);
        String url = examServiceUrl + "/api/questions/exam/" + attempt.getExamId();
        HttpHeaders headers = new HttpHeaders();
        if (resolvedAuth != null && !resolvedAuth.isBlank()) {
            headers.set("Authorization", resolvedAuth);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<List<QuestionDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<QuestionDTO>>() {}
            );
            List<QuestionDTO> questions = response.getBody() != null ? response.getBody() : List.of();
            log.info("Fetched {} questions from exam-service for examId {}", questions.size(), attempt.getExamId());
            return questions;
        } catch (Exception e) {
            log.error("Failed to fetch questions from exam-service for attemptId {} (examId {}): {}",
                    attemptId, attempt.getExamId(), e.getMessage());
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
        return submitExam(request, null);
    }

    @Transactional
    public SubmitExamResponse submitExam(SubmitExamRequest request, String authHeader) {
        Attempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new AttemptNotFoundException("Attempt not found with ID: " + request.getAttemptId()));

        if ("SUBMITTED".equalsIgnoreCase(attempt.getStatus())) {
            throw new ExamAlreadySubmittedException("Exam attempt " + request.getAttemptId() + " has already been submitted.");
        }

        attempt.setStatus("SUBMITTED");
        attempt.setEndTime(LocalDateTime.now());
        attemptRepository.save(attempt);

        // Notify Result Service asynchronously / via REST
        try {
            String resolvedAuth = resolveAuthHeader(authHeader);
            List<Answer> answers = answerRepository.findByAttemptId(attempt.getId());
            List<QuestionDTO> questions = getQuestionsForAttempt(attempt.getId(), resolvedAuth);

            int correctAnswers = (int) answers.stream()
                .filter(answer -> questions.stream().anyMatch(question -> question.getId().equals(answer.getQuestionId())
                    && question.getOptions() != null && question.getOptions().stream()
                    .anyMatch(option -> option.getId().equals(answer.getSelectedOptionId())
                        && Boolean.TRUE.equals(option.getIsCorrect()))))
                .count();

            int totalQuestions = questions.size();
            if (totalQuestions == 0 && !answers.isEmpty()) {
                totalQuestions = (int) answers.stream().map(Answer::getQuestionId).distinct().count();
                log.warn("Questions list was empty from exam-service. Using answered questions count ({}) for attemptId {}",
                        totalQuestions, attempt.getId());
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("attemptId", attempt.getId());
            payload.put("studentId", attempt.getStudentId());
            payload.put("examId", attempt.getExamId());
            payload.put("totalQuestions", totalQuestions);
            payload.put("correctAnswers", correctAnswers);

            log.info("Sending grading payload to result-service at {}/results/grade for attemptId {}: total={}, correct={}",
                    resultServiceUrl, attempt.getId(), totalQuestions, correctAnswers);

            restTemplate.postForObject(resultServiceUrl + "/results/grade", payload, Object.class);
            log.info("Successfully posted result to result-service for attemptId {}", attempt.getId());
        } catch (Exception e) {
            log.error("Failed to post result to result-service for attemptId {}: {}", attempt.getId(), e.getMessage(), e);
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
