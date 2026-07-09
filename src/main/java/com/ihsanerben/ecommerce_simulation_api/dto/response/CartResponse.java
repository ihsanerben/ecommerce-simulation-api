package com.ihsanerben.ecommerce_simulation_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        BigDecimal totalPrice
) {
}
