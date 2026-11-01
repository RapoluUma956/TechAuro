package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.ApiResponse;
import com.assetmanagement.backend.dto.rbac.RbacDtos.*;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.service.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/managers", "/api/managers"})
@CrossOrigin(originPatterns = "*")
public class ManagerAssignmentController {

    private final RbacService rbacService;

    public ManagerAssignmentController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    // 1. GET /api/managers/{managerId}/employees
    @GetMapping("/{managerId}/employees")
    public ResponseEntity<ApiResponse<ManagerAssignmentSummaryDto>> getAssignedEmployees(
            @PathVariable Long managerId) {
        ManagerAssignmentSummaryDto data = rbacService.getManagerAssignmentSummary(managerId);
        return ResponseEntity.ok(ApiResponse.success("Manager assigned employees retrieved successfully", data));
    }

    // 2. PUT /api/managers/{managerId}/employees
    @PutMapping("/{managerId}/employees")
    public ResponseEntity<ApiResponse<ManagerAssignmentSummaryDto>> assignEmployees(
            @PathVariable Long managerId,
            @Valid @RequestBody AssignManagerEmployeesRequest request) {
        ManagerAssignmentSummaryDto data = rbacService.assignEmployeesToManager(managerId, request);
        return ResponseEntity.ok(ApiResponse.success("Employees assigned to manager successfully", data));
    }

    // 3. GET /api/managers/me/employees (returns the list of employees assigned to currently logged in manager)
    @GetMapping("/me/employees")
    public ResponseEntity<ApiResponse<List<AssignedEmployeeDto>>> getMyAssignedEmployees(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        List<AssignedEmployeeDto> employees = rbacService.getAssignedEmployeesForManager(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("My assigned employees retrieved successfully", employees));
    }
}

