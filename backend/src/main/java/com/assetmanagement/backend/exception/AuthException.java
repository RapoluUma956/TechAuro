package com.assetmanagement.backend.exception;

import com.assetmanagement.backend.dto.FieldErrorResponse;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class AuthException extends RuntimeException {

    private final HttpStatus status;
    private final List<FieldErrorResponse> fieldErrors;

    public AuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.fieldErrors = new ArrayList<>();
    }

    public AuthException(HttpStatus status, String message, List<FieldErrorResponse> fieldErrors) {
        super(message);
        this.status = status;
        this.fieldErrors = (fieldErrors != null) ? fieldErrors : new ArrayList<>();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<FieldErrorResponse> getFieldErrors() {
        return fieldErrors;
    }
}
