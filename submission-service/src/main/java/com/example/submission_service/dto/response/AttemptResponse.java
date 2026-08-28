package com.example.submission_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptResponse {

    private Long attemptId;
    private Long examId;
    private Long studentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Double score;
}
