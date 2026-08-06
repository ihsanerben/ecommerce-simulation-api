package com.ihsanerben.ecommerce_simulation_api.websocket;

import java.time.Instant;

public record LiveNotification(
        String type,
        String message,
        Instant occurredAt
) {
}
