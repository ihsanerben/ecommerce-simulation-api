package com.ihsanerben.ecommerce_simulation_api.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        ProductResponse product,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
