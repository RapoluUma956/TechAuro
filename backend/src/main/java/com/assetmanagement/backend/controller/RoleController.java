package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.ApiResponse;
import com.assetmanagement.backend.dto.rbac.RbacDtos.*;
import com.assetmanagement.backend.service.RbacService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/roles", "/api/roles"})
@CrossOrigin(originPatterns = "*")
public class RoleController {

    private final RbacService rbacService;

    public RoleController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    // 1. GET /api/roles
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleSummaryDto>>> getAllRoles(
            @RequestParam(required = false) String search) {
        List<RoleSummaryDto> roles = rbacService.getAllRoles(search);
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    // 2. GET /api/roles/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleSummaryDto>> getRoleById(@PathVariable Long id) {
        RoleSummaryDto role = rbacService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role));
    }

    // 3. POST /api/roles
    @PostMapping
    public ResponseEntity<ApiResponse<RoleSummaryDto>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        RoleSummaryDto created = rbacService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", created));
    }

    // 4. PUT /api/roles/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleSummaryDto>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleSummaryDto updated = rbacService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updated));
    }

    // 5. DELETE /api/roles/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        rbacService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    // 5b. GET /api/roles/{id}/users
    @GetMapping("/{id}/users")
    public ResponseEntity<ApiResponse<List<AssignedEmployeeDto>>> getAssignedEmployees(@PathVariable Long id) {
        List<AssignedEmployeeDto> users = rbacService.getAssignedEmployees(id);
        return ResponseEntity.ok(ApiResponse.success("Assigned employees retrieved successfully", users));
    }

    // 6. GET /api/roles/{id}/permissions
    @GetMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<RolePermissionMatrixResponse>> getRolePermissions(@PathVariable Long id) {
        RolePermissionMatrixResponse matrix = rbacService.getRolePermissionMatrix(id);
        return ResponseEntity.ok(ApiResponse.success("Role permissions retrieved successfully", matrix));
    }

    // 7. PUT /api/roles/{id}/permissions
    @PutMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<RolePermissionMatrixResponse>> updateRolePermissions(
            @PathVariable Long id,
            @RequestBody UpdateRolePermissionsRequest request) {
        RolePermissionMatrixResponse matrix = rbacService.updateRolePermissions(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role permissions updated successfully", matrix));
    }
}

