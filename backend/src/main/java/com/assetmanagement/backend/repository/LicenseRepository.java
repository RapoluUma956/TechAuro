package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LicenseRepository
        extends JpaRepository<License, Long>, JpaSpecificationExecutor<License> {

    List<License> findBySoftwareName(String softwareName);

    List<License> findByStatus(String status);

    List<License> findByLicenseType(String licenseType);

    @Query("SELECT DISTINCT l.softwareName FROM License l WHERE l.softwareName IS NOT NULL AND TRIM(l.softwareName) != '' ORDER BY l.softwareName")
    List<String> findDistinctSoftwareNames();

    @Query("SELECT DISTINCT l.publisher FROM License l WHERE l.publisher IS NOT NULL AND TRIM(l.publisher) != '' ORDER BY l.publisher")
    List<String> findDistinctPublishers();

    @Query("SELECT COALESCE(SUM(l.totalQuantity), 0) FROM License l")
    Long sumTotalSeats();

    @Query("SELECT COUNT(l) FROM License l WHERE l.expiryDate IS NOT NULL AND l.expiryDate >= :today AND l.expiryDate <= :soonDate AND UPPER(l.status) != 'EXPIRED'")
    long countExpiringSoon(@Param("today") LocalDate today, @Param("soonDate") LocalDate soonDate);
}