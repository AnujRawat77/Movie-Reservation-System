package com.movie_reservation.MovieReservationSystem.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "CONFLICT" -> HttpStatus.CONFLICT;
            case "UNAUTHORIZED", "HOLD_NOT_OWNED" -> HttpStatus.FORBIDDEN;
            case "SEAT_ALREADY_HELD", "SEAT_ALREADY_BOOKED" -> HttpStatus.CONFLICT;
            case "HOLD_EXPIRED" -> HttpStatus.GONE;
            default -> HttpStatus.BAD_REQUEST;
        };
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("code", "VALIDATION_ERROR");
        errorDetails.put("message", "Validation failed");
        errorDetails.put("errors", errors);
        errorDetails.put("timestamp", LocalDateTime.now());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("error", errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildErrorResponse("INVALID_PARAMETER", "Invalid parameter value: " + ex.getName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return buildErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String code, String message, HttpStatus status) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("code", code);
        errorDetails.put("message", message);
        errorDetails.put("timestamp", LocalDateTime.now());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("error", errorDetails);

        return ResponseEntity.status(status).body(response);
    }
}
