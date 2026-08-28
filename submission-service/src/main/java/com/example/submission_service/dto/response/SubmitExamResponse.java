package com.example.submission_service.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitExamResponse {

    private Long attemptId;
    private String message;
    private String status;
}
