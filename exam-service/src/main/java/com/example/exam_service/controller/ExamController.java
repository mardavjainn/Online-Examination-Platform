package com.example.exam_service.controller;

import com.example.exam_service.dto.*;
import com.example.exam_service.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/exams", "/exams"})
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ExamResponseDTO> createExam(@Valid @RequestBody ExamRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.createExam(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ExamResponseDTO>> getAllExams(Pageable pageable) {
        return ResponseEntity.ok(examService.getAllExams(pageable));
    }

    @GetMapping("/published")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExamResponseDTO>> getPublishedExams() {
        return ResponseEntity.ok(examService.getPublishedExams());
    }

    @GetMapping("/drafts")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<ExamResponseDTO>> getDraftExams() {
        return ResponseEntity.ok(examService.getDraftExams());
    }

    @GetMapping("/{examId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExamResponseDTO> getExamById(@PathVariable Long examId) {
        return ResponseEntity.ok(examService.getExamById(examId));
    }

    @PutMapping("/{examId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ExamResponseDTO> updateExam(@PathVariable Long examId,
            @Valid @RequestBody ExamUpdateRequest request) {
        return ResponseEntity.ok(examService.updateExam(examId, request));
    }

    @PutMapping("/{examId}/publish")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> publishExam(@PathVariable Long examId) {
        examService.publishExam(examId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{examId}/unpublish")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> unpublishExam(@PathVariable Long examId) {
        examService.unpublishExam(examId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{examId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable Long examId) {
        examService.deleteExam(examId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExamResponseDTO>> searchExams(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(examService.searchExams(keyword));
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExamStatsDTO> getExamStats() {
        return ResponseEntity.ok(examService.getExamStats());
    }
}
