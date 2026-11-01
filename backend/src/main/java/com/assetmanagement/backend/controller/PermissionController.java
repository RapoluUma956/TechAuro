package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.ApiResponse;
import com.assetmanagement.backend.dto.rbac.RbacDtos.PermissionDto;
import com.assetmanagement.backend.service.RbacService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/permissions", "/api/permissions"})
@CrossOrigin(originPatterns = "*")
public class PermissionController {

    private final RbacService rbacService;

    public PermissionController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    // GET /api/permissions
    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionDto>>> getAllPermissions() {
        List<PermissionDto> permissions = rbacService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved successfully", permissions));
    }
}

