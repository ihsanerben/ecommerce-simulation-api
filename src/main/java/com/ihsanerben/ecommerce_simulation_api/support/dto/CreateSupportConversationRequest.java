package com.ihsanerben.ecommerce_simulation_api.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportConversationRequest(
        @NotBlank @Size(max = 120) String subject) {
}
