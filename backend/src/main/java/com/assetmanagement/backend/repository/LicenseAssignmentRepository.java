package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.*;
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

    List<LicenseAssignment> findByLicense_LicenseIdOrderByAssignedAtDesc(
            Long licenseId
    );

    List<LicenseAssignment> findByStatus(
            String status
    );

    long countByLicense_LicenseIdAndStatus(
            Long licenseId,
            String status
    );

    long countByStatus(
            String status
    );

    java.util.Optional<LicenseAssignment> findByAssignmentIdAndLicense_LicenseId(
            Long assignmentId,
            Long licenseId
    );

    boolean existsByLicense_LicenseIdAndUser_UserIdAndStatus(
            Long licenseId,
            Long userId,
            String status
    );
}