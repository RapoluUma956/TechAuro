package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.DeviceAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceAssignmentRepository
        extends JpaRepository<DeviceAssignment, Long> {

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findByUser_UserId(Long userId);

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findByDevice_DeviceId(Long deviceId);

    @EntityGraph(attributePaths = {"device", "user"})
    Optional<DeviceAssignment> findByDevice_DeviceIdAndStatus(
            Long deviceId,
            String status
    );

    @EntityGraph(attributePaths = {"device", "user"})
    Optional<DeviceAssignment> findByUser_UserIdAndStatus(
            Long userId,
            String status
    );

    @EntityGraph(attributePaths = {"device", "user"})
    Optional<DeviceAssignment> findBySerialNumberAndStatus(
            String serialNumber,
            String status
    );

    @EntityGraph(attributePaths = {"device", "user"})
    Optional<DeviceAssignment> findBySerialNumberIgnoreCaseAndStatus(
            String serialNumber,
            String status
    );

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findBySerialNumber(String serialNumber);

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findByStatus(String status);

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findByDevice_DeviceIdOrderByAssignedAtDesc(Long deviceId);

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findByUser_UserIdOrderByAssignedAtDesc(Long userId);
}
