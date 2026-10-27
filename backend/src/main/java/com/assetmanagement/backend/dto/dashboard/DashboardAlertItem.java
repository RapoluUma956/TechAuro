package com.assetmanagement.backend.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record DashboardAlertItem(
        String id,
        String type,
        String severity,
        String title,
        String message,
        Long deviceId,
        String assetTag,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime createdAt,
        boolean read
) {}
