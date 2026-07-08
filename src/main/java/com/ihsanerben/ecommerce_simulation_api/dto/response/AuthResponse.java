package com.ihsanerben.ecommerce_simulation_api.dto.response;

import com.ihsanerben.ecommerce_simulation_api.entity.Role;

public record AuthResponse(
        String token,
        String username,
        Role role
) {
}
