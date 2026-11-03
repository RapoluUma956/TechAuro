package com.assetmanagement.backend.exception;

import com.assetmanagement.backend.dto.ApiResponse;
import com.assetmanagement.backend.dto.FieldErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 BAD REQUEST: Bean Validation Failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        ApiResponse<Void> response = ApiResponse.error("Please correct the highlighted fields", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 BAD REQUEST: Malformed or unreadable JSON request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed HTTP request body: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("Malformed or unreadable JSON request body. Please verify the request payload.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 BAD REQUEST: Missing required query parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        ApiResponse<Void> response = ApiResponse.error("Required parameter '" + ex.getParameterName() + "' is missing.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 BAD REQUEST: Type mismatch in URL or parameters
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = String.format("Invalid value '%s' for parameter '%s'. Expected format: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type");
        ApiResponse<Void> response = ApiResponse.error(msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 BAD REQUEST: Illegal argument / state
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 401 UNAUTHORIZED / 403 FORBIDDEN: Authentication Exceptions
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), ex.getFieldErrors());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    // 404 NOT FOUND: Entity Not Found Exceptions
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "The requested resource could not be found.";
        ApiResponse<Void> response = ApiResponse.error(message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 405 METHOD NOT ALLOWED
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String msg = String.format("HTTP method '%s' is not supported for this endpoint.", ex.getMethod());
        ApiResponse<Void> response = ApiResponse.error(msg);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // 409 CONFLICT: Database constraint & duplicate violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Database integrity violation: {}", ex.getMessage());
        String msg = "A record with this identifier, serial number, asset tag, or email already exists.";
        ApiResponse<Void> response = ApiResponse.error(msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Custom HTTP Status Exceptions from service layer (404, 409, 400, etc.)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : "Request could not be processed";
        ApiResponse<Void> response = ApiResponse.error(reason);
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    // 500 INTERNAL SERVER ERROR: Uncaught server exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled server exception: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error("Something went wrong. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
