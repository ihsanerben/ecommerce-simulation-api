package com.ihsanerben.ecommerce_simulation_api.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotRequest(
        @NotBlank(message = "Message is required.")
        @Size(max = 500, message = "Message must not exceed 500 characters.")
        String message
) {
}
