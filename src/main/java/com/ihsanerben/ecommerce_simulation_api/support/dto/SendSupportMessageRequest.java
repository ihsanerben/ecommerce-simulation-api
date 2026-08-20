package com.ihsanerben.ecommerce_simulation_api.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendSupportMessageRequest(
        @NotNull Long conversationId,
        @NotBlank @Size(max = 1000) String content) {
}
