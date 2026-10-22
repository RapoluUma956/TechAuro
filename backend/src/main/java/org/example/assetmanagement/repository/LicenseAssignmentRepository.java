package org.example.assetmanagement.repository;

import org.example.assetmanagement.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface LicenseAssignmentRepository
        extends JpaRepository<LicenseAssignment, Long> {

    List<LicenseAssignment> findByUser_UserId(
            Long userId
    );

    List<LicenseAssignment> findByLicense_LicenseId(
            Long licenseId
    );

    List<LicenseAssignment> findByStatus(
            String status
    );
}