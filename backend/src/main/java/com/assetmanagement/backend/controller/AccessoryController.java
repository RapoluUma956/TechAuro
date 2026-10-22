package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.Accessory;
import com.assetmanagement.backend.service.AccessoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accessories")
@CrossOrigin(origins = "*")
public class AccessoryController {

    private final AccessoryService accessoryService;

    public AccessoryController(AccessoryService accessoryService) {
        this.accessoryService = accessoryService;
    }

    @GetMapping
    public ResponseEntity<List<Accessory>> getAllAccessories() {
        return ResponseEntity.ok(accessoryService.getAllAccessories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Accessory> getAccessoryById(@PathVariable Long id) {
        return ResponseEntity.ok(accessoryService.getAccessoryById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Accessory>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(accessoryService.getByCategory(category));
    }

    @PostMapping
    public ResponseEntity<Accessory> createAccessory(@RequestBody Accessory accessory) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accessoryService.createAccessory(accessory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Accessory> updateAccessory(@PathVariable Long id, @RequestBody Accessory accessory) {
        return ResponseEntity.ok(accessoryService.updateAccessory(id, accessory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccessory(@PathVariable Long id) {
        accessoryService.deleteAccessory(id);
        return ResponseEntity.noContent().build();
    }
}