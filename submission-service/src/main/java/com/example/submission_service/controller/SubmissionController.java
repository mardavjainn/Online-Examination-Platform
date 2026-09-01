package com.example.submission_service.controller;

import com.example.submission_service.dto.client.QuestionDTO;
import com.example.submission_service.dto.request.StartExamRequest;
import com.example.submission_service.dto.request.SubmitAnswerRequest;
import com.example.submission_service.dto.request.SubmitExamRequest;
import com.example.submission_service.dto.response.AnswerResponse;
import com.example.submission_service.dto.response.AttemptResponse;
import com.example.submission_service.dto.response.SubmitExamResponse;
import com.example.submission_service.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/submissions", "/submissions"})
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/start")
    public ResponseEntity<AttemptResponse> startExam(@Valid @RequestBody StartExamRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        AttemptResponse response = submissionService.startExam(request, authHeader);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{attemptId}/questions")
    public ResponseEntity<List<QuestionDTO>> getQuestions(@PathVariable Long attemptId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        List<QuestionDTO> questions = submissionService.getQuestionsForAttempt(attemptId, authHeader);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/answer")
    public ResponseEntity<AnswerResponse> saveAnswer(@Valid @RequestBody SubmitAnswerRequest request) {
        AnswerResponse response = submissionService.saveAnswer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmitExamResponse> submitExam(@Valid @RequestBody SubmitExamRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        SubmitExamResponse response = submissionService.submitExam(request, authHeader);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByStudent(@PathVariable Long studentId) {
        List<AttemptResponse> attempts = submissionService.getAttemptsByStudent(studentId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<AttemptResponse> getAttemptDetails(@PathVariable Long attemptId) {
        AttemptResponse response = submissionService.getAttemptDetails(attemptId);
        return ResponseEntity.ok(response);
    }
}
