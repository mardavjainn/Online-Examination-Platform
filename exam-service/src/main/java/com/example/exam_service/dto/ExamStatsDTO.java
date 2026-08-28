package com.example.exam_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamStatsDTO {
    private long totalExams;
    private long publishedExams;
    private long draftExams;
    private long totalQuestions;
    private long totalOptions;
}
