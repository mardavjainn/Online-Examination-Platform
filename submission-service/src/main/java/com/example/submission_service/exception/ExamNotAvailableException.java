package com.example.submission_service.exception;

public class ExamNotAvailableException extends RuntimeException {
    public ExamNotAvailableException(String message) {
        super(message);
    }
}
