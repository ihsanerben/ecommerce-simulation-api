package com.ihsanerben.ecommerce_simulation_api.catalog.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class CacheKeyFactory {

    public String productSearch(Long categoryId, String search, Pageable pageable) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase();
        String normalizedCategory = categoryId == null ? "all" : categoryId.toString();
        return "%s|%s|%d|%d|%s".formatted(
                normalizedCategory,
                normalizedSearch,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
    }
}
