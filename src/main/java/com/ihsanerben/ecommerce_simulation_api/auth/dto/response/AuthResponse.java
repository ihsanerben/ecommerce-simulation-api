package com.ihsanerben.ecommerce_simulation_api.auth.dto.response;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;

public record AuthResponse(String username, Role role) {
}
