package com.example.result_service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse {
    private Long id;
    private Long attemptId;
    private Long studentId;
    private Long examId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private BigDecimal score;
    private BigDecimal percentage;
    private LocalDateTime createdAt;
}
