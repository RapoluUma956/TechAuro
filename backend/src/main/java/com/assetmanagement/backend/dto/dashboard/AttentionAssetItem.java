package com.assetmanagement.backend.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record AttentionAssetItem(
        Long deviceId,
        String assetTag,
        Long employeeId,
        String employeeName,
        String designation,
        String healthStatus,
        String connectionStatus,
        Integer cpuUsage,
        Integer ramUsage,
        Integer diskUsage,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime lastSeenAt
) {}
