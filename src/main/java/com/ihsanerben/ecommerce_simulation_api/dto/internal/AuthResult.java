package com.ihsanerben.ecommerce_simulation_api.dto.internal;

import com.ihsanerben.ecommerce_simulation_api.dto.response.AuthResponse;

public record AuthResult(AuthResponse response, AuthTokens tokens) {
}
