package com.ihsanerben.ecommerce_simulation_api.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID eventId,
        Long orderId,
        Long userId,
        String recipientEmail,
        Instant occurredAt
) {
}
