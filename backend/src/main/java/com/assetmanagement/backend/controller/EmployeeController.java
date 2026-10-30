package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.employee.EmployeeDtos.*;
import com.assetmanagement.backend.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/employees", "/api/employees"})
@CrossOrigin(originPatterns = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 1. Employee Dashboard Metrics
    @GetMapping("/metrics")
    public ResponseEntity<EmployeeMetricsResponse> getMetrics() {
        return ResponseEntity.ok(employeeService.getEmployeeMetrics());
    }

    // 2. Employee List with pagination, search, and filtering
    @GetMapping
    public ResponseEntity<EmployeeListResponse> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(employeeService.getEmployees(search, status, role, page, size, sortBy, sortDir));
    }

    // 3. Available Devices for assignment
    @GetMapping("/available-devices")
    public ResponseEntity<AvailableDevicesResponse> getAvailableDevices() {
        return ResponseEntity.ok(employeeService.getAvailableDevices());
    }

    // 4. Create Employee
    @PostMapping
    public ResponseEntity<CreateEmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    // 5. Employee Profile Details
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeProfileResponse> getEmployeeProfile(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeProfile(id));
    }

    // 6. Device Details Tab
    @GetMapping("/{id}/device")
    public ResponseEntity<EmployeeDeviceDetailsResponse> getEmployeeDevice(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeDevice(id));
    }

    // 7. Employee Accessories Tab
    @GetMapping("/{id}/accessories")
    public ResponseEntity<EmployeeAccessoriesResponse> getEmployeeAccessories(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(employeeService.getEmployeeAccessories(id, search, type, status, page, size));
    }

    // 8. Employee Software Tab
    @GetMapping("/{id}/software")
    public ResponseEntity<EmployeeSoftwareResponse> getEmployeeSoftware(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String updateStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {
        return ResponseEntity.ok(employeeService.getEmployeeSoftware(id, search, publisher, updateStatus, page, size));
    }

    // 9. Employee Vulnerabilities Tab
    @GetMapping("/{id}/vulnerabilities")
    public ResponseEntity<EmployeeVulnerabilitiesResponse> getEmployeeVulnerabilities(
            @PathVariable Long id,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(employeeService.getEmployeeVulnerabilities(id, severity, priority, status, search, page, size));
    }

    // 10. Employee Licenses Tab
    @GetMapping("/{id}/licenses")
    public ResponseEntity<EmployeeLicensesResponse> getEmployeeLicenses(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(employeeService.getEmployeeLicenses(id, search, status, page, size));
    }

    // 11. Reassign / Change Device
    @PutMapping("/{id}/device")
    public ResponseEntity<ReassignDeviceResponse> reassignDevice(
            @PathVariable Long id,
            @RequestBody ReassignDeviceRequest request) {
        return ResponseEntity.ok(employeeService.reassignDevice(id, request));
    }

    // 12. Unassign Device
    @DeleteMapping("/{id}/device")
    public ResponseEntity<UnassignDeviceResponse> unassignDevice(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.unassignDevice(id));
    }
}

