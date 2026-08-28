package com.example.exam_service.controller;

import com.example.exam_service.dto.QuestionRequestDTO;
import com.example.exam_service.dto.QuestionResponseDTO;
import com.example.exam_service.dto.QuestionUpdateRequest;
import com.example.exam_service.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/exams/{examId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionResponseDTO> addQuestion(@PathVariable Long examId,
                                                          @Valid @RequestBody QuestionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.addQuestion(examId, request));
    }

    @GetMapping("/exams/{examId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestionsByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExam(examId));
    }

    @GetMapping("/questions/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionResponseDTO> getQuestionById(@PathVariable Long questionId) {
        return ResponseEntity.ok(questionService.getQuestionById(questionId));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionResponseDTO> updateQuestion(@PathVariable Long questionId,
                                                            @Valid @RequestBody QuestionUpdateRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(questionId, request));
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/questions/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuestionResponseDTO>> searchQuestions(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(questionService.searchQuestions(keyword));
    }
}
