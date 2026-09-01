package com.example.result_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultRequest {

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @PositiveOrZero(message = "Total questions cannot be negative")
    private Integer totalQuestions;

    @PositiveOrZero(message = "Correct answers cannot be negative")
    private Integer correctAnswers;

    @Valid
    private List<AnswerResult> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerResult {
        @NotNull(message = "Answer correctness is required")
        private Boolean correct;
    }
}
