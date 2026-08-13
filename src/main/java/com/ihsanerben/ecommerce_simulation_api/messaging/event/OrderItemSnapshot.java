package com.ihsanerben.ecommerce_simulation_api.messaging.event;

import java.math.BigDecimal;

public record OrderItemSnapshot(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        int remainingStock
) {
}
