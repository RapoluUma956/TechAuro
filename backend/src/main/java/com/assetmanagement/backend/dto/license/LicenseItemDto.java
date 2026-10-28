package com.assetmanagement.backend.dto.license;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LicenseItemDto(
        Long licenseId,
        String licenseName,
        String softwareName,
        String publisher,
        String licenseKey,
        String licenseType,
        String renewalType,
        String renewalMethod,
        Integer totalQuantity,
        Integer assignedQuantity,
        Integer availableQuantity,
        LocalDate purchaseDate,
        LocalDate startDate,
        LocalDate expiryDate,
        String status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
