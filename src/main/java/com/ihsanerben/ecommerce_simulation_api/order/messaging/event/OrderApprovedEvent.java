package com.ihsanerben.ecommerce_simulation_api.order.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record OrderApprovedEvent(
        UUID eventId,
        Long orderId,
        Long userId,
        String recipientEmail,
        Instant occurredAt
) {
}
