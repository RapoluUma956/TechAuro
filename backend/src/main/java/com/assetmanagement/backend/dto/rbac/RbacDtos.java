package com.assetmanagement.backend.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class RbacDtos {

    public record RoleSummaryDto(
            Long id,
            String roleName,
            String description,
            boolean isSystemRole,
            long assignedEmployeeCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record CreateRoleRequest(
            @NotBlank(message = "Role name is required")
            String roleName,
            String description
    ) {}

    public record UpdateRoleRequest(
            @NotBlank(message = "Role name is required")
            String roleName,
            String description
    ) {}

    public record PermissionDto(
            Long id,
            String moduleName,
            String permissionCode,
            String description
    ) {}

    public record RolePermissionDto(
            Long permissionId,
            String moduleName,
            String permissionCode,
            String description,
            String accessLevel,
            String dataScope
    ) {}

    public record RolePermissionMatrixResponse(
            Long roleId,
            String roleName,
            String description,
            boolean isSystemRole,
            List<RolePermissionDto> permissions
    ) {}

    public record UpdatePermissionItemRequest(
            @NotNull(message = "Permission ID is required")
            Long permissionId,
            @NotBlank(message = "Access level is required (HIDE, READ, WRITE)")
            String accessLevel,
            String dataScope
    ) {}

    public record UpdateRolePermissionsRequest(
            List<UpdatePermissionItemRequest> permissions
    ) {}

    public record AssignUserRoleRequest(
            Long roleId,
            List<Long> roleIds
    ) {}

    public record UserRolesResponse(
            Long userId,
            List<String> roleNames,
            List<Long> roleIds
    ) {}

    public record AssignedEmployeeDto(
            Long userId,
            String employeeCode,
            String fullName,
            String email,
            String designation,
            String status,
            LocalDateTime assignedAt
    ) {}

    public record AssignManagerEmployeesRequest(
            List<Long> employeeUserIds
    ) {}

    public record ManagerAssignmentSummaryDto(
            Long managerUserId,
            String managerName,
            String managerEmail,
            int assignedCount,
            List<AssignedEmployeeDto> assignedEmployees
    ) {}
}

