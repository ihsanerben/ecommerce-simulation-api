package com.ihsanerben.ecommerce_simulation_api.order.dto.response;

import com.ihsanerben.ecommerce_simulation_api.catalog.dto.response.ProductResponse;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        ProductResponse product,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
