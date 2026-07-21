package com.ihsanerben.ecommerce_simulation_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ErrorResponseIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void unauthenticatedRequest_returnsStandard401Body() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/cart"));
    }

    @Test
    void malformedJson_returnsStandard400Body() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body."))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void unsupportedMediaType_returnsStandard415Body() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not-json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void unsupportedHttpMethod_returnsStandard405Body() throws Exception {
        mockMvc.perform(get("/api/auth/register"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void missingDomainResource_returnsStandard404Body() throws Exception {
        mockMvc.perform(get("/api/products/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/products/999999999"));
    }

    @Test
    void validationAndConflictResponses_followTheSameContract() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("x", "invalid-email", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        RegisterRequest valid = new RegisterRequest("error-user", "error@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void openApi_documentsStandardErrorSchemaForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse").exists())
                .andExpect(jsonPath("$.paths['/api/auth/csrf'].get.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/csrf'].get.responses['200']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/products'].get.parameters[?(@.name == 'page')]").exists())
                .andExpect(jsonPath("$.paths['/api/products'].get.parameters[?(@.name == 'size')]").exists())
                .andExpect(jsonPath("$.paths['/api/products'].get.parameters[?(@.name == 'sort')]").exists())
                .andExpect(jsonPath("$.paths['/api/cart'].get.responses['401'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErrorResponse"))
                .andExpect(jsonPath("$.paths['/api/cart'].get.responses['403'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErrorResponse"))
                .andExpect(jsonPath("$.paths['/api/cart'].get.responses['500'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErrorResponse"));
    }
}
