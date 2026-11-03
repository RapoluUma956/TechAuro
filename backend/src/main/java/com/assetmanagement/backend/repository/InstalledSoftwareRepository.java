package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.InstalledSoftware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstalledSoftwareRepository extends JpaRepository<InstalledSoftware, Long> {

    List<InstalledSoftware> findByDevice_DeviceId(Long deviceId);

    List<InstalledSoftware> findByDevice_DeviceIdIn(Collection<Long> deviceIds);

    Optional<InstalledSoftware> findByDevice_DeviceIdAndSoftwareNameIgnoreCase(
            Long deviceId,
            String softwareName
    );
}
