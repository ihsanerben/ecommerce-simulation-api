package com.ihsanerben.ecommerce_simulation_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.RevokedAccessTokenRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.security.csrf-enabled=true")
class SwaggerCookieAuthFlowIT extends AbstractIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CartRepository carts;
    @Autowired UserRepository users;
    @Autowired RevokedAccessTokenRepository revokedTokens;

    @AfterEach
    void tearDown() {
        carts.deleteAll();
        users.deleteAll();
        revokedTokens.deleteAll();
    }

    @Test
    void swaggerStyleFlow_usesCsrfAndHttpOnlyCookiesEndToEnd() throws Exception {
        Cookie csrf = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("ECOMMERCE-XSRF-TOKEN"))
                .andReturn().getResponse().getCookie("ECOMMERCE-XSRF-TOKEN");

        var registration = mockMvc.perform(post("/api/auth/register")
                        .cookie(csrf)
                        .header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("swagger-user", "swagger@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andReturn().getResponse();

        Cookie access = registration.getCookie("access_token");
        Cookie refresh = registration.getCookie("refresh_token");
        mockMvc.perform(get("/api/cart").cookie(access)).andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(csrf, refresh)
                        .header("X-XSRF-TOKEN", csrf.getValue()))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().value("refresh_token", org.hamcrest.Matchers.not(refresh.getValue())));
    }

    @Test
    void stateChangingRequest_withoutCsrfHeader_returnsStandard403Body() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("csrf-user", "csrf@example.com", "password123"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(
                        "CSRF token is missing or invalid. Reload Swagger UI or initialize CSRF protection and retry."))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void loadingSwaggerUi_initializesCsrfCookieBeforeTheFirstPost() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("ECOMMERCE-XSRF-TOKEN"));

        mockMvc.perform(get("/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("requestInterceptor")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ECOMMERCE-XSRF-TOKEN")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("X-XSRF-TOKEN")));
    }
}
