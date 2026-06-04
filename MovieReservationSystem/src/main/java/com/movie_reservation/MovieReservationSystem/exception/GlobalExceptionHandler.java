package com.movie_reservation.MovieReservationSystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        HttpStatus status;
        switch (ex.getErrorCode()) {
            case "CONFLICT": status = HttpStatus.CONFLICT; break;
            case "UNAUTHORIZED": status = HttpStatus.FORBIDDEN; break;
            case "HOLD_NOT_OWNED": status = HttpStatus.FORBIDDEN; break;
            case "SEAT_ALREADY_HELD": status = HttpStatus.CONFLICT; break;
            case "SEAT_ALREADY_BOOKED": status = HttpStatus.CONFLICT; break;
            case "HOLD_EXPIRED": status = HttpStatus.GONE; break;
            default: status = HttpStatus.BAD_REQUEST;
        }
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildErrorResponse("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildErrorResponse("INVALID_PARAMETER", "Invalid parameter value: " + ex.getName(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse("INTERNAL_ERROR", "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String code, String message, HttpStatus status) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", code);
        errorDetails.put("message", message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", errorDetails);

        return ResponseEntity.status(status).body(response);
    }
}
