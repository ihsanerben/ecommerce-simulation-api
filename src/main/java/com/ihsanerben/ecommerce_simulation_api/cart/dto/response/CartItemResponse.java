package com.ihsanerben.ecommerce_simulation_api.cart.dto.response;

import com.ihsanerben.ecommerce_simulation_api.catalog.dto.response.ProductResponse;

public record CartItemResponse(
        Long id,
        ProductResponse product,
        Integer quantity
) {
}
