package com.example.submission_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitExamRequest {

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;
}
