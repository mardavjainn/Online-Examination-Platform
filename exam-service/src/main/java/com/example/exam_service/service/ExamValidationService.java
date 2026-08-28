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
        validateExamBeforePublish(exam, exam.getQuestions());
    }

    public void validateExamBeforePublish(Exam exam, java.util.List<Question> questions) {
        if (exam == null) {
            throw new ValidationException("Exam is required");
        }

        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty()) {
            throw new ValidationException("Exam title cannot be empty");
        }

        if (questions == null || questions.isEmpty()) {
            throw new ValidationException("Exam must have at least 1 question before publishing");
        }

        long activeQuestionCount = questions.stream()
                .filter(q -> q != null && !Boolean.TRUE.equals(q.getIsDeleted()))
                .count();

        if (activeQuestionCount == 0) {
            throw new ValidationException("Exam must have at least 1 question before publishing");
        }

        for (Question question : questions) {
            if (question == null || Boolean.TRUE.equals(question.getIsDeleted())) {
                continue;
            }
            if (question.getOptions() == null || question.getOptions().isEmpty()) {
                throw new ValidationException("Each question must have at least 2 options");
            }
            
            java.util.List<com.example.exam_service.entity.Option> activeOptions = question.getOptions().stream()
                    .filter(option -> option != null && !Boolean.TRUE.equals(option.getIsDeleted()))
                    .collect(java.util.stream.Collectors.toList());

            if (activeOptions.size() < 2) {
                throw new ValidationException("Each question must have at least 2 options");
            }

            long correctCount = activeOptions.stream()
                    .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                    .count();

            if (correctCount != 1) {
                throw new ValidationException("Each question must have exactly 1 correct answer");
            }
        }
    }
}
