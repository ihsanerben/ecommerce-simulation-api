package com.ihsanerben.ecommerce_simulation_api.chatbot.dto;

public record ChatbotReply(
        String message,
        String category,
        boolean matched
) {
}
