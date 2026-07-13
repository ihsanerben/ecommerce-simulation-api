package com.ihsanerben.ecommerce_simulation_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "E-Commerce Simulation API",
                version = "1.0",
                description = "An e-commerce simulation API built with a layered architecture, "
                        + "JWT-based authentication, and a transactional order checkout flow."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "You don't need to prefix the token you get from POST /api/auth/login with 'Bearer <token>' here, just enter the token itself."
)
public class OpenApiConfig {
}
