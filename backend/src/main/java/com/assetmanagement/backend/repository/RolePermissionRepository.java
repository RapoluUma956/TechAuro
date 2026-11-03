package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.Role;
import com.assetmanagement.backend.entity.RolePermission;
import com.assetmanagement.backend.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    List<RolePermission> findByRole_RoleId(Long roleId);
    List<RolePermission> findByRoleIn(List<Role> roles);
    Optional<RolePermission> findByRole_RoleIdAndPermission_PermissionId(Long roleId, Long permissionId);
    Optional<RolePermission> findByRole_RoleIdAndPermission_PermissionCode(Long roleId, String permissionCode);
    void deleteByRole_RoleId(Long roleId);
    void deleteByRole_RoleIdAndPermission_PermissionId(Long roleId, Long permissionId);
    long countByRole_RoleId(Long roleId);
}

