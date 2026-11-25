package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.RemoteTerminalSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemoteTerminalSessionRepository extends JpaRepository<RemoteTerminalSession, Long> {
    Optional<RemoteTerminalSession> findBySessionId(String sessionId);
    List<RemoteTerminalSession> findByDeviceIdOrderByStartedAtDesc(Long deviceId);
    List<RemoteTerminalSession> findBySerialNumberOrderByStartedAtDesc(String serialNumber);
}

