package com.ihsanerben.ecommerce_simulation_api.support.dto;

import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversationStatus;
import java.time.Instant;

public record SupportConversationResponse(
        Long id, Long clientId, String clientUsername, Long agentId, String agentUsername,
        String subject, SupportConversationStatus status, Instant createdAt) {
}
