package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.license.*;
import com.assetmanagement.backend.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/licenses")
@CrossOrigin(originPatterns = "*")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    // ==================== 1. SUMMARY METRICS ====================
    @GetMapping("/summary")
    public ResponseEntity<LicenseSummaryResponse> getSummary() {
        return ResponseEntity.ok(licenseService.getSummary());
    }

    // ==================== 2. FILTER DROPDOWNS ====================
    @GetMapping("/filter-options")
    public ResponseEntity<Map<String, List<String>>> getFilterOptions() {
        return ResponseEntity.ok(licenseService.getFilterOptions());
    }

    // ==================== 3. GET LICENSES (PAGINATED & FILTERED) ====================
    @GetMapping
    public ResponseEntity<LicensePageResponse> getLicenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String software,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String expiry
    ) {
        return ResponseEntity.ok(licenseService.getLicensesPage(page, size, search, software, publisher, status, expiry));
    }

    // ==================== 4. GET LICENSE BY ID (WITH ASSIGNMENTS) ====================
    @GetMapping("/{licenseId}")
    public ResponseEntity<LicenseDetailResponse> getLicenseById(@PathVariable Long licenseId) {
        return ResponseEntity.ok(licenseService.getLicenseDetail(licenseId));
    }

    // ==================== 5. CREATE LICENSE ====================
    @PostMapping
    public ResponseEntity<LicenseItemDto> createLicense(@Valid @RequestBody CreateLicenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.createLicense(request));
    }

    // ==================== 6. UPDATE LICENSE ====================
    @PutMapping("/{licenseId}")
    public ResponseEntity<LicenseItemDto> updateLicense(
            @PathVariable Long licenseId,
            @Valid @RequestBody UpdateLicenseRequest request
    ) {
        return ResponseEntity.ok(licenseService.updateLicense(licenseId, request));
    }

    // ==================== 7. DELETE LICENSE ====================
    @DeleteMapping("/{licenseId}")
    public ResponseEntity<Void> deleteLicense(@PathVariable Long licenseId) {
        licenseService.deleteLicense(licenseId);
        return ResponseEntity.noContent().build();
    }

    // ==================== 8. ASSIGN LICENSE TO EMPLOYEE ====================
    @PostMapping("/{licenseId}/assign")
    public ResponseEntity<LicenseAssignmentDto> assignLicense(
            @PathVariable Long licenseId,
            @Valid @RequestBody AssignLicenseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.assignLicense(licenseId, request));
    }

    // ==================== 9. RELEASE / RETURN LICENSE ====================
    @PostMapping("/{licenseId}/release")
    public ResponseEntity<LicenseAssignmentDto> releaseLicense(
            @PathVariable Long licenseId,
            @Valid @RequestBody ReleaseLicenseRequest request
    ) {
        return ResponseEntity.ok(licenseService.releaseLicense(licenseId, request));
    }
}