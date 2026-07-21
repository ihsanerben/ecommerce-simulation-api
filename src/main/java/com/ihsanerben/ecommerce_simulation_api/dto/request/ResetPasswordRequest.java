package com.ihsanerben.ecommerce_simulation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Reset token is required") String token,
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters") String newPassword
) {
}
