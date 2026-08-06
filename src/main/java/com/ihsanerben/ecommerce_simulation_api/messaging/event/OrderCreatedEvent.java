package com.ihsanerben.ecommerce_simulation_api.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        int itemCount,
        Instant occurredAt
) {
}
