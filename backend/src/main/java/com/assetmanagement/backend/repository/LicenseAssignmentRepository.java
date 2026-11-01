package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.LicenseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseAssignmentRepository extends JpaRepository<LicenseAssignment, Long> {

    List<LicenseAssignment> findByUser_UserId(Long userId);

    List<LicenseAssignment> findByUser_UserIdIn(Collection<Long> userIds);

    List<LicenseAssignment> findByUser_UserIdInAndStatus(Collection<Long> userIds, String status);

    List<LicenseAssignment> findByLicense_LicenseId(Long licenseId);

    List<LicenseAssignment> findByLicense_LicenseIdAndUser_UserIdIn(Long licenseId, Collection<Long> userIds);

    List<LicenseAssignment> findByLicense_LicenseIdAndUser_UserIdInOrderByAssignedAtDesc(Long licenseId, Collection<Long> userIds);

    List<LicenseAssignment> findByLicense_LicenseIdOrderByAssignedAtDesc(Long licenseId);

    List<LicenseAssignment> findByStatus(String status);

    long countByLicense_LicenseIdAndStatus(Long licenseId, String status);

    long countByLicense_LicenseIdAndUser_UserIdInAndStatus(Long licenseId, Collection<Long> userIds, String status);

    long countByStatus(String status);

    long countByUser_UserIdInAndStatus(Collection<Long> userIds, String status);

    Optional<LicenseAssignment> findByAssignmentIdAndLicense_LicenseId(Long assignmentId, Long licenseId);

    boolean existsByLicense_LicenseIdAndUser_UserIdAndStatus(Long licenseId, Long userId, String status);
}
