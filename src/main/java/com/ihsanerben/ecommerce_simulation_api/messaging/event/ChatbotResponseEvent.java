package com.ihsanerben.ecommerce_simulation_api.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record ChatbotResponseEvent(
        UUID conversationId,
        String message,
        Instant occurredAt
) {
}
