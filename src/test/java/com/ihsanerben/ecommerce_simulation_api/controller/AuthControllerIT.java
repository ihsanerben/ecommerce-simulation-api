package com.ihsanerben.ecommerce_simulation_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.LoginRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.RevokedAccessTokenRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RevokedAccessTokenRepository revokedAccessTokenRepository;

    @Autowired
    private CartRepository cartRepository;

    @AfterEach
    void tearDown() {
        cartRepository.deleteAll();
        userRepository.deleteAll();
        revokedAccessTokenRepository.deleteAll();
    }

    @Test
    void register_withValidRequest_returns201AndHttpOnlyCookies() throws Exception {
        RegisterRequest request = new RegisterRequest("ihsan", "ihsan@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(jsonPath("$.username").value("ihsan"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_withDuplicateUsername_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        RegisterRequest duplicate = new RegisterRequest("ihsan", "other@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("ihsan", "not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void login_withValidCredentials_returns200AndHttpOnlyCookies() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("ihsan", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("ihsan", "wrong-password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_rotatesTokenAndRejectsReusingTheOldRefreshToken() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("ihsan", "ihsan@example.com", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse();
        Cookie oldRefresh = registration.getCookie("refresh_token");

        mockMvc.perform(post("/api/auth/refresh").cookie(oldRefresh))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refresh_token", org.hamcrest.Matchers.not(oldRefresh.getValue())));

        mockMvc.perform(post("/api/auth/refresh").cookie(oldRefresh))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_blacklistsAccessTokenAndClearsCookies() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("ihsan", "ihsan@example.com", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse();
        Cookie access = registration.getCookie("access_token");
        Cookie refresh = registration.getCookie("refresh_token");

        mockMvc.perform(get("/api/cart").cookie(access)).andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/logout").cookie(access, refresh))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0));
        mockMvc.perform(get("/api/cart").cookie(access))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/cart"));
        mockMvc.perform(post("/api/auth/refresh").cookie(refresh)).andExpect(status().isUnauthorized());
    }
}
