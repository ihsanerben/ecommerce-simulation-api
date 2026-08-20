package com.ihsanerben.ecommerce_simulation_api.chatbot.messaging;

import java.time.Instant;
import java.util.UUID;

public record ChatbotInteractionEvent(
        UUID eventId,
        String question,
        String category,
        boolean matched,
        Instant occurredAt
) {
}
