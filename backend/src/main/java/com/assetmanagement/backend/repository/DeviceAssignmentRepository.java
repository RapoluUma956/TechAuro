package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceAssignmentRepository
        extends JpaRepository<DeviceAssignment, Long> {

    List<DeviceAssignment> findByUser_UserId(Long userId);

    List<DeviceAssignment> findByDevice_DeviceId(Long deviceId);

    Optional<DeviceAssignment> findByDevice_DeviceIdAndStatus(
            Long deviceId,
            String status
    );

    List<DeviceAssignment> findByStatus(String status);
}