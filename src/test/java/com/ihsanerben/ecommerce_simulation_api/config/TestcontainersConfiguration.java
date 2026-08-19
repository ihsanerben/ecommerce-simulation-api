package com.ihsanerben.ecommerce_simulation_api.config;

import com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageDocument;
import com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
    }

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:8.0"));
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.9.1"));
    }

    @Bean
    ApplicationRunner errorMessageTestData(ErrorMessageRepository repository) {
        List<ErrorMessageDocument> messages = List.of(
                message("RESOURCE_NOT_FOUND", "The requested resource was not found."),
                message("DUPLICATE_RESOURCE", "The resource already exists."),
                message("INSUFFICIENT_STOCK", "There is not enough stock for this product."),
                message("EMPTY_CART", "Cart is empty, cannot checkout."),
                message("INVALID_ORDER_STATE", "The order is not in a valid state for this operation."),
                message("INVALID_TOKEN", "The token is invalid or expired."),
                message("PASSWORD_REUSE", "The new password cannot be a recently used password."),
                message("RATE_LIMIT_EXCEEDED", "Too many requests. Please try again later."),
                message("MALFORMED_REQUEST_BODY", "Malformed request body."),
                message("TYPE_MISMATCH", "The request parameter has an invalid value."),
                message("INVALID_SORT_PROPERTY", "The requested sort property is invalid."),
                message("MISSING_PARAMETER", "A required request parameter is missing."),
                message("METHOD_NOT_SUPPORTED", "The HTTP method is not supported for this endpoint."),
                message("MEDIA_TYPE_NOT_SUPPORTED", "The request content type is not supported."),
                message("ENDPOINT_NOT_FOUND", "The requested endpoint was not found."),
                message("DATA_INTEGRITY_VIOLATION", "The operation conflicts with existing or related data."),
                message("MAIL_FAILURE", "Email service is temporarily unavailable. Please try again later."),
                message("BAD_CREDENTIALS", "Invalid username or password."),
                message("ACCESS_DENIED", "You do not have permission to perform this action."),
                message("VALIDATION_FAILED", "Validation failed."),
                message("UNEXPECTED_ERROR", "An unexpected error occurred."),
                message("AUTHENTICATION_REQUIRED", "Authentication is required to access this resource."),
                message("CSRF_TOKEN_INVALID",
                        "CSRF token is missing or invalid. Reload Swagger UI or initialize CSRF protection and retry."));

        return args -> messages.forEach(message -> repository.findByCode(message.getCode())
                .orElseGet(() -> repository.save(message)));
    }

    private ErrorMessageDocument message(String code, String message) {
        return ErrorMessageDocument.builder()
                .code(code)
                .message(message)
                .build();
    }
}
