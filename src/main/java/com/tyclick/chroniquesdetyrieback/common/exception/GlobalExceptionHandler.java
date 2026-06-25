package com.tyclick.chroniquesdetyrieback.common.exception;

import com.tyclick.chroniquesdetyrieback.common.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
        * Handle BusinessException and return a structured error response.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(exception.getMessage())
                // With HttpServletRequest, we can get the request URI to include in the error response
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /*
        * Handle MethodArgumentNotValidException and return a structured error response with validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        });
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

}