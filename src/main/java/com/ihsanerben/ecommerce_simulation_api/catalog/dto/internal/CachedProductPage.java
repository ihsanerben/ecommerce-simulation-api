package com.ihsanerben.ecommerce_simulation_api.catalog.dto.internal;

import com.ihsanerben.ecommerce_simulation_api.catalog.dto.response.ProductResponse;

import java.util.List;

public record CachedProductPage(
        List<ProductResponse> content,
        long totalElements
) {
}
