package org.example.assetmanagement.repository;

import org.example.assetmanagement.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface LicenseRepository
        extends JpaRepository<License, Long> {

    List<License> findBySoftwareName(String softwareName);

    List<License> findByStatus(String status);

    List<License> findByLicenseType(String licenseType);
}