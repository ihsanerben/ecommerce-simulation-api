package com.ihsanerben.ecommerce_simulation_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "E-Commerce Simulation API",
                version = "1.0",
                description = "Katmanlı mimari, JWT tabanlı kimlik doğrulama ve "
                        + "transactional sipariş akışı ile geliştirilmiş bir e-ticaret simülasyon API'si."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "POST /api/auth/login ile aldığınız token'ı buraya 'Bearer <token>' önekiyle yapıştırmanıza gerek yok, sadece token'ın kendisini girin."
)
public class OpenApiConfig {
}
