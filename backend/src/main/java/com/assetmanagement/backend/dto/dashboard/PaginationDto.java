package com.assetmanagement.backend.dto.dashboard;

public record PaginationDto(
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
