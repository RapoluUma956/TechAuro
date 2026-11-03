package com.assetmanagement.backend.dto.notification;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationDtos {

    public record NotificationDto(
            Long notificationId,
            String notificationType,
            String title,
            String message,
            String severity,
            String status,
            Boolean isRead,
            LocalDateTime createdAt,
            LocalDateTime readAt,
            Long deviceId,
            String deviceAssetTag,
            String deviceName,
            String serialNumber,
            Long userId,
            String userName,
            String employeeCode
    ) {}

    public record NotificationSummaryResponse(
            long total,
            long unreadCount,
            long criticalCount,
            long highCount,
            long mediumCount,
            long lowCount
    ) {}

    public record NotificationPageResponse(
            List<NotificationDto> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last,
            long unreadCount
    ) {}

    public record CreateNotificationRequest(
            String notificationType,
            String title,
            String message,
            String severity,
            Long deviceId,
            Long userId
    ) {}
}

