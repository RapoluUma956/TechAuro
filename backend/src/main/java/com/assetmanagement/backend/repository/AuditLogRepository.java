package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByUser_UserId(Long userId);

    List<AuditLog> findByAction(String action);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
}
