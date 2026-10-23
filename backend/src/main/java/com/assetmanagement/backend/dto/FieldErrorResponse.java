package com.assetmanagement.backend.dto;

public record FieldErrorResponse(
        String field,
        String message
) {}
