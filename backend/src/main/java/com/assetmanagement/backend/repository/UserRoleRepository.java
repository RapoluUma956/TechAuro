package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.entity.UserRole;
import com.assetmanagement.backend.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByUser_UserId(Long userId);
    List<UserRole> findByRole_RoleId(Long roleId);
    long countByRole_RoleId(Long roleId);
    boolean existsByRole_RoleId(Long roleId);
    Optional<UserRole> findByUser_UserIdAndRole_RoleId(Long userId, Long roleId);
    void deleteByUser(User user);
    void deleteByUser_UserId(Long userId);
}
