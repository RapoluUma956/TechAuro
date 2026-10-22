package org.example.assetmanagement.repository;

import org.example.assetmanagement.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByAssetTag(String assetTag);

    Optional<Device> findBySerialNumber(String serialNumber);

    boolean existsByAssetTag(String assetTag);

    boolean existsBySerialNumber(String serialNumber);
}