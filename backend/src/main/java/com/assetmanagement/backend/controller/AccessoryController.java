package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.accessory.*;
import com.assetmanagement.backend.entity.Accessory;
import com.assetmanagement.backend.service.AccessoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/accessories", "/api/accessories"})
@CrossOrigin(originPatterns = "*")
public class AccessoryController {

    private final AccessoryService accessoryService;

    public AccessoryController(AccessoryService accessoryService) {
        this.accessoryService = accessoryService;
    }

    // ==================== DASHBOARD & METRICS ====================

    // GET /api/accessories/summary
    @GetMapping("/summary")
    public ResponseEntity<AccessorySummaryResponse> getSummary() {
        return ResponseEntity.ok(accessoryService.getSummary());
    }

    // GET /api/accessories/categories
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryOptionDto>> getCategories() {
        return ResponseEntity.ok(accessoryService.getCategories());
    }

    // ==================== LAPTOP INVENTORY APIS ====================

    // GET /api/accessories/laptops/summary
    @GetMapping("/laptops/summary")
    public ResponseEntity<LaptopSummaryResponse> getLaptopSummary() {
        return ResponseEntity.ok(accessoryService.getLaptopSummary());
    }

    // GET /api/accessories/laptops/configurations
    @GetMapping("/laptops/configurations")
    public ResponseEntity<LaptopConfigurationPageResponse> getLaptopConfigurations(
            @RequestParam(required = false, defaultValue = "") String brand,
            @RequestParam(required = false, defaultValue = "") String ram,
            @RequestParam(required = false, defaultValue = "") String ssd,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok(accessoryService.getLaptopConfigurations(brand, ram, ssd, search, page, size));
    }

    // POST /api/accessories/laptops/bulk
    @PostMapping("/laptops/bulk")
    public ResponseEntity<BulkAddLaptopResponse> bulkAddLaptops(@RequestBody BulkAddLaptopRequest request) {
        BulkAddLaptopResponse response = accessoryService.bulkAddLaptops(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/accessories/laptops/{deviceId}
    @GetMapping("/laptops/{deviceId}")
    public ResponseEntity<LaptopDeviceItemDto> getLaptopById(@PathVariable Long deviceId) {
        return ResponseEntity.ok(accessoryService.getLaptopById(deviceId));
    }

    // ==================== ORDINARY ACCESSORIES APIS ====================

    // GET /api/accessories (Paginated & Filterable)
    @GetMapping
    public ResponseEntity<AccessoryPageResponse> getAccessories(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String category) {
        return ResponseEntity.ok(accessoryService.getAccessoriesPage(page, size, search, category));
    }

    // POST /api/accessories (Add / Merge)
    @PostMapping
    public ResponseEntity<CreateAccessoryResponse> createAccessory(@RequestBody CreateAccessoryRequest request) {
        CreateAccessoryResponse response = accessoryService.createOrAddAccessory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/accessories/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Accessory> getAccessoryById(@PathVariable Long id) {
        return ResponseEntity.ok(accessoryService.getAccessoryById(id));
    }

    // PUT /api/accessories/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Accessory> updateAccessory(@PathVariable Long id, @RequestBody Accessory accessory) {
        return ResponseEntity.ok(accessoryService.updateAccessory(id, accessory));
    }

    // POST /api/accessories/{id}/assign
    @PostMapping("/{id}/assign")
    public ResponseEntity<AssignAccessoryResponse> assignAccessory(
            @PathVariable Long id,
            @RequestBody AssignAccessoryRequest request) {
        AssignAccessoryResponse response = accessoryService.assignAccessory(id, request);
        return ResponseEntity.ok(response);
    }

    // POST /api/accessories/laptops/{deviceId}/assign
    @PostMapping("/laptops/{deviceId}/assign")
    public ResponseEntity<AssignAccessoryResponse> assignLaptop(
            @PathVariable Long deviceId,
            @RequestBody AssignAccessoryRequest request) {
        AssignAccessoryResponse response = accessoryService.assignLaptop(deviceId, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/accessories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccessory(@PathVariable Long id) {
        accessoryService.deleteAccessory(id);
        return ResponseEntity.noContent().build();
    }
}