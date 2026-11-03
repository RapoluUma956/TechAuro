package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.ApiResponse;
import com.assetmanagement.backend.dto.rbac.RbacDtos.*;
import com.assetmanagement.backend.service.RbacService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/users/{userId}/roles", "/api/users/{userId}/roles"})
@CrossOrigin(originPatterns = "*")
public class UserRoleController {

    private final RbacService rbacService;

    public UserRoleController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserRolesResponse>> getUserRoles(@PathVariable Long userId) {
        UserRolesResponse res = rbacService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success("User roles retrieved successfully", res));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserRolesResponse>> assignUserRole(
            @PathVariable Long userId,
            @RequestBody AssignUserRoleRequest request) {
        UserRolesResponse res = rbacService.assignUserRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User roles updated successfully", res));
    }
}

