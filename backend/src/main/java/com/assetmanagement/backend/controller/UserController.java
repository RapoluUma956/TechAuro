package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.AssignDeviceRequest;
import com.assetmanagement.backend.dto.EmployeeDetailResponse;
import com.assetmanagement.backend.dto.UserRequest;
import com.assetmanagement.backend.dto.UserResponse;
import com.assetmanagement.backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
@CrossOrigin(originPatterns = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<EmployeeDetailResponse> getEmployeeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getEmployeeDetail(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<EmployeeDetailResponse> getEmployeeDetailsAlias(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getEmployeeDetail(id));
    }

    @GetMapping("/employee/{employeeCode}")
    public ResponseEntity<UserResponse> getUserByEmployeeCode(@PathVariable String employeeCode) {
        return ResponseEntity.ok(userService.getUserByEmployeeCode(employeeCode));
    }

    @GetMapping("/employee/{employeeCode}/detail")
    public ResponseEntity<EmployeeDetailResponse> getEmployeeDetailByCode(@PathVariable String employeeCode) {
        return ResponseEntity.ok(userService.getEmployeeDetailByCode(employeeCode));
    }

    @GetMapping("/employee/{employeeCode}/details")
    public ResponseEntity<EmployeeDetailResponse> getEmployeeDetailsByCodeAlias(@PathVariable String employeeCode) {
        return ResponseEntity.ok(userService.getEmployeeDetailByCode(employeeCode));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PostMapping("/{id}/assign-device")
    public ResponseEntity<UserResponse> assignDevice(
            @PathVariable Long id,
            @RequestBody AssignDeviceRequest request) {
        return ResponseEntity.ok(userService.assignDevice(id, request));
    }

    @PostMapping("/{id}/unassign-device")
    public ResponseEntity<UserResponse> unassignDevice(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unassignDevice(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
