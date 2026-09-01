package com.example.result_service.controller;

import com.example.result_service.dto.request.ResultRequest;
import com.example.result_service.dto.response.ResultDetailResponse;
import com.example.result_service.dto.response.ResultResponse;
import com.example.result_service.service.ResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/results", "/api/results"})
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @PostMapping("/grade")
    public ResponseEntity<ResultResponse> grade(@Valid @RequestBody ResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resultService.grade(request));
    }

    @GetMapping
    public ResponseEntity<List<ResultResponse>> getAll() {
        return ResponseEntity.ok(resultService.getAllResults());
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<ResultResponse> getById(@PathVariable Long resultId) {
        return ResponseEntity.ok(resultService.getById(resultId));
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<ResultResponse> getByAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(resultService.getByAttemptId(attemptId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ResultResponse>> getByStudent(@PathVariable Long studentId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Long authenticatedStudentId)
                || !authenticatedStudentId.equals(studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(resultService.getByStudentId(studentId));
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<ResultDetailResponse>> getByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(resultService.getByExamId(examId));
    }
}
