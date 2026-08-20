package com.ihsanerben.ecommerce_simulation_api.auth.dto.internal;

import com.ihsanerben.ecommerce_simulation_api.auth.dto.response.AuthResponse;

public record AuthResult(AuthResponse response, AuthTokens tokens) {
}
