package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.HealthThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthThresholdRepository extends JpaRepository<HealthThreshold, Long> {

    Optional<HealthThreshold> findFirstByOrderByThresholdIdAsc();
}
