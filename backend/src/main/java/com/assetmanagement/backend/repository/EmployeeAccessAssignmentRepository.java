package com.assetmanagement.backend.repository;

import com.assetmanagement.backend.entity.EmployeeAccessAssignment;
import com.assetmanagement.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAccessAssignmentRepository extends JpaRepository<EmployeeAccessAssignment, Long> {

    List<EmployeeAccessAssignment> findByManager(User manager);

    List<EmployeeAccessAssignment> findByManager_UserId(Long managerUserId);

    List<EmployeeAccessAssignment> findByEmployee(User employee);

    List<EmployeeAccessAssignment> findByEmployee_UserId(Long employeeUserId);

    boolean existsByManager_UserIdAndEmployee_UserId(Long managerUserId, Long employeeUserId);

    Optional<EmployeeAccessAssignment> findByManager_UserIdAndEmployee_UserId(Long managerUserId, Long employeeUserId);

    void deleteByManager_UserIdAndEmployee_UserId(Long managerUserId, Long employeeUserId);

    void deleteByManager_UserId(Long managerUserId);

    void deleteByEmployee_UserId(Long employeeUserId);

    long countByManager_UserId(Long managerUserId);

    @Query("SELECT e.employee.userId FROM EmployeeAccessAssignment e WHERE e.manager.userId = :managerId")
    List<Long> findAssignedEmployeeIdsByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT e.manager.userId FROM EmployeeAccessAssignment e WHERE e.employee.userId = :employeeId")
    List<Long> findManagerIdsByEmployeeId(@Param("employeeId") Long employeeId);
}

