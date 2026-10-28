package com.assetmanagement.backend.dto.accessory;

import java.util.List;

public class AccessoryPageResponse {

    private List<AccessoryItemDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public AccessoryPageResponse() {}

    public AccessoryPageResponse(List<AccessoryItemDto> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<AccessoryItemDto> getContent() {
        return content;
    }

    public void setContent(List<AccessoryItemDto> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
