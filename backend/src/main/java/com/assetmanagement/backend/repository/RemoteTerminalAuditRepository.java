package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.RemoteTerminalAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemoteTerminalAuditRepository extends JpaRepository<RemoteTerminalAudit, Long> {
    List<RemoteTerminalAudit> findBySessionIdOrderByRecordedAtAsc(String sessionId);
    List<RemoteTerminalAudit> findByDeviceIdOrderByRecordedAtDesc(Long deviceId);
}

