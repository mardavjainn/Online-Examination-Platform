package com.example.exam_service.service;

import com.example.exam_service.entity.Exam;
import com.example.exam_service.entity.Question;
import com.example.exam_service.exception.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class ExamValidationService {

    public void validateExamBeforePublish(Exam exam) {
        if (exam == null) {
            throw new ValidationException("Exam is required");
        }

        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty()) {
            throw new ValidationException("Exam title cannot be empty");
        }

        if (exam.getQuestions() == null || exam.getQuestions().isEmpty()) {
            throw new ValidationException("Exam must have at least 1 question before publishing");
        }

        for (Question question : exam.getQuestions()) {
            if (question == null || question.getIsDeleted() != null && question.getIsDeleted()) {
                continue;
            }
            if (question.getOptions() == null || question.getOptions().isEmpty()) {
                throw new ValidationException("Each question must have at least 2 options");
            }
            if (question.getOptions().size() < 2) {
                throw new ValidationException("Each question must have at least 2 options");
            }

            long correctCount = question.getOptions().stream()
                    .filter(option -> option != null && Boolean.TRUE.equals(option.getIsCorrect()) && Boolean.FALSE.equals(option.getIsDeleted()))
                    .count();

            if (correctCount != 1) {
                throw new ValidationException("Each question must have exactly 1 correct answer");
            }
        }
    }
}
