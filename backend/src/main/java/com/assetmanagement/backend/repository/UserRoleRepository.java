package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.entity.UserRole;
import com.assetmanagement.backend.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByUser_UserId(Long userId);
    void deleteByUser(User user);
}
