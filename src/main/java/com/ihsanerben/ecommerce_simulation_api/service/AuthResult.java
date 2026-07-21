package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.response.AuthResponse;

public record AuthResult(AuthResponse response, AuthTokens tokens) {
}
