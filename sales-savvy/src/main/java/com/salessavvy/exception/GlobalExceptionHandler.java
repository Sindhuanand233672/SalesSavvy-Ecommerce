
package com.salessavvy.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import com.salessavvy.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle request validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Please provide valid input",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    // Handle constraint validation errors
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = createResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );

        return ResponseEntity.badRequest().body(response);
    }

    // Handle database constraint errors
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseErrors(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = createResponse(
                HttpStatus.CONFLICT,
                "Database constraint violation. Check duplicate or invalid data.",
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Handle ResponseStatusException with its original HTTP status
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(
                ex.getStatusCode().value()
        );

        String message = ex.getReason() != null
                ? ex.getReason()
                : status.getReasonPhrase();

        ApiErrorResponse response = createResponse(
                status,
                message,
                request
        );

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    // Handle application and business logic errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeErrors(
            RuntimeException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = createResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Invalid request",
                request
        );

        return ResponseEntity.badRequest().body(response);
    }

    // Handle unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralErrors(
            Exception ex,
            HttpServletRequest request) {

        ApiErrorResponse response = createResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );

        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
        ).body(response);
    }

    // Common response creation method
    private ApiErrorResponse createResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        return new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                null
        );
    }
}
