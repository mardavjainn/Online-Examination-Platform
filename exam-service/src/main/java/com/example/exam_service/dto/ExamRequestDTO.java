package com.example.exam_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Long createdBy; // Teacher ID from Auth Service

    private String category;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer duration; // in minutes

    private Integer durationMinutes;

    private Boolean isPublished;

    public Integer getDuration() {
        return duration != null ? duration : durationMinutes;
    }
}
