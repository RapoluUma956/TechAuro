package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByAssetTag(String assetTag);

    Optional<Device> findBySerialNumber(String serialNumber);

    Optional<Device> findBySerialNumberIgnoreCase(String serialNumber);

    Optional<Device> findByHostname(String hostname);

    Optional<Device> findByCurrentUserId(Long currentUserId);

    List<Device> findByCurrentUserIdIn(java.util.Collection<Long> userIds);

    List<Device> findByDeviceIdIn(java.util.Collection<Long> deviceIds);

    @org.springframework.data.jpa.repository.Query("SELECT d.deviceId FROM Device d WHERE d.currentUserId IN :userIds")
    List<Long> findDeviceIdsByCurrentUserIdIn(@org.springframework.data.repository.query.Param("userIds") java.util.Collection<Long> userIds);

    List<Device> findByDeviceStatus(String deviceStatus);

    List<Device> findByConnectionStatus(String connectionStatus);

    boolean existsByAssetTag(String assetTag);

    boolean existsBySerialNumber(String serialNumber);
}
