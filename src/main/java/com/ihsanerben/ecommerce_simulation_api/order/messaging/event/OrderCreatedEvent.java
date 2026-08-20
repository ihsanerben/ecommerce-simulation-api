package com.ihsanerben.ecommerce_simulation_api.order.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Long orderId,
        Long userId,
        String recipientEmail,
        BigDecimal totalAmount,
        int itemCount,
        List<OrderItemSnapshot> items,
        Instant occurredAt
) {
}
