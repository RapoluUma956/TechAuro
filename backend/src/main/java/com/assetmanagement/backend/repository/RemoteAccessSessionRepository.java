package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.RemoteAccessSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemoteAccessSessionRepository extends JpaRepository<RemoteAccessSession, Long> {
    Optional<RemoteAccessSession> findBySessionId(String sessionId);
    List<RemoteAccessSession> findByDeviceIdOrderByRequestedAtDesc(Long deviceId);
    List<RemoteAccessSession> findBySerialNumberOrderByRequestedAtDesc(String serialNumber);
}
