package com.example.exam_service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Boolean isPublished;
    private Integer totalQuestions;
    private Integer totalOptions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private String category;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Long version;
    private List<QuestionDTO> questions;
}
