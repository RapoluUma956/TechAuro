package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.DeviceAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
    Optional<DeviceAssignment> findFirstByDevice_DeviceIdAndStatusOrderByAssignedAtDesc(
            Long deviceId,
            String status
    );

    default Optional<DeviceAssignment> findByDevice_DeviceIdAndStatus(
            Long deviceId,
            String status
    ) {
        return findFirstByDevice_DeviceIdAndStatusOrderByAssignedAtDesc(deviceId, status);
    }

    @EntityGraph(attributePaths = {"device", "user"})
    Optional<DeviceAssignment> findFirstByUser_UserIdAndStatusOrderByAssignedAtDesc(
            Long userId,
            String status
    );

    default Optional<DeviceAssignment> findByUser_UserIdAndStatus(
            Long userId,
            String status
    ) {
        return findFirstByUser_UserIdAndStatusOrderByAssignedAtDesc(userId, status);
    }

    @EntityGraph(attributePaths = {"device", "user"})
    Optional<DeviceAssignment> findFirstBySerialNumberAndStatusOrderByAssignedAtDesc(
            String serialNumber,
            String status
    );

    default Optional<DeviceAssignment> findBySerialNumberAndStatus(
            String serialNumber,
            String status
    ) {
        return findFirstBySerialNumberAndStatusOrderByAssignedAtDesc(serialNumber, status);
    }

    @EntityGraph(attributePaths = {"device", "user"})
    Optional<DeviceAssignment> findFirstBySerialNumberIgnoreCaseAndStatusOrderByAssignedAtDesc(
            String serialNumber,
            String status
    );

    default Optional<DeviceAssignment> findBySerialNumberIgnoreCaseAndStatus(
            String serialNumber,
            String status
    ) {
        return findFirstBySerialNumberIgnoreCaseAndStatusOrderByAssignedAtDesc(serialNumber, status);
    }

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findBySerialNumber(String serialNumber);

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findByStatus(String status);

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findByDevice_DeviceIdOrderByAssignedAtDesc(Long deviceId);

    @EntityGraph(attributePaths = {"device", "user"})
    List<DeviceAssignment> findByUser_UserIdOrderByAssignedAtDesc(Long userId);

    @Query("SELECT DISTINCT da.device.deviceId FROM DeviceAssignment da WHERE da.user.userId IN :userIds AND da.status = :status")
    List<Long> findDeviceIdsByUser_UserIdInAndStatus(@Param("userIds") Collection<Long> userIds, @Param("status") String status);

    @EntityGraph(attributePaths = {"device", "user"})
    @Query("SELECT da FROM DeviceAssignment da WHERE da.user.userId IN :userIds AND da.status = :status")
    List<DeviceAssignment> findByUser_UserIdInAndStatus(@Param("userIds") Collection<Long> userIds, @Param("status") String status);

    @EntityGraph(attributePaths = {"device", "user"})
    @Query("SELECT da FROM DeviceAssignment da WHERE da.user.userId IN :userIds")
    List<DeviceAssignment> findByUser_UserIdIn(@Param("userIds") Collection<Long> userIds);
}
