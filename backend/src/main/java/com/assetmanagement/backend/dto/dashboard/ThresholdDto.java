package com.assetmanagement.backend.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record ThresholdDto(
        Long thresholdId,
        Integer cpuWarningPercent,
        Integer ramWarningPercent,
        Integer diskCriticalPercent,
        Integer offlineTimeoutMinutes,
        Integer warrantyWarningDays,
        Integer licenseWarningDays,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime updatedAt
) {}
