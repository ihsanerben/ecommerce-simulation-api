package com.ihsanerben.ecommerce_simulation_api.chatbot.dto;

import java.util.UUID;

public record ChatbotResponse(
        UUID conversationId,
        String message
) {
}
