package com.assetmanagement.backend.dto.accessory;

import java.util.List;

public class CategoryOptionDto {

    private String category;
    private List<String> subCategories;

    public CategoryOptionDto() {}

    public CategoryOptionDto(String category, List<String> subCategories) {
        this.category = category;
        this.subCategories = subCategories;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<String> subCategories) {
        this.subCategories = subCategories;
    }
}
