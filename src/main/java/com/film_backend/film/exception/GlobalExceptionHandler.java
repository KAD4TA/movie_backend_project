package com.film_backend.film.exception;

import static java.util.Map.entry;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import com.film_backend.film.dtos.response.ApiErrorResponse;

import io.jsonwebtoken.MalformedJwtException;

/**
 * Global exception handler for managing application-wide exceptions and returning standardized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<String, String> ERROR_MESSAGE_MAPPINGS = Map.ofEntries(
        entry("user ID not found", "Invalid refresh token: User ID is missing."),
        entry("Unsupported file type", "Unsupported file type: Only JPG and PNG are supported."),
        entry("Image file is too large", "Image file is too large: Maximum size is 10 MB."),
        entry("Image source cannot be null", "Image source cannot be null or empty."),
        entry("Base64 images are not supported", "Base64 images are not supported. Provide a URL or local file path."),
        entry("User not found.", "User not found."),
        entry("Token is required", "Authorization token is required."),
        entry("Invalid token.", "Invalid or malformed token."),
        entry("Email already in use", "Email address is already in use."),
        entry("Profile image is required.", "Profile image is required."),
        entry("Invalid file path", "Invalid file path: Provide a valid local file path."),
        // JwtUtil error messages (English)
        entry("Invalid JWT secret key", "Invalid JWT secret key."),
        entry("User ID cannot be null", "User ID cannot be null."),
        entry("User ID not found in token", "User ID not found in token."),
        entry("Invalid user ID format in token", "Invalid user ID format in token."),
        entry("Invalid token", "Invalid token."),
        entry("Role not found in token", "Role not found in token."),
        entry("Invalid token: Unable to retrieve expiration date", "Invalid token: Unable to retrieve expiration date."),
        entry("Failed to blacklist token", "Failed to blacklist token.")
    );

    private static final Map<String, String> ERROR_CODE_MAPPINGS = Map.of(
        "User not found.", "USER_NOT_FOUND",
        "Invalid token.", "INVALID_TOKEN",
        // JwtUtil error codes
        "Invalid JWT secret key", "INVALID_JWT_SECRET",
        "User ID cannot be null", "INVALID_USER_ID",
        "User ID not found in token", "MISSING_USER_ID",
        "Invalid user ID format in token", "INVALID_USER_ID_FORMAT",
        "Invalid token", "INVALID_TOKEN",
        "Role not found in token", "MISSING_ROLE",
        "Invalid token: Unable to retrieve expiration date", "INVALID_TOKEN_EXPIRATION",
        "Failed to blacklist token", "BLACKLIST_TOKEN_FAILED"
    );

    /**
     * Builds a standardized error response.
     *
     * @param message The error message.
     * @param code    The error code.
     * @param status  The HTTP status.
     * @param details Optional error details.
     * @return The error response.
     */
    private ApiErrorResponse buildResponse(String message, String code, HttpStatus status, Map<String, String> details) {
        return ApiErrorResponse.builder()
                .error(message)
                .errorCode(code)
                .status(status.value())
                .timestamp(ZonedDateTime.now().toString())
                .details(details)
                .build();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String errorMessage = "Unsupported content type: " + ex.getContentType() + ". Expected 'application/json'.";
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(buildResponse(errorMessage, "UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE, null));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse> handleMultipartException(MultipartException ex) {
        String errorMessage = "Failed to parse multipart request: '/api/admin/movies' endpoint no longer supports multipart. Please use 'application/json'.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(errorMessage, "INVALID_MULTIPART_REQUEST", HttpStatus.BAD_REQUEST, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String errorMessage = ERROR_MESSAGE_MAPPINGS.getOrDefault(ex.getMessage(), ex.getMessage());
        String errorCode = ERROR_CODE_MAPPINGS.getOrDefault(ex.getMessage(), "INVALID_INPUT");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(errorMessage, errorCode, HttpStatus.BAD_REQUEST, null));
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse("Database access error: Invalid or missing identifier.", "INVALID_DATABASE_ACCESS", HttpStatus.BAD_REQUEST, null));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildResponse(ex.getMessage(), "UNAUTHORIZED", HttpStatus.UNAUTHORIZED, null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildResponse(ex.getMessage(), "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, null));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildResponse(ex.getMessage(), "USER_NOT_FOUND", HttpStatus.NOT_FOUND, null));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJwtException(MalformedJwtException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse("Invalid token format: " + ex.getMessage(), "INVALID_TOKEN", HttpStatus.BAD_REQUEST, null));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorResponse> handleIOException(IOException ex) {
        String errorMessage = "Failed to process image: " + ex.getMessage();
        if (ex.getMessage().contains("Source file not found")) {
            errorMessage = "Source file not found: " + ex.getMessage();
        } else if (ex.getMessage().contains("Failed to download image from URL")) {
            errorMessage = "Failed to download image from URL: " + ex.getMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(errorMessage, "IMAGE_PROCESSING_ERROR", HttpStatus.BAD_REQUEST, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse("Validation error", "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse("An unexpected error occurred.", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, null));
    }
}