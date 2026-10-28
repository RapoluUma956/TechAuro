package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.license.*;
import com.assetmanagement.backend.entity.License;
import com.assetmanagement.backend.entity.LicenseAssignment;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.repository.LicenseAssignmentRepository;
import com.assetmanagement.backend.repository.LicenseRepository;
import com.assetmanagement.backend.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseAssignmentRepository licenseAssignmentRepository;
    private final UserRepository userRepository;

    public LicenseService(
            LicenseRepository licenseRepository,
            LicenseAssignmentRepository licenseAssignmentRepository,
            UserRepository userRepository
    ) {
        this.licenseRepository = licenseRepository;
        this.licenseAssignmentRepository = licenseAssignmentRepository;
        this.userRepository = userRepository;
    }

    // ==================== 1. SUMMARY METRICS ====================
    @Transactional(readOnly = true)
    public LicenseSummaryResponse getSummary() {
        long totalLicenses = licenseRepository.count();
        Long totalSeatsLong = licenseRepository.sumTotalSeats();
        long totalSeats = totalSeatsLong != null ? totalSeatsLong : 0L;
        long assignedSeats = licenseAssignmentRepository.countByStatus("ACTIVE");
        long availableSeats = Math.max(0L, totalSeats - assignedSeats);

        LocalDate today = LocalDate.now();
        LocalDate soonDate = today.plusDays(30);
        long expiringSoonCount = licenseRepository.countExpiringSoon(today, soonDate);

        return new LicenseSummaryResponse(
                totalLicenses,
                totalSeats,
                assignedSeats,
                availableSeats,
                expiringSoonCount
        );
    }

    // ==================== 2. PAGINATED SEARCH & FILTER ====================
    @Transactional(readOnly = true)
    public LicensePageResponse getLicensesPage(
            int page,
            int size,
            String search,
            String software,
            String publisher,
            String status,
            String expiry
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt", "licenseId"));

        Specification<License> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search query across licenseName, softwareName, and publisher
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("licenseName")), pattern);
                Predicate softwareMatch = cb.like(cb.lower(root.get("softwareName")), pattern);
                Predicate publisherMatch = cb.like(cb.lower(root.get("publisher")), pattern);
                predicates.add(cb.or(nameMatch, softwareMatch, publisherMatch));
            }

            // Software filter
            if (software != null && !software.trim().isEmpty() && !"ALL".equalsIgnoreCase(software.trim())) {
                predicates.add(cb.equal(cb.lower(root.get("softwareName")), software.trim().toLowerCase()));
            }

            // Publisher filter
            if (publisher != null && !publisher.trim().isEmpty() && !"ALL".equalsIgnoreCase(publisher.trim())) {
                predicates.add(cb.equal(cb.lower(root.get("publisher")), publisher.trim().toLowerCase()));
            }

            // Status filter
            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            // Expiry filter
            if (expiry != null && !expiry.trim().isEmpty() && !"ALL".equalsIgnoreCase(expiry.trim())) {
                LocalDate now = LocalDate.now();
                String exp = expiry.trim().toUpperCase();
                if ("EXPIRING_SOON".equals(exp) || "30".equals(exp) || exp.contains("SOON")) {
                    predicates.add(cb.isNotNull(root.get("expiryDate")));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("expiryDate"), now));
                    predicates.add(cb.lessThanOrEqualTo(root.get("expiryDate"), now.plusDays(30)));
                    predicates.add(cb.notEqual(cb.upper(root.get("status")), "EXPIRED"));
                } else if ("EXPIRED".equals(exp)) {
                    Predicate pastDate = cb.and(cb.isNotNull(root.get("expiryDate")), cb.lessThan(root.get("expiryDate"), now));
                    Predicate expiredStatus = cb.equal(cb.upper(root.get("status")), "EXPIRED");
                    predicates.add(cb.or(pastDate, expiredStatus));
                } else if ("ACTIVE".equals(exp)) {
                    Predicate futureDate = cb.or(cb.isNull(root.get("expiryDate")), cb.greaterThanOrEqualTo(root.get("expiryDate"), now));
                    Predicate activeStatus = cb.equal(cb.upper(root.get("status")), "ACTIVE");
                    predicates.add(cb.and(futureDate, activeStatus));
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<License> pageResult = licenseRepository.findAll(spec, pageable);

        List<LicenseItemDto> content = pageResult.getContent().stream()
                .map(this::toItemDto)
                .toList();

        return new LicensePageResponse(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    // ==================== 3. GET LICENSE BY ID WITH ASSIGNMENTS ====================
    @Transactional(readOnly = true)
    public LicenseDetailResponse getLicenseDetail(Long id) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License not found with id: " + id));

        int assignedQuantity = (int) licenseAssignmentRepository.countByLicense_LicenseIdAndStatus(id, "ACTIVE");
        int totalQuantity = license.getTotalQuantity() != null ? license.getTotalQuantity() : 0;
        int availableQuantity = Math.max(0, totalQuantity - assignedQuantity);

        List<LicenseAssignment> assignments = licenseAssignmentRepository.findByLicense_LicenseIdOrderByAssignedAtDesc(id);
        List<LicenseAssignmentDto> assignmentDtos = assignments.stream()
                .map(this::toAssignmentDto)
                .toList();

        return new LicenseDetailResponse(
                license.getLicenseId(),
                license.getLicenseName(),
                license.getSoftwareName(),
                license.getPublisher(),
                license.getLicenseKey(),
                license.getLicenseType(),
                license.getRenewalType(),
                license.getRenewalMethod(),
                totalQuantity,
                assignedQuantity,
                availableQuantity,
                license.getPurchaseDate(),
                license.getStartDate(),
                license.getExpiryDate(),
                license.getStatus(),
                license.getNotes(),
                license.getCreatedAt(),
                license.getUpdatedAt(),
                assignmentDtos
        );
    }

    // ==================== 4. CREATE LICENSE ====================
    @Transactional
    public LicenseItemDto createLicense(CreateLicenseRequest request) {
        if (request.totalQuantity() == null || request.totalQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total quantity must be a positive integer.");
        }

        if (request.startDate() != null && request.expiryDate() != null && request.expiryDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be earlier than start date.");
        }

        License license = new License();
        license.setLicenseName(request.licenseName().trim());
        license.setSoftwareName(request.softwareName().trim());
        license.setPublisher(request.publisher().trim());
        license.setLicenseKey(request.licenseKey() != null && !request.licenseKey().isBlank() ? request.licenseKey().trim() : null);
        license.setLicenseType(request.licenseType() != null ? request.licenseType().trim() : "SUBSCRIPTION");
        license.setRenewalType(request.renewalType() != null && !request.renewalType().isBlank() ? request.renewalType().trim() : "YEARLY");
        license.setRenewalMethod(request.renewalMethod() != null && !request.renewalMethod().isBlank() ? request.renewalMethod().trim() : "AUTO");
        license.setTotalQuantity(request.totalQuantity());
        license.setTotalLicenses(request.totalQuantity());
        license.setAvailableQuantity(request.totalQuantity());
        license.setPurchaseDate(request.purchaseDate() != null ? request.purchaseDate() : LocalDate.now());
        license.setStartDate(request.startDate());
        license.setExpiryDate(request.expiryDate());
        license.setStatus(request.status() != null && !request.status().isBlank() ? request.status().trim() : "ACTIVE");
        license.setNotes(request.notes());

        License saved = licenseRepository.save(license);
        return toItemDto(saved, 0);
    }

    // ==================== 5. UPDATE LICENSE ====================
    @Transactional
    public LicenseItemDto updateLicense(Long id, UpdateLicenseRequest request) {
        License existing = licenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License not found with id: " + id));

        int activeAssignments = (int) licenseAssignmentRepository.countByLicense_LicenseIdAndStatus(id, "ACTIVE");

        if (request.totalQuantity() == null || request.totalQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total quantity must be a positive integer.");
        }

        if (request.totalQuantity() < activeAssignments) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total quantity (" + request.totalQuantity() + ") cannot be less than active assignments (" + activeAssignments + ")."
            );
        }

        if (request.startDate() != null && request.expiryDate() != null && request.expiryDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be earlier than start date.");
        }

        existing.setLicenseName(request.licenseName().trim());
        existing.setSoftwareName(request.softwareName().trim());
        existing.setPublisher(request.publisher().trim());

        // Do not overwrite existing licenseKey if empty/null in update
        if (request.licenseKey() != null && !request.licenseKey().isBlank()) {
            existing.setLicenseKey(request.licenseKey().trim());
        }

        if (request.licenseType() != null && !request.licenseType().isBlank()) {
            existing.setLicenseType(request.licenseType().trim());
        }
        if (request.renewalType() != null && !request.renewalType().isBlank()) {
            existing.setRenewalType(request.renewalType().trim());
        }
        if (request.renewalMethod() != null && !request.renewalMethod().isBlank()) {
            existing.setRenewalMethod(request.renewalMethod().trim());
        }

        existing.setTotalQuantity(request.totalQuantity());
        existing.setTotalLicenses(request.totalQuantity());
        existing.setAvailableQuantity(Math.max(0, request.totalQuantity() - activeAssignments));
        existing.setPurchaseDate(request.purchaseDate());
        existing.setStartDate(request.startDate());
        existing.setExpiryDate(request.expiryDate());

        if (request.status() != null && !request.status().isBlank()) {
            existing.setStatus(request.status().trim());
        }
        existing.setNotes(request.notes());

        License saved = licenseRepository.save(existing);
        return toItemDto(saved, activeAssignments);
    }

    // ==================== 6. DELETE LICENSE ====================
    @Transactional
    public void deleteLicense(Long id) {
        License existing = licenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License not found with id: " + id));

        long activeAssignments = licenseAssignmentRepository.countByLicense_LicenseIdAndStatus(id, "ACTIVE");
        if (activeAssignments > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot delete license '" + existing.getLicenseName() + "' because it has " + activeAssignments + " active assignment(s). Please release all seats before deleting."
            );
        }

        // Clean up any historical/returned assignments
        List<LicenseAssignment> assignments = licenseAssignmentRepository.findByLicense_LicenseId(id);
        if (!assignments.isEmpty()) {
            licenseAssignmentRepository.deleteAll(assignments);
        }

        licenseRepository.delete(existing);
    }

    // ==================== 7. ASSIGN LICENSE ====================
    @Transactional
    public LicenseAssignmentDto assignLicense(Long licenseId, AssignLicenseRequest request) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License not found with id: " + licenseId));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with id: " + request.userId()));

        int totalQuantity = license.getTotalQuantity() != null ? license.getTotalQuantity() : 0;
        int activeAssignments = (int) licenseAssignmentRepository.countByLicense_LicenseIdAndStatus(licenseId, "ACTIVE");
        int available = Math.max(0, totalQuantity - activeAssignments);

        if (available <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No seats available for license '" + license.getLicenseName() + "'. Total seats: " + totalQuantity + ", Active: " + activeAssignments
            );
        }

        // Check if employee already has active assignment
        boolean alreadyAssigned = licenseAssignmentRepository.existsByLicense_LicenseIdAndUser_UserIdAndStatus(licenseId, request.userId(), "ACTIVE");
        if (alreadyAssigned) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee " + user.getFirstName() + " " + user.getLastName() + " is already assigned an active seat for this license."
            );
        }

        LicenseAssignment assignment = new LicenseAssignment();
        assignment.setLicense(license);
        assignment.setUser(user);
        assignment.setAssignedAt(request.assignedAt() != null ? request.assignedAt().atStartOfDay() : LocalDateTime.now());
        assignment.setStatus("ACTIVE");
        assignment.setNotes(request.notes());

        LicenseAssignment savedAssignment = licenseAssignmentRepository.save(assignment);

        // Update license available quantity cache
        license.setAvailableQuantity(Math.max(0, totalQuantity - (activeAssignments + 1)));
        licenseRepository.save(license);

        return toAssignmentDto(savedAssignment);
    }

    // ==================== 8. RELEASE / RETURN LICENSE ====================
    @Transactional
    public LicenseAssignmentDto releaseLicense(Long licenseId, ReleaseLicenseRequest request) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License not found with id: " + licenseId));

        LicenseAssignment assignment = licenseAssignmentRepository.findByAssignmentIdAndLicense_LicenseId(request.assignmentId(), licenseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id: " + request.assignmentId() + " for this license"));

        if (!"ACTIVE".equalsIgnoreCase(assignment.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assignment is already marked as returned/released."
            );
        }

        assignment.setStatus("RETURNED");
        assignment.setReturnedAt(request.returnedAt() != null ? request.returnedAt().atStartOfDay() : LocalDateTime.now());
        LicenseAssignment savedAssignment = licenseAssignmentRepository.save(assignment);

        int totalQuantity = license.getTotalQuantity() != null ? license.getTotalQuantity() : 0;
        int activeAssignments = (int) licenseAssignmentRepository.countByLicense_LicenseIdAndStatus(licenseId, "ACTIVE");
        license.setAvailableQuantity(Math.max(0, totalQuantity - activeAssignments));
        licenseRepository.save(license);

        return toAssignmentDto(savedAssignment);
    }

    // ==================== 9. FILTER DROPDOWN OPTIONS ====================
    @Transactional(readOnly = true)
    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> options = new HashMap<>();
        options.put("softwares", licenseRepository.findDistinctSoftwareNames());
        options.put("publishers", licenseRepository.findDistinctPublishers());
        return options;
    }

    // ==================== BACKWARD COMPATIBLE METHODS ====================
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

    // ==================== HELPER MAPPERS ====================
    private LicenseItemDto toItemDto(License l) {
        int active = (int) licenseAssignmentRepository.countByLicense_LicenseIdAndStatus(l.getLicenseId(), "ACTIVE");
        return toItemDto(l, active);
    }

    private LicenseItemDto toItemDto(License l, int assignedCount) {
        int total = l.getTotalQuantity() != null ? l.getTotalQuantity() : 0;
        int avail = Math.max(0, total - assignedCount);

        return new LicenseItemDto(
                l.getLicenseId(),
                l.getLicenseName(),
                l.getSoftwareName(),
                l.getPublisher(),
                l.getLicenseKey(),
                l.getLicenseType() != null ? l.getLicenseType() : "SUBSCRIPTION",
                l.getRenewalType() != null ? l.getRenewalType() : "YEARLY",
                l.getRenewalMethod() != null ? l.getRenewalMethod() : "AUTO",
                total,
                assignedCount,
                avail,
                l.getPurchaseDate(),
                l.getStartDate(),
                l.getExpiryDate(),
                l.getStatus() != null ? l.getStatus() : "ACTIVE",
                l.getNotes(),
                l.getCreatedAt(),
                l.getUpdatedAt()
        );
    }

    private LicenseAssignmentDto toAssignmentDto(LicenseAssignment a) {
        User u = a.getUser();
        String name = u != null ? ((u.getFirstName() != null ? u.getFirstName() : "") + " " + (u.getLastName() != null ? u.getLastName() : "")).trim() : "Unknown";
        String code = u != null ? u.getEmployeeCode() : "";

        return new LicenseAssignmentDto(
                a.getAssignmentId(),
                a.getLicense() != null ? a.getLicense().getLicenseId() : null,
                u != null ? u.getUserId() : null,
                name,
                code,
                a.getAssignedAt(),
                a.getReturnedAt(),
                a.getStatus(),
                a.getNotes()
        );
    }
}