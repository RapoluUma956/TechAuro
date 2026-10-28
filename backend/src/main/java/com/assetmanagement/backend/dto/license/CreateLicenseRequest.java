package com.assetmanagement.backend.dto.license;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateLicenseRequest(
        @NotBlank(message = "License name is required")
        String licenseName,

        @NotBlank(message = "Software name is required")
        String softwareName,

        @NotBlank(message = "Publisher is required")
        String publisher,

        String licenseKey,

        @NotBlank(message = "License type is required")
        String licenseType,

        String renewalType,

        String renewalMethod,

        @NotNull(message = "Total quantity is required")
        @Min(value = 1, message = "Total quantity must be at least 1")
        Integer totalQuantity,

        LocalDate purchaseDate,

        LocalDate startDate,

        LocalDate expiryDate,

        String status,

        String notes
) {}
