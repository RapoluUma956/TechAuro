package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.AccessoryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AccessoryAssignmentRepository extends JpaRepository<AccessoryAssignment, Long> {

    List<AccessoryAssignment> findByUser_UserId(Long userId);

    List<AccessoryAssignment> findByUser_UserIdIn(Collection<Long> userIds);

    List<AccessoryAssignment> findByUser_UserIdInAndStatus(Collection<Long> userIds, String status);

    List<AccessoryAssignment> findByAccessory_AccessoryId(Long accessoryId);

    List<AccessoryAssignment> findByAccessory_AccessoryIdAndUser_UserIdIn(Long accessoryId, Collection<Long> userIds);

    List<AccessoryAssignment> findByAccessory_AccessoryIdAndUser_UserIdInAndStatus(Long accessoryId, Collection<Long> userIds, String status);

    List<AccessoryAssignment> findByStatus(String status);

    long countByStatus(String status);

    long countByUser_UserIdInAndStatus(Collection<Long> userIds, String status);
}
