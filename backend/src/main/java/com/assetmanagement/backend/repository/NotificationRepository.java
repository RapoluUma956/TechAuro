package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<Notification> findByDevice_DeviceIdOrderByCreatedAtDesc(
            Long deviceId
    );

    List<Notification> findByStatusOrderByCreatedAtDesc(
            String status
    );

    List<Notification> findBySeverity(
            String severity
    );
}