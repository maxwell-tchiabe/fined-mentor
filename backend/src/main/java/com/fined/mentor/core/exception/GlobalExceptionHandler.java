package com.fined.mentor.core.exception;

import com.fined.mentor.auth.exception.InvalidTokenException;
import com.fined.mentor.auth.exception.UserAlreadyActivatedException;
import com.fined.mentor.auth.exception.UserNotFoundException;
import com.fined.mentor.chat.exception.*;
import com.fined.mentor.core.dto.ApiResponse;
import com.fined.mentor.quiz.exception.QuizException;
import com.fined.mentor.quiz.exception.QuizGenerationException;
import com.fined.mentor.quiz.exception.QuizNotFoundException;
import com.fined.mentor.quiz.exception.QuizStateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("Validation error: {}", errorMessage);
        return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
    }

    @ExceptionHandler({ QuizGenerationException.class, ChatException.class })
    public ResponseEntity<ApiResponse<Object>> handleBusinessExceptions(RuntimeException ex) {
        log.error("Business exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("An unexpected error occurred"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(NoResourceFoundException ex) {
        // This occurs when a request matches a static resource path but no resource is
        // found.
        log.warn("Static resource not found: {}", ex.getMessage());
        String message = "Resource not found. Did you mean to call an API endpoint (e.g. /api/chat/sessions)?";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message));
    }

    @ExceptionHandler({ QuizNotFoundException.class, QuizStateNotFoundException.class })
    public ResponseEntity<ApiResponse<Object>> handleQuizNotFoundExceptions(RuntimeException ex) {
        log.warn("Quiz not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(QuizException.class)
    public ResponseEntity<ApiResponse<Object>> handleQuizExceptions(RuntimeException ex) {
        log.error("Quiz error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({ ChatSessionNotFoundException.class, ChatMessageNotFoundException.class })
    public ResponseEntity<ApiResponse<Object>> handleChatNotFoundExceptions(RuntimeException ex) {
        log.warn("Chat resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({ ChatSessionException.class, ChatMessageException.class })
    public ResponseEntity<ApiResponse<Object>> handleChatExceptions(RuntimeException ex) {
        log.error("Chat error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("Invalid token access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyActivatedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyActivatedException(UserAlreadyActivatedException ex) {
        log.warn("User already activated: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
