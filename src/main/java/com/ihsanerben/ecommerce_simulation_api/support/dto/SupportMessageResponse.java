package com.ihsanerben.ecommerce_simulation_api.support.dto;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;
import java.time.Instant;

public record SupportMessageResponse(
        Long id, Long conversationId, Long senderId, String senderUsername,
        Role senderRole, String content, Instant sentAt) {
}
