package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmployeeCode(String employeeCode);

    Optional<User> findByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);
}