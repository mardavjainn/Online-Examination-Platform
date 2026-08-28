package com.example.submission_service.exception;

public class ExamAlreadySubmittedException extends RuntimeException {
    public ExamAlreadySubmittedException(String message) {
        super(message);
    }
}
