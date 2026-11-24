package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.RemoteAccessAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemoteAccessAuditRepository extends JpaRepository<RemoteAccessAudit, Long> {
    List<RemoteAccessAudit> findBySessionIdOrderByRecordedAtAsc(String sessionId);
    List<RemoteAccessAudit> findByDeviceIdOrderByRecordedAtDesc(Long deviceId);
}
