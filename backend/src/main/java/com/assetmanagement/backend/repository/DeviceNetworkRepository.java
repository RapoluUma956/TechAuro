package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceNetworkRepository
        extends JpaRepository<DeviceNetwork, Long> {

    List<DeviceNetwork> findByDevice_DeviceId(Long deviceId);

    List<DeviceNetwork> findByDevice_DeviceIdAndConnectionStatus(
            Long deviceId,
            String connectionStatus
    );

    Optional<DeviceNetwork> findByDevice_DeviceIdAndInterfaceName(
            Long deviceId,
            String interfaceName
    );
}