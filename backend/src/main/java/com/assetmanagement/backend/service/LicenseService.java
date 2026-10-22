package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.License;
import com.assetmanagement.backend.repository.LicenseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;

    public LicenseService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    @Transactional(readOnly = true)
    public List<License> getAllLicenses() {
        return licenseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public License getLicenseById(Long id) {
        return licenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<License> getBySoftwareName(String softwareName) {
        return licenseRepository.findBySoftwareName(softwareName);
    }

    @Transactional
    public License createLicense(License license) {
        return licenseRepository.save(license);
    }

    @Transactional
    public License updateLicense(Long id, License updated) {
        License existing = getLicenseById(id);
        existing.setLicenseName(updated.getLicenseName());
        existing.setSoftwareName(updated.getSoftwareName());
        existing.setPublisher(updated.getPublisher());
        existing.setLicenseKey(updated.getLicenseKey());
        existing.setLicenseType(updated.getLicenseType());
        existing.setTotalQuantity(updated.getTotalQuantity());
        existing.setAvailableQuantity(updated.getAvailableQuantity());
        existing.setPurchaseDate(updated.getPurchaseDate());
        existing.setStartDate(updated.getStartDate());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setWarrantyEndDate(updated.getWarrantyEndDate());
        existing.setStatus(updated.getStatus());
        existing.setNotes(updated.getNotes());
        return licenseRepository.save(existing);
    }

    @Transactional
    public void deleteLicense(Long id) {
        License existing = getLicenseById(id);
        licenseRepository.delete(existing);
    }
}