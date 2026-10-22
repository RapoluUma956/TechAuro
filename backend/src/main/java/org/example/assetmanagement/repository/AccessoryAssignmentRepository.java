package org.example.assetmanagement.repository;

import org.example.assetmanagement.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface AccessoryAssignmentRepository
        extends JpaRepository<AccessoryAssignment, Long> {

    List<AccessoryAssignment> findByUser_UserId(
            Long userId
    );

    List<AccessoryAssignment> findByAccessory_AccessoryId(
            Long accessoryId
    );

    List<AccessoryAssignment> findByStatus(
            String status
    );
}