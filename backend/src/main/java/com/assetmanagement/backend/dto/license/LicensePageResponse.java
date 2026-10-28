package com.assetmanagement.backend.dto.license;

import java.util.List;

public record LicensePageResponse(
        List<LicenseItemDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
