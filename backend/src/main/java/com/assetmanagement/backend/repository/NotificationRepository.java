package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByDevice_DeviceIdOrderByCreatedAtDesc(Long deviceId);

    List<Notification> findByStatusOrderByCreatedAtDesc(String status);

    List<Notification> findBySeverity(String severity);

    List<Notification> findAllByOrderByCreatedAtDesc();

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Notification> findByIsReadFalseOrderByCreatedAtDesc(Pageable pageable);

    long countByIsReadFalse();

    long countBySeverityIgnoreCase(String severity);

    long countBySeverityIgnoreCaseAndIsReadFalse(String severity);

    boolean existsByTitleAndDevice_DeviceId(String title, Long deviceId);

    boolean existsByTitle(String title);

    List<Notification> findByIsReadFalse();

    @Query("SELECT n FROM Notification n WHERE (n.user.userId IN :userIds OR n.device.deviceId IN :deviceIds) ORDER BY n.createdAt DESC")
    List<Notification> findByUser_UserIdInOrDevice_DeviceIdInOrderByCreatedAtDesc(
            @Param("userIds") Collection<Long> userIds,
            @Param("deviceIds") Collection<Long> deviceIds
    );

    @Query("SELECT n FROM Notification n WHERE n.user.userId IN :userIds ORDER BY n.createdAt DESC")
    List<Notification> findByUser_UserIdInOrderByCreatedAtDesc(
            @Param("userIds") Collection<Long> userIds
    );
}
