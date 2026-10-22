package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.License;
import com.assetmanagement.backend.service.LicenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/licenses")
@CrossOrigin(origins = "*")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping
    public ResponseEntity<List<License>> getAllLicenses() {
        return ResponseEntity.ok(licenseService.getAllLicenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<License> getLicenseById(@PathVariable Long id) {
        return ResponseEntity.ok(licenseService.getLicenseById(id));
    }

    @GetMapping("/software/{softwareName}")
    public ResponseEntity<List<License>> getBySoftwareName(@PathVariable String softwareName) {
        return ResponseEntity.ok(licenseService.getBySoftwareName(softwareName));
    }

    @PostMapping
    public ResponseEntity<License> createLicense(@RequestBody License license) {
        return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.createLicense(license));
    }

    @PutMapping("/{id}")
    public ResponseEntity<License> updateLicense(@PathVariable Long id, @RequestBody License license) {
        return ResponseEntity.ok(licenseService.updateLicense(id, license));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLicense(@PathVariable Long id) {
        licenseService.deleteLicense(id);
        return ResponseEntity.noContent().build();
    }
}