package com.fined.mentor.quiz.exception;


public class QuizValidationException extends RuntimeException {
    public QuizValidationException(String message) { super(message); }
    public QuizValidationException(String message, Throwable cause) { super(message, cause); }
}
