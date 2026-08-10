package com.ihsanerben.ecommerce_simulation_api.messaging.event;

import java.math.BigDecimal;

public record OrderItemSnapshot(
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
