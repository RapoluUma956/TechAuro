package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Accessory;
import com.assetmanagement.backend.repository.AccessoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class AccessoryService {

    private final AccessoryRepository accessoryRepository;

    public AccessoryService(AccessoryRepository accessoryRepository) {
        this.accessoryRepository = accessoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Accessory> getAllAccessories() {
        return accessoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Accessory getAccessoryById(Long id) {
        return accessoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Accessory not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Accessory> getByCategory(String category) {
        return accessoryRepository.findByCategory(category);
    }

    @Transactional
    public Accessory createAccessory(Accessory accessory) {
        return accessoryRepository.save(accessory);
    }

    @Transactional
    public Accessory updateAccessory(Long id, Accessory updated) {
        Accessory existing = getAccessoryById(id);
        existing.setCategory(updated.getCategory());
        existing.setSubCategory(updated.getSubCategory());
        existing.setBrand(updated.getBrand());
        existing.setManufacturer(updated.getManufacturer());
        existing.setModel(updated.getModel());
        existing.setProductName(updated.getProductName());
        existing.setDescription(updated.getDescription());
        existing.setSpecifications(updated.getSpecifications());
        existing.setTotalQuantity(updated.getTotalQuantity());
        existing.setAvailableQuantity(updated.getAvailableQuantity());
        existing.setDamagedQuantity(updated.getDamagedQuantity());
        return accessoryRepository.save(existing);
    }

    @Transactional
    public void deleteAccessory(Long id) {
        Accessory existing = getAccessoryById(id);
        accessoryRepository.delete(existing);
    }
}