package com.ihsanerben.ecommerce_simulation_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email
) {
}
