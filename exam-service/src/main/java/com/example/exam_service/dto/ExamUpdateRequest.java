package com.example.exam_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamUpdateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String category;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Boolean isPublished;
}
