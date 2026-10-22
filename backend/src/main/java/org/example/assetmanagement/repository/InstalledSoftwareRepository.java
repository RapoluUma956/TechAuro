package org.example.assetmanagement.repository;

import org.example.assetmanagement.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstalledSoftwareRepository
        extends JpaRepository<InstalledSoftware, Long> {

    List<InstalledSoftware> findByDevice_DeviceId(Long deviceId);

    Optional<InstalledSoftware> findByDevice_DeviceIdAndSoftwareNameIgnoreCase(
            Long deviceId,
            String softwareName
    );
}