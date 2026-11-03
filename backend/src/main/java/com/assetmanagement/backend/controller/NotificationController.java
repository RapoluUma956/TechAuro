package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.notification.NotificationDtos.*;
import com.assetmanagement.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/notifications", "/api/notifications"})
@CrossOrigin(originPatterns = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 1. Get notifications with pagination, filters, and search
    @GetMapping
    public ResponseEntity<NotificationPageResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) String search) {

        // Auto-synchronize rule-based notifications before querying
        notificationService.syncSystemNotifications();

        return ResponseEntity.ok(notificationService.getNotifications(page, size, severity, type, unreadOnly, search));
    }

    // 2. Notification summary metrics
    @GetMapping("/summary")
    public ResponseEntity<NotificationSummaryResponse> getSummary() {
        notificationService.syncSystemNotifications();
        return ResponseEntity.ok(notificationService.getSummary());
    }

    // 3. Trigger manual notification synchronization
    @PostMapping("/sync")
    public ResponseEntity<Void> syncNotifications() {
        notificationService.syncSystemNotifications();
        return ResponseEntity.ok().build();
    }

    // 4. Get single notification
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    // 5. Get notifications by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    // 6. Create custom notification
    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(@RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
    }

    // 7. Mark single notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsReadPatch(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsReadPut(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    // 8. Mark all notifications as read
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    // 9. Delete / Dismiss notification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
