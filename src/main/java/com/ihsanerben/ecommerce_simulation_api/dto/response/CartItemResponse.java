package com.ihsanerben.ecommerce_simulation_api.dto.response;

public record CartItemResponse(
        Long id,
        ProductResponse product,
        Integer quantity
) {
}
