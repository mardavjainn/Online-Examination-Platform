package com.example.result_service.service;

import com.example.result_service.dto.request.ResultRequest;
import com.example.result_service.dto.response.ResultDetailResponse;
import com.example.result_service.dto.response.ResultResponse;
import com.example.result_service.entity.Result;
import com.example.result_service.exception.ResultNotFoundException;
import com.example.result_service.repository.ResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ResultService {

    private static final Logger log = LoggerFactory.getLogger(ResultService.class);

    private final ResultRepository resultRepository;

    public ResultService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Transactional
    public ResultResponse grade(ResultRequest request) {
        log.info("Received grading request for attemptId {}, studentId {}, examId {}: totalQuestions={}, correctAnswers={}",
                request.getAttemptId(), request.getStudentId(), request.getExamId(),
                request.getTotalQuestions(), request.getCorrectAnswers());

        int total = request.getAnswers() != null && !request.getAnswers().isEmpty()
                ? request.getAnswers().size() : valueOrZero(request.getTotalQuestions());
        int correct = request.getAnswers() != null && !request.getAnswers().isEmpty()
                ? (int) request.getAnswers().stream().filter(answer -> Boolean.TRUE.equals(answer.getCorrect())).count()
                : valueOrZero(request.getCorrectAnswers());

        if (total <= 0) {
            log.error("Invalid grading request for attemptId {}: total questions is {}", request.getAttemptId(), total);
            throw new IllegalArgumentException("At least one question is required");
        }
        if (correct > total) {
            log.error("Invalid grading request for attemptId {}: correct ({}) > total ({})", request.getAttemptId(), correct, total);
            throw new IllegalArgumentException("Correct answers cannot exceed total questions");
        }

        Result result = resultRepository.findByAttemptId(request.getAttemptId()).orElseGet(Result::new);
        result.setAttemptId(request.getAttemptId());
        result.setStudentId(request.getStudentId());
        result.setExamId(request.getExamId());
        result.setTotalQuestions(total);
        result.setCorrectAnswers(correct);
        result.setScore(BigDecimal.valueOf(correct).setScale(2));
        result.setPercentage(BigDecimal.valueOf(correct * 100.0 / total).setScale(2, RoundingMode.HALF_UP));

        Result savedResult = resultRepository.save(result);
        log.info("Successfully persisted Result entity ID {} for attemptId {}: score={}, percentage={}%",
                savedResult.getId(), savedResult.getAttemptId(), savedResult.getScore(), savedResult.getPercentage());
        return toResponse(savedResult);
    }

    @Transactional(readOnly = true)
    public ResultResponse getById(Long id) {
        return resultRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResultNotFoundException("Result not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public ResultResponse getByAttemptId(Long attemptId) {
        return resultRepository.findByAttemptId(attemptId).map(this::toResponse)
                .orElseThrow(() -> new ResultNotFoundException("Result not found for attempt ID: " + attemptId));
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> getByStudentId(Long studentId) {
        return resultRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ResultDetailResponse> getByExamId(Long examId) {
        return resultRepository.findByExamIdOrderByCreatedAtDesc(examId).stream().map(this::toDetailResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> getAllResults() {
        return resultRepository.findAll().stream().map(this::toResponse).toList();
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private ResultResponse toResponse(Result result) {
        return ResultResponse.builder().id(result.getId()).attemptId(result.getAttemptId())
                .studentId(result.getStudentId()).examId(result.getExamId())
                .totalQuestions(result.getTotalQuestions()).correctAnswers(result.getCorrectAnswers())
                .score(result.getScore()).percentage(result.getPercentage()).createdAt(result.getCreatedAt()).build();
    }

    private ResultDetailResponse toDetailResponse(Result result) {
        return ResultDetailResponse.builder().resultId(result.getId()).attemptId(result.getAttemptId())
                .studentId(result.getStudentId()).examId(result.getExamId())
                .totalQuestions(result.getTotalQuestions()).correctAnswers(result.getCorrectAnswers())
                .score(result.getScore()).percentage(result.getPercentage()).status("GRADED")
                .createdAt(result.getCreatedAt()).build();
    }
}
