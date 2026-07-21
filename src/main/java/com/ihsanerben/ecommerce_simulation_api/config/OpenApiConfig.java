package com.ihsanerben.ecommerce_simulation_api.config;

import com.ihsanerben.ecommerce_simulation_api.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "E-Commerce Simulation API",
                version = "1.0",
                description = "JWT authentication uses browser-managed HttpOnly cookies. In Swagger UI, first execute "
                        + "GET /api/auth/csrf once, then register or log in. Swagger automatically copies the standard "
                        + "ECOMMERCE-XSRF-TOKEN cookie into the X-XSRF-TOKEN header; the browser automatically sends authentication cookies. "
                        + "The Authorize button is only a compatibility fallback for manually generated Bearer access tokens."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Authentication is normally handled by HttpOnly cookies. Bearer tokens remain supported for API tooling."
)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer standardErrorResponses() {
        return openApi -> {
            ModelConverters.getInstance().read(ErrorResponse.class)
                    .forEach(openApi.getComponents()::addSchemas);

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        operation.getResponses().forEach((code, response) -> {
                            if (!code.startsWith("2") && response.getContent() == null) {
                                response.setContent(errorContent());
                            }
                        });

                        if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                            operation.getResponses().addApiResponse("401",
                                    errorResponse("Authentication cookie is missing, expired, or revoked"));
                            operation.getResponses().addApiResponse("403",
                                    errorResponse("The authenticated user is not allowed to perform this action"));
                        }

                        operation.getResponses().addApiResponse("500",
                                errorResponse("An unexpected server error occurred"));
                    }));
        };
    }

    private ApiResponse errorResponse(String description) {
        return new ApiResponse().description(description).content(errorContent());
    }

    @SuppressWarnings("rawtypes")
    private Content errorContent() {
        Schema schema = new Schema<>().$ref("#/components/schemas/ErrorResponse");
        return new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType().schema(schema));
    }
}
