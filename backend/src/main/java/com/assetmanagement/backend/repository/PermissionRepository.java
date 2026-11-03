package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermissionCode(String permissionCode);
    Optional<Permission> findByPermissionName(String permissionName);
    boolean existsByPermissionCode(String permissionCode);
    boolean existsByPermissionName(String permissionName);
    List<Permission> findAllByOrderByPermissionIdAsc();
}

