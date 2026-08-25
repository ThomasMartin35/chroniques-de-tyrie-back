package com.tyclick.chroniquesdetyrieback.common.exception;

import com.tyclick.chroniquesdetyrieback.common.dto.response.ApiErrorResponse;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.AvatarFileTooLargeException;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.UnsupportedAvatarFormatException;
import com.tyclick.chroniquesdetyrieback.media.delivery.exception.PublicMediaNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle BusinessException and return a structured error response.
     *
     * @param exception The exception thrown when a business rule is violated.
     * @param request   The HttpServletRequest object to get the request URI for the error response.
     * @return A ResponseEntity containing the ApiErrorResponse with error details.
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

    /**
     * Handle MethodArgumentNotValidException and return a structured error response with validation errors.
     *
     * @param exception The exception thrown when validation fails for method arguments.
     * @param request   The HttpServletRequest object to get the request URI for the error response.
     * @return A ResponseEntity containing the ApiErrorResponse with validation error details.
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

    /**
     * Handle AuthenticationFailedException and return a structured error response indicating authentication failure.
     *
     * @param exception The exception thrown when authentication fails (e.g., invalid credentials).
     * @param request   The HttpServletRequest object to get the request URI for the error response.
     * @return A ResponseEntity containing the ApiErrorResponse with authentication failure details.
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationFailedException(
            AuthenticationFailedException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle AvatarFileTooLargeException and MaxUploadSizeExceededException and return a structured error response indicating that the uploaded avatar file exceeds the maximum allowed size.
     * @param exception The exception thrown when the uploaded avatar file exceeds the maximum allowed size.
     * @param request The HttpServletRequest object to get the request URI for the error response.
     * @return A ResponseEntity containing the ApiErrorResponse with details about the file size limit violation.
     */
    @ExceptionHandler({
            AvatarFileTooLargeException.class,
            MaxUploadSizeExceededException.class
    })
    public ResponseEntity<ApiErrorResponse> handleAvatarFileTooLarge(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase())
                .message("Avatar file exceeds the maximum allowed size")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(errorResponse);
    }

    /**
     * Handle UnsupportedAvatarFormatException and return a structured error response indicating that the uploaded avatar file format is not supported.
     * @param exception The exception thrown when the uploaded avatar file format is not supported.
     * @param request The HttpServletRequest object to get the request URI for the error response.
     * @return A ResponseEntity containing the ApiErrorResponse with details about the unsupported avatar format.
     */
    @ExceptionHandler(UnsupportedAvatarFormatException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedAvatarFormat(
            UnsupportedAvatarFormatException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(errorResponse);
    }

    /**
     * Handle PublicMediaNotFoundException and return a structured error response indicating that the requested public media was not found.
     * @param exception The exception thrown when the requested public media is not found.
     * @param request The HttpServletRequest object to get the request URI for the error response.
     * @return A ResponseEntity containing the ApiErrorResponse with details about the public media not found error.
     */
    @ExceptionHandler(PublicMediaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePublicMediaNotFound(
            PublicMediaNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

}