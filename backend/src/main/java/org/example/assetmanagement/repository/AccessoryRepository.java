package org.example.assetmanagement.repository;

import org.example.assetmanagement.entity.*;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface AccessoryRepository
        extends JpaRepository<Accessory, Long> {

    List<Accessory> findByCategory(String category);

    List<Accessory> findBySubCategory(String subCategory);

    List<Accessory> findByBrand(String brand);
}