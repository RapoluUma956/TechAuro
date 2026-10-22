package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Notification;
import com.assetmanagement.backend.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsRead(Long id) {
        Notification notification = getNotificationById(id);
        notification.setStatus("READ");
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotification(Long id) {
        Notification notification = getNotificationById(id);
        notificationRepository.delete(notification);
    }
}